package com.freyza.employee.core.di

import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import com.freyza.employee.presentation.ui.viewmodels.DailyReportViewModel
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.presentation.ui.viewmodels.ProfileViewModel
import com.freyza.employee.presentation.ui.viewmodels.ReportDetailViewModel
import com.freyza.employee.presentation.ui.viewmodels.SessionViewModel
import com.freyza.employee.presentation.ui.viewmodels.TravelPlanViewModel
import com.freyza.employee.presentation.ui.viewmodels.VisitDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
  viewModel { SessionViewModel(get(), get()) }
  viewModel { LoginViewModel(get(), get(), get(), get()) }
  viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  viewModel { TravelPlanViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { DailyReportViewModel(get(), get(), get(), get(), get(), get()) }
  viewModel { (visitType: VisitType, reportId: String, employeeId: String) ->
    AddVisitViewModel(
      visitType, reportId, employeeId, get(), get(), get(), get()
  viewModel { (reportId: String) ->
    ReportDetailViewModel(
      reportId = reportId,
      sessionManager = get(),
      dailyReportRepository = get(),
      getAllRoutesWithLocationUseCase = get(),
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
  viewModel { (visitType: VisitType, reportId: String, employeeId: String) ->
    AddVisitViewModel(
      visitType = visitType,
      reportId = reportId,
      employeeId = employeeId,
      sessionManager = get(),
      createVisitUseCase = get(),
      snackbarManager = get(),
      serverTime = get()
    )
  }
  viewModel { ProfileViewModel(get(), get(), get()) }
}
