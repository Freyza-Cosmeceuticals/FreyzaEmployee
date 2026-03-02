package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.blankLocation
import com.freyza.employee.domain.model.routeName
import com.freyza.employee.domain.model.toRouteWithLocation
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.dailyreport.GetTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.domain.usecase.location.GetLocationUseCase
import com.freyza.employee.domain.usecase.route.GetAllRoutesWithLocationUseCase
import com.freyza.employee.domain.usecase.route.GetRouteUseCase
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class HomeViewModel(
  private val sessionManager: SessionManager,
  private val getRecentExpensesUseCase: GetRecentExpensesUseCase,
  private val getCurrentTravelPlanUseCase: GetCurrentTravelPlanUseCase,
  private val getTodayTravelPlanEntryUseCase: GetTodayTravelPlanEntryUseCase,
  private val getRouteUseCase: GetRouteUseCase,
  private val getLocationUseCase: GetLocationUseCase,
  private val getAllRoutesWithLocationUseCase: GetAllRoutesWithLocationUseCase,
  private val getTodayDailyReportUseCase: GetTodayDailyReportUseCase,
  private val createTodayDailyReportUseCase: CreateTodayDailyReportUseCase,
) : ViewModel() {

  companion object {
    const val TAG = "HomeViewModel"
  }

  private val _uiState = MutableStateFlow(HomeScreenUiState())
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeScreenUiState())

  init {
    Logger.d(TAG, "Init")
  }

  fun refresh() {
    Logger.d(TAG, "Refreshing data")

    val employeeId = sessionManager.currentEmployee.value?.id
    if (employeeId != null) {
      loadCurrentDailyReport(employeeId)
      loadAllRoutes()
      loadCurrentTravelPlan(employeeId)
    }
  }

  fun loadRecentExpenses() {
    viewModelScope.launch {
      val result =
        getRecentExpensesUseCase.execute(GetRecentExpensesUseCase.Input(Constants.NUM_RECENT_EXPENSES))

      when (result) {
        is GetRecentExpensesUseCase.Output.Success -> {
          _uiState.update {
            it.copy(recentExpenses = UIState.Ready(result.expenses))
          }
          Logger.d(TAG, "${result.expenses.size} Recent Expenses Fetched Successfully")
        }

        is GetRecentExpensesUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(recentExpenses = UIState.Error(message = "Unable to fetch recent expenses"))
          }
          Logger.e(TAG, "Cannot fetch recent expenses: ${result.message}")
        }
      }
    }
  }

  fun loadCurrentTravelPlan(employeeId: String) {
    Logger.i(TAG, "Fetching current travel plan for emp:$employeeId")

    _uiState.update {
      it.copy(
        currentTravelPlan = UIState.Loading(it.currentTravelPlan.data),
        todayTravelPlanEntry = UIState.Loading(it.todayTravelPlanEntry.data),
        todayPlanEntryRoute = UIState.Loading(it.todayPlanEntryRoute.data)
      )
    }
    viewModelScope.launch {
      when (val result = getCurrentTravelPlanUseCase.execute(
        GetCurrentTravelPlanUseCase.Input(
          employeeId
        )
      )) {
        is GetCurrentTravelPlanUseCase.Output.Success -> {
          _uiState.update {
            it.copy(currentTravelPlan = UIState.Ready(result.travelPlan))
          }
          Logger.d(TAG, "tpId:${result.travelPlan?.id} Current Travel Plan Fetched Successfully")

          // fetch today's entry
          if (result.travelPlan?.id !== null) {
            loadTodayTravelPlanEntry(result.travelPlan.id)
          } else {
            // since we can't fetch any further data, set these to ready
            _uiState.update {
              it.copy(
                todayTravelPlanEntry = UIState.Ready(null),
                todayPlanEntryRoute = UIState.Ready(null)
              )
            }
          }
        }

        is GetCurrentTravelPlanUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              currentTravelPlan = UIState.Error(message = "Unable to fetch current travel plan")
            )
          }
          Logger.e(TAG, "Cannot fetch current travel plan: ${result.message}")
        }
      }
    }
  }

  private fun loadTodayTravelPlanEntry(tpId: String) {
    Logger.i(TAG, "Fetching today travel plan entry for tp:$tpId")

    _uiState.update {
      it.copy(
        todayTravelPlanEntry = UIState.Loading(it.todayTravelPlanEntry.data),
        todayPlanEntryRoute = UIState.Loading(it.todayPlanEntryRoute.data)
      )
    }
    viewModelScope.launch {
      when (val result = getTodayTravelPlanEntryUseCase.execute(
        GetTodayTravelPlanEntryUseCase.Input(tpId)
      )) {
        is GetTodayTravelPlanEntryUseCase.Output.Success -> {
          _uiState.update {
            it.copy(todayTravelPlanEntry = UIState.Ready(result.travelPlanEntry))
          }
          Logger.d(
            TAG, "${result.travelPlanEntry?.id} Today Travel Plan Entry Fetched Successfully"
          )

          // fetch plan's route
          if (result.travelPlanEntry?.routeId !== null) loadCurrentRoute(result.travelPlanEntry.routeId)
          else {
            _uiState.update {
              it.copy(
                todayPlanEntryRoute = UIState.Ready(null)
              )
            }
          }
        }

        is GetTodayTravelPlanEntryUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              todayTravelPlanEntry = UIState.Error(message = "Unable to fetch today travel plan entry")
            )
          }
          Logger.e(TAG, "Cannot fetch today travel plan entry: ${result.message}")
        }
      }
    }
  }

  private fun loadCurrentRoute(routeId: String) {
    Logger.i(TAG, "Fetching route with $routeId")

    _uiState.update {
      it.copy(
        todayPlanEntryRoute = UIState.Loading(it.todayPlanEntryRoute.data)
      )
    }
    viewModelScope.launch {
      when (val result = getRouteUseCase.execute(GetRouteUseCase.Input(routeId))) {
        is GetRouteUseCase.Output.Success -> {
          _uiState.update {
            it.copy(
              todayPlanEntryRoute = UIState.Loading(
                result.route?.toRouteWithLocation(
                  blankLocation(), blankLocation()
                )
              )
            )
          }
          Logger.d(
            TAG, "${result.route?.id} Current (Partial) Route Fetched Successfully ${result.route}"
          )

          // fetch both locations if route exists
          if (result.route !== null) {
            loadLocationPair(result.route.srcLocId, result.route.destLocId)
          } else {
            _uiState.update {
              it.copy(
                todayPlanEntryRoute = UIState.Ready(it.todayPlanEntryRoute.data)
              )
            }
          }
        }

        is GetRouteUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              todayPlanEntryRoute = UIState.Error(message = "Unable to fetch the route")
            )
          }
          Logger.e(TAG, "Cannot fetch current route: ${result.message}")
        }
      }
    }
  }

  private fun loadLocationPair(srcLocId: String, destLocId: String) {
    Logger.i(TAG, "Fetching location pair for $srcLocId -> $destLocId")

    // should already be in loading
    _uiState.update { it.copy(todayPlanEntryRoute = UIState.Loading(it.todayPlanEntryRoute.data)) }

    viewModelScope.launch {
      coroutineScope {
        val srcDeferred = async(Dispatchers.IO) {
          getLocationUseCase.execute(GetLocationUseCase.Input(srcLocId))
        }

        val destDeferred = async(Dispatchers.IO) {
          getLocationUseCase.execute(GetLocationUseCase.Input(destLocId))
        }

        val srcResult = srcDeferred.await()
        val destResult = destDeferred.await()

        if (srcResult is GetLocationUseCase.Output.Success && destResult is GetLocationUseCase.Output.Success) {
          val pair =
            if (srcResult.location != null && destResult.location != null) srcResult.location to destResult.location else null
          _uiState.update {
            it.copy(
              todayPlanEntryRoute = UIState.Ready(
                if (pair != null) it.todayPlanEntryRoute.data?.copy(
                  srcLoc = pair.first, destLoc = pair.second
                ) else it.todayPlanEntryRoute.data
              )
            )
          }

          Logger.d(
            TAG,
            "${srcResult.location?.id} -> ${destResult.location?.id} Location Pair Fetched Successfully ${srcResult.location} -> ${destResult.location}"
          )
        } else {
          _uiState.update {
            it.copy(
              todayPlanEntryRoute = UIState.Error(message = "Unable to fetch locations")
            )
          }

          if (srcResult is GetLocationUseCase.Output.Failure) Logger.e(
            TAG, "Cannot fetch location pair: ${srcResult.message}"
          )
          if (destResult is GetLocationUseCase.Output.Failure) Logger.e(
            TAG, "Cannot fetch location pair: ${destResult.message}"
          )
        }
      }
    }
  }

  fun loadCurrentDailyReport(
    employeeId: String? = sessionManager.currentEmployee.value?.id,
  ) {
    Logger.i(TAG, "Fetching current daily report for emp:$employeeId")

    if (employeeId == null) {
      Logger.e(TAG, "Current employee not set. cannot load daily report, aborting")
      return
    }

    _uiState.update {
      it.copy(
        currentDailyReport = UIState.Loading(
          it.currentDailyReport.data, "Loading Daily Report"
        )
      )
    }

    _uiState.update {
      it.copy(
        todayReportDayType = UIState.Loading(),
        todayReportRoute = UIState.Loading()
      )
    }

    viewModelScope.launch {
      when (val result = getTodayDailyReportUseCase.execute(
        GetTodayDailyReportUseCase.Input(
          Clock.System.todayIn(TimeZone.of(Constants.TIMEZONE)), employeeId
        )
      )) {
        is GetTodayDailyReportUseCase.Output.Success -> {
          _uiState.update {
            it.copy(currentDailyReport = UIState.Ready(result.dailyReport))
          }
          Logger.d(
            TAG, "dailyReportId:${result.dailyReport?.id} Today Daily Report Fetched Successfully"
          )

          if (result.dailyReport?.id == null) {
            Logger.i(
              TAG,
              "Today's Daily Report does not exists, need to create one before proceeding, showing the bottom sheet"
            )

            _uiState.update {
              it.copy(
                todayReportDayType = UIState.Idle(),
                todayReportRoute = UIState.Idle()
              )
            }
          } else {
            // set the resolved dayType and route if a valid dailyReport is found
            setReportDayType(result.dailyReport.dayType)
            result.dailyReport.routeId?.let { setReportRoute(it) }
          }
        }

        is GetTodayDailyReportUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              currentDailyReport = UIState.Error(message = "Unable to fetch today's daily report"),
              todayReportDayType = UIState.Error(message = "Unable to fetch today's daily report"),
              todayReportRoute = UIState.Error(message = "Unable to fetch today's daily report")
            )
          }
          Logger.e(TAG, "Cannot fetch today's daily report: ${result.message}")
        }
      }
    }
  }

  private fun setReportDayType(dayType: DayType) {
    Logger.d(TAG, "Setting reportDayType to ${dayType.titleCase()}")
    _uiState.update {
      it.copy(todayReportDayType = UIState.Ready(dayType))
    }
  }

  private fun setReportRoute(routeId: String) {
    if (_uiState.value.routes.data?.isEmpty() != true) {
      loadAllRoutes()
    }

    _uiState.update {
      it.copy(todayReportRoute = UIState.Loading(it.todayReportRoute.data))
    }

    viewModelScope.launch {
      _uiState.map { it.routes }.filterNotNull().filter { it.data.isNullOrEmpty().not() }.first()
        .let { availableRoute ->
          val route = availableRoute.data?.find { it.id == routeId }

          Logger.d(TAG, "Setting reportRoute to ${route?.routeName()}")
          _uiState.update {
            it.copy(todayReportRoute = UIState.Ready(route))
          }
        }
    }
  }

  fun createCurrentDailyReport(dayType: DayType, routeId: String?) {
    val employeeId = sessionManager.currentEmployee.value?.id
    Logger.i(TAG, "Creating current daily report for emp:$employeeId")
    if (employeeId == null) {
      Logger.e(TAG, "Current employee not set. cannot create daily report, aborting")
      return
    }

    _uiState.update {
      it.copy(
        currentDailyReport = UIState.Loading(
          it.currentDailyReport.data, "Creating Daily Report"
        )
      )
    }

    viewModelScope.launch {
      when (val result = createTodayDailyReportUseCase.execute(
        CreateTodayDailyReportUseCase.Input(
          Clock.System.todayIn(TimeZone.of(Constants.TIMEZONE)), employeeId, dayType, routeId
        )
      )) {
        is CreateTodayDailyReportUseCase.Output.Success -> {
          _uiState.update {
            it.copy(currentDailyReport = UIState.Ready(result.dailyReport))
          }
          Logger.d(
            TAG, "dailyReportId:${result.dailyReport?.id} Today Daily Report Created Successfully"
          )

          if (result.dailyReport?.id == null) {
            Logger.i(
              TAG, "Today's Daily Report was not created, check logs"
            )
          } else {
            // set the resolved dayType and route if a valid dailyReport is created
            setReportDayType(result.dailyReport.dayType)
            result.dailyReport.routeId?.let { setReportRoute(it) }
          }
        }

        is CreateTodayDailyReportUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              currentDailyReport = UIState.Error(message = "Unable to create today's daily report")
            )
          }
          Logger.e(TAG, "Cannot create today's daily report: ${result.message}")
        }
      }
    }
  }

  private fun loadAllRoutes() {
    Logger.i(TAG, "Fetching all routes")

    _uiState.update {
      it.copy(routes = UIState.Loading(it.routes.data))
    }

    viewModelScope.launch {
      when (val result =
        getAllRoutesWithLocationUseCase.execute(GetAllRoutesWithLocationUseCase.Input())) {
        is GetAllRoutesWithLocationUseCase.Output.Success -> {
          _uiState.update {
            it.copy(routes = UIState.Ready(result.routes))
          }
          Logger.d(
            TAG, "All routes fetched successfully: ${result.routes.size} routes"
          )
        }

        is GetAllRoutesWithLocationUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              routes = UIState.Error(message = "Unable to routes")
            )
          }
          Logger.e(TAG, "Cannot fetch all routes with location: ${result.message}")
        }
      }
    }
  }
}
