package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.PointOfInterest
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyPoiDoctor
import com.freyza.employee.domain.model.dummyVisitDoctorAllTrue

data class VisitDetailUiState(
  val visitId: String,
  val visit: UIState<Visit?> = UIState.Idle(),
  val poi: UIState<PointOfInterest?> = UIState.Idle(),
  val report: UIState<DailyReport?> = UIState.Idle(),
  val deletingState: UIState<Unit> = UIState.Idle(),
)

fun dummyVisitDetailUiState() = VisitDetailUiState(
  visitId = "fdc8b26f-9a2c-4789-88b2-6f9a2cf789b8",
  visit = UIState.Ready(dummyVisitDoctorAllTrue()),
  poi = UIState.Ready(dummyPoiDoctor()),
  report = UIState.Ready(dummyDailyReportWork(locked = false, dateNow = true))
)

fun dummyVisitDetailErrorUiState() = VisitDetailUiState(
  visitId = "fdc8b26f-9a2c-4789-88b2-6f9a2cf789b8",
  visit = UIState.Error("Previews don't need visits"),
  report = UIState.Error("report not there")
)
