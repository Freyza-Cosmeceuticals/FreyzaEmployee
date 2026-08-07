package com.freyza.employee.data.repository

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.BeginReportRequest
import com.freyza.employee.data.network.dto.BeginReportResponse
import com.freyza.employee.data.network.dto.DailyReportDto
import com.freyza.employee.data.network.dto.PointOfInterestDto
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.data.network.dto.VisitDto
import com.freyza.employee.data.network.dto.VisitUpdateDto
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.PointOfInterest
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.repository.DailyReportRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PoisResponse(
  @SerialName("success")
  val success: Boolean,
  @SerialName("data")
  val data: List<PointOfInterestDto>,
)

@Serializable
data class VisitCreateResponse(
  @SerialName("success")
  val success: Boolean,
  @SerialName("data")
  val data: VisitDto,
)

@Serializable
data class PoiResponse(
  @SerialName("success")
  val success: Boolean,
  @SerialName("data")
  val data: PointOfInterestDto,
)

@Serializable
data class ErrorResponse(
  @SerialName("message")
  val message: String,
)

class DailyReportRepositoryImpl(
  private val postgrest: Postgrest,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val auth: Auth,
) : DailyReportRepository {
  private val poiCache = mutableMapOf<String, List<PointOfInterest>>()
  private val reportCache = mutableMapOf<String, DailyReport>()

  companion object {
    const val TAG: String = "DailyReportRepo"
    private const val TABLE_DAILY_REPORT = "dailyReport"
    private const val TABLE_VISIT = "visit"
    private const val SELECT_WITH_VISITS = "*, visits:visit!visit_reportId_fkey(*)"
    private const val SELECT_ALL = "*"
  }

  override suspend fun getTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    withVisits: Boolean,
  ): Result<DailyReport?> {
    return try {
      val thisDate = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)
      val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

      withContext(Dispatchers.IO) {
        Logger.d(
          TAG,
          "Querying dailyReport for current employee and date: $thisDate, withVisits=${withVisits}"
        )

        val dailyReportDto = postgrest.from(TABLE_DAILY_REPORT).select(
          columns = Columns.raw(selectQuery)
        ) {
          filter {
            DailyReportDto::employeeId eq employeeId
            DailyReportDto::date eq today
          }
        }.decodeSingleOrNull<DailyReportDto>()

        val dailyReport = dailyReportDto?.toDomain()
        if (dailyReport != null) {
          reportCache[dailyReport.id] = dailyReport
        }
        Result.Success(dailyReport)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getRecentDailyReports(
    numDailyReports: Int,
    employeeId: String,
    withVisits: Boolean,
  ): Result<List<DailyReport>> {
    val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying recent daily reports withVisits=${withVisits}")

        val reportsDto = postgrest.from(TABLE_DAILY_REPORT).select(
          columns = Columns.raw(selectQuery)
        ) {
          filter {
            DailyReportDto::employeeId eq employeeId
          }
          order(DailyReportDto::date.name, Order.DESCENDING)
          limit(numDailyReports.toLong())
        }.decodeList<DailyReportDto>()

        val reports = reportsDto.map { it.toDomain() }
        reports.forEach { reportCache[it.id] = it }
        Result.Success(reports)
      }
    } catch (e: Exception) {
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getVisits(dailyReportId: String): Result<List<Visit>> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying visits for current employee and dailyReportId: $dailyReportId")

        val visitsDto = postgrest.from(TABLE_VISIT).select {
          filter {
            VisitDto::reportId eq dailyReportId
          }
        }.decodeList<VisitDto>()

        val visits = visitsDto.map { it.toDomain() }
        Result.Success(visits)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getDailyReport(
    id: String,
    withVisits: Boolean,
    forceRefresh: Boolean,
  ): Result<DailyReport?> {
    if (!forceRefresh && reportCache.containsKey(id)) {
      val cached = reportCache[id]!!
      // If we need visits but cache doesn't have them (or it's empty but we expect some), 
      // we might want to fetch. But for now, let's say if withVisits is true, we must have them in cache.
      // Actually, if we cached it WITH visits, we're good. If we cached it WITHOUT, and now need them, fetch.
      if (!withVisits || cached.visits.isNotEmpty()) {
        Logger.d(TAG, "Cache hit for dailyReport ID: $id (withVisits=$withVisits)")
        return Result.Success(cached)
      }
    }

    val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying dailyReport with id: $id withVisits=$withVisits from network")

        val dailyReportDto = postgrest.from(TABLE_DAILY_REPORT).select(
          columns = Columns.raw(selectQuery)
        ) {
          filter {
            DailyReportDto::id eq id
          }
        }.decodeSingleOrNull<DailyReportDto>()

        val dailyReport = dailyReportDto?.toDomain()
        if (dailyReport != null) {
          reportCache[id] = dailyReport
        }
        Result.Success(dailyReport)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getVisit(id: String): Result<Visit?> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying visit with id: $id")

        val visitDto = postgrest.from(TABLE_VISIT).select {
          filter {
            VisitDto::id eq id
          }
        }.decodeSingleOrNull<VisitDto>()

        val visit = visitDto?.toDomain()
        Result.Success(visit)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getPois(
    locationId: String,
    visitType: VisitType,
    forceRefresh: Boolean,
  ): Result<List<PointOfInterest>> {
    // 1. Read from cache if available and not force refreshing
    if (!forceRefresh) {
      val cached = poiCache[locationId]?.filter { it.type == visitType }
      if (!cached.isNullOrEmpty()) {
        Logger.d(TAG, "Cache hit for POIs (Type: $visitType) at location: $locationId")
        return Result.Success(cached)
      }
    }

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching POIs for locationId: $locationId, visitType: $visitType")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.get("${appConfig.apiUrl}/api/pois") {
          header(HttpHeaders.Authorization, "Bearer $token")
          parameter("locationId", locationId)
          parameter("visitType", visitType.name)
        }

        if (response.status.isSuccess()) {
          val poisResponse = response.body<PoisResponse>()
          if (poisResponse.success) {
            val pois = poisResponse.data.map { dto ->
              dto.toDomain()
            }

            // NOTE: We do not store the result in cache here because this fetch only 
            // returns POIs for a specific visitType. We want the locationId entry in 
            // cache to always be complete (containing all POIs for that location).

            Logger.d(
              TAG,
              "Fetched ${pois.size} POIs for locationId: $locationId, visitType: $visitType"
            )
            Result.Success(pois)
          } else {
            Result.Error("Unable to fetch POIs")
          }
        } else {
          val error = response.body<ErrorResponse>()
          Logger.e(TAG, "API Error: ${response.status} - $error")
          Result.Error("Error: ${error.message}")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getPoisByLocation(
    locationId: String,
    forceRefresh: Boolean,
  ): Result<List<PointOfInterest>> {
    // 1. Read from cache if available and not force refreshing
    if (!forceRefresh) {
      val cached = poiCache[locationId]
      if (cached != null) {
        Logger.d(TAG, "Cache hit for all POIs at location: $locationId")
        return Result.Success(cached)
      }
    }

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching all POIs for locationId: $locationId")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.get("${appConfig.apiUrl}/api/pois") {
          header(HttpHeaders.Authorization, "Bearer $token")
          parameter("locationId", locationId)
        }

        if (response.status.isSuccess()) {
          val poisResponse = response.body<PoisResponse>()
          if (poisResponse.success) {
            val pois = poisResponse.data.map { dto ->
              dto.toDomain()
            }

            // 2. Assured that this entry contains all POIs for the location
            poiCache[locationId] = pois

            Logger.d(TAG, "Fetched and cached ${pois.size} POIs for locationId: $locationId")
            Result.Success(pois)
          } else {
            Result.Error("Unable to fetch POIs")
          }
        } else {
          val error = response.body<ErrorResponse>()
          Logger.e(TAG, "API Error: ${response.status} - $error")
          Result.Error("Error: ${error.message}")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getPoi(id: String): Result<PointOfInterest?> {
    // Check cache first across all locations
    poiCache.values.flatten().find { it.id == id }?.let {
      Logger.d(TAG, "Cache hit for POI with id: $id")
      return Result.Success(it)
    }

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching POI with id: $id")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.get("${appConfig.apiUrl}/api/pois/$id") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (response.status.isSuccess()) {
          val poiResponse = response.body<PoiResponse>()
          if (poiResponse.success) {
            val poi = poiResponse.data.toDomain()

            poiCache[poi.locationId]?.let { cachedList ->
              poiCache[poi.locationId] = (cachedList + poi).distinctBy { it.id }
              Logger.d(
                TAG,
                "Updated location cache for ${poi.locationId} with fetched POI: ${poi.id}"
              )
            }

            Result.Success(poi)
          } else {
            Result.Error("Unable to fetch POI")
          }
        } else if (response.status.value == 404) {
          Result.Success(null)
        } else {
          val error = response.body<ErrorResponse>()
          Logger.e(TAG, "API Error: ${response.status} - $error")
          Result.Error("Error: ${error.message}")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun createTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    dayType: DayType,
    routeId: String?,
    travellingWithId: String?,
  ): Result<DailyReport> {
    return try {
      val thisDate = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)

      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Creating dailyReport via api for date: $thisDate")

        val request = BeginReportRequest(
          date = thisDate,
          dayType = dayType,
          routeId = routeId,
          travellingWithId = travellingWithId
        )

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.post("${appConfig.apiUrl}/api/reports/begin") {
          contentType(ContentType.Application.Json)
          header(HttpHeaders.Authorization, "Bearer $token")
          setBody(request)
        }

        if (response.status.isSuccess()) {
          val beginResponse = response.body<BeginReportResponse>()
          if (beginResponse.success) {
            Logger.d(TAG, "Daily report created successfully")
            val report = beginResponse.data.toDomain()
            reportCache[report.id] = report
            Result.Success(report)
          } else {
            Logger.e(TAG, "Unable to create daily report. ${response.status.description}")
            Result.Error("Unable to create daily report")
          }
        } else {
          val error = response.body<ErrorResponse>()
          Logger.e(TAG, "API Error: ${response.status} - $error")
          Result.Error("Error: ${error.message}")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun createVisit(
    today: LocalDate,
    employeeId: String,
    dailyReportId: String,
    visitCreateDto: VisitCreateDto,
  ): Result<Visit> {
    return try {
      val thisDate = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)

      withContext(Dispatchers.IO) {
        Logger.d(
          TAG,
          "Creating visit via API for dailyReportId: $dailyReportId and date: $thisDate"
        )

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.post("${appConfig.apiUrl}/api/visits/create") {
          contentType(ContentType.Application.Json)
          header(HttpHeaders.Authorization, "Bearer $token")
          setBody(visitCreateDto)
        }

        if (response.status.isSuccess()) {
          val createResponse = response.body<VisitCreateResponse>()
          if (createResponse.success) {
            val visit = createResponse.data.toDomain()
            // Invalidate associated report cache as visits changed
            reportCache.remove(dailyReportId)
            Result.Success(visit)
          } else {
            Result.Error("Unable to create visit")
          }
        } else {
          val error = response.body<ErrorResponse>()
          Logger.e(TAG, "API Error: ${response.status} - $error")
          Result.Error("Error: ${error.message}")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun lockReport(reportId: String): Result<Boolean> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Locking report:$reportId")

        postgrest.from(TABLE_DAILY_REPORT).update(
          {
            DailyReportDto::locked setTo true
          }
        ) {
          filter {
            DailyReportDto::id eq reportId
          }
        }

        reportCache.clear()
        Result.Success(true)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun deleteVisit(visitId: String): Result<Boolean> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Deleting visit:$visitId")

        postgrest.from(TABLE_VISIT).delete {
          filter {
            VisitDto::id eq visitId
          }
        }

        reportCache.clear()
        Result.Success(true)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun updateVisit(
    visitId: String,
    visitUpdateDto: VisitUpdateDto,
  ): Result<Visit> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Updating visit via API visit:$visitId")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.put("${appConfig.apiUrl}/api/visits/${visitId}") {
          contentType(ContentType.Application.Json)
          header(HttpHeaders.Authorization, "Bearer $token")
          setBody(visitUpdateDto)
        }

        if (response.status.isSuccess()) {
          val updateResponse = response.body<VisitCreateResponse>()
          if (updateResponse.success) {
            val visit = updateResponse.data.toDomain()
            // Invalidate associated report cache
            reportCache.remove(visit.reportId)
            Result.Success(visit)
          } else {
            Result.Error("Unable to update visit")
          }
        } else {
          val error = response.body<ErrorResponse>()
          Logger.e(TAG, "API Error: ${response.status} - $error")
          Result.Error("Error: ${error.message}")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }
}
