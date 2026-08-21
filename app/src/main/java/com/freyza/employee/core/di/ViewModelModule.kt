package com.freyza.employee.core.di

import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import com.freyza.employee.presentation.ui.viewmodels.AppUpdateViewModel
import com.freyza.employee.presentation.ui.viewmodels.DailyReportViewModel
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.presentation.ui.viewmodels.ProfileViewModel
import com.freyza.employee.presentation.ui.viewmodels.ReportDetailViewModel
import com.freyza.employee.presentation.ui.viewmodels.SessionViewModel
import com.freyza.employee.presentation.ui.viewmodels.TravelPlanViewModel
import com.freyza.employee.presentation.ui.viewmodels.VisitDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
  viewModelOf(::SessionViewModel)
  viewModelOf(::LoginViewModel)
  viewModelOf(::HomeViewModel)
  viewModelOf(::TravelPlanViewModel)
  viewModelOf(::DailyReportViewModel)
  viewModel { (reportId: String) ->
    ReportDetailViewModel(
      reportId = reportId,
      sessionManager = get(),
      dailyReportRepository = get(),
      routeRepository = get(),
      userRepository = get(),
      lockReportUseCase = get(),
      snackbarManager = get(),
      serverTime = get()
    )
  }
  viewModel { (visitId: String) ->
    VisitDetailViewModel(
      visitId = visitId,
      sessionManager = get(),
      dailyReportRepository = get(),
      snackbarManager = get(),
      serverTime = get()
    )
  }
  viewModel { (visitType: VisitType, reportId: String, employeeId: String, visitId: String?) ->
    AddVisitViewModel(
      visitType = visitType,
      reportId = reportId,
      employeeId = employeeId,
      visitId = visitId,
      sessionManager = get(),
      createVisitUseCase = get(),
      dailyReportRepository = get(),
      routeRepository = get(),
      snackbarManager = get(),
      serverTime = get(),
      locationTracker = get()
    )
  }
  viewModelOf(::ProfileViewModel)
  viewModelOf(::AppUpdateViewModel)
}
