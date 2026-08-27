package com.freyza.employee.data.repository

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Result
import com.freyza.employee.core.network.ApiException
import com.freyza.employee.core.network.ApiErrorResponse
import com.freyza.employee.core.network.safeApiCall
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
  val data: PointOfInterestDto?,
)


class DailyReportRepositoryImpl(
  private val postgrest: Postgrest,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val auth: Auth,
) : DailyReportRepository {
  // Cache for complete POI lists keyed by location ID
  private val poiCache = mutableMapOf<String, List<PointOfInterest>>()

  // Cache for daily reports keyed by report ID
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
    forceRefresh: Boolean
  ): Result<DailyReport?> {
    return safeApiCall(TAG) {
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
        dailyReport
      }
    }
  }

  override suspend fun getRecentDailyReports(
    numDailyReports: Int,
    employeeId: String,
    withVisits: Boolean,
  ): Result<List<DailyReport>> {
    val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

    return safeApiCall(TAG) {
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
        reports
      }
    }
  }

  override suspend fun getVisits(dailyReportId: String): Result<List<Visit>> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying visits for current employee and dailyReportId: $dailyReportId")

        val visitsDto = postgrest.from(TABLE_VISIT).select {
          filter {
            VisitDto::reportId eq dailyReportId
          }
          order(VisitDto::createdAt.name, Order.DESCENDING)
        }.decodeList<VisitDto>()

        visitsDto.map { it.toDomain() }
      }
    }
  }

  override suspend fun getDailyReport(
    id: String,
    withVisits: Boolean,
    forceRefresh: Boolean,
  ): Result<DailyReport?> {
    if (!forceRefresh && reportCache.containsKey(id)) {
      val cached = reportCache[id]!!
      // If we need visits but cache doesn't have them (or it's empty, but we expect some),
      // we might want to fetch. But for now, let's say if withVisits is true, we must have them in cache.
      // Actually, if we cached it WITH visits, we're good. If we cached it WITHOUT, and now need them, fetch.
      if (!withVisits || cached.visits.isNotEmpty()) {
        Logger.d(TAG, "Cache hit for dailyReport ID: $id (withVisits=$withVisits)")
        return Result.Success(cached)
      }
    }

    val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

    return safeApiCall(TAG) {
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
        dailyReport
      }
    }
  }

  override suspend fun getVisit(id: String, forceRefresh: Boolean): Result<Visit?> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying visit with id: $id")

        val visitDto = postgrest.from(TABLE_VISIT).select {
          filter {
            VisitDto::id eq id
          }
        }.decodeSingleOrNull<VisitDto>()

        visitDto?.toDomain()
      }
    }
  }

  override suspend fun getPois(
    locationIds: List<String>,
    visitType: VisitType,
    forceRefresh: Boolean,
  ): Result<List<PointOfInterest>> {
    val cacheKey = locationIds.sorted().joinToString(",")

    // 1. Read from cache if available and not force refreshing
    if (!forceRefresh) {
      val cached = poiCache[cacheKey]?.filter { it.type == visitType }
      if (!cached.isNullOrEmpty()) {
        Logger.d(TAG, "Cache hit for POIs (Type: $visitType) at locations: $cacheKey")
        return Result.Success(cached)
      }
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching POIs for locationIds: $cacheKey, visitType: $visitType")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.get("${appConfig.apiUrl}/api/pois") {
          header(HttpHeaders.Authorization, "Bearer $token")
          parameter("locationIds", cacheKey)
          parameter("visitType", visitType.name)
        }

        if (response.status.isSuccess()) {
          val poisResponse = response.body<PoisResponse>()
          if (poisResponse.success) {
            val pois = poisResponse.data.map { dto ->
              dto.toDomain()
            }

            // NOTE: We do not store the result in cache here because this fetch only 
            // returns POIs for a specific visitType. We want the cache entry 
            // to always be complete (containing all POIs for those locations).

            Logger.d(
              TAG,
              "Fetched ${pois.size} POIs for locationIds: $cacheKey, visitType: $visitType"
            )
            pois
          } else {
            throw Exception("Unable to fetch POIs")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw ApiException(response.status.value, error, error.message)
        }
      }
    }
  }

  override suspend fun getPoisByLocation(
    locationIds: List<String>,
    forceRefresh: Boolean,
  ): Result<List<PointOfInterest>> {
    val cacheKey = locationIds.sorted().joinToString(",")

    // 1. Read from cache if available and not force refreshing
    if (!forceRefresh) {
      val cached = poiCache[cacheKey]
      if (cached != null) {
        Logger.d(TAG, "Cache hit for all POIs at locations: $cacheKey")
        return Result.Success(cached)
      }
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching all POIs for locationIds: $cacheKey")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.get("${appConfig.apiUrl}/api/pois") {
          header(HttpHeaders.Authorization, "Bearer $token")
          parameter("locationIds", cacheKey)
        }

        if (response.status.isSuccess()) {
          val poisResponse = response.body<PoisResponse>()
          if (poisResponse.success) {
            val pois = poisResponse.data.map { dto ->
              dto.toDomain()
            }

            // 2. Assured that this entry contains all POIs for the locations
            poiCache[cacheKey] = pois

            Logger.d(TAG, "Fetched and cached ${pois.size} POIs for locationIds: $cacheKey")
            pois
          } else {
            throw Exception("Unable to fetch POIs")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw ApiException(response.status.value, error, error.message)
        }
      }
    }
  }

  override suspend fun getPoi(id: String): Result<PointOfInterest?> {
    // Check cache first across all locations
    poiCache.values.flatten().find { it.id == id }?.let {
      Logger.d(TAG, "Cache hit for POI with id: $id")
      return Result.Success(it)
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching POI with id: $id")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.get("${appConfig.apiUrl}/api/pois/$id") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (response.status.isSuccess()) {
          val poiResponse = response.body<PoiResponse>()
          if (poiResponse.success && poiResponse.data != null) {
            val poi = poiResponse.data.toDomain()

            poiCache[poi.locationId]?.let { cachedList ->
              poiCache[poi.locationId] = (cachedList + poi).distinctBy { it.id }
              Logger.d(
                TAG,
                "Updated location cache for ${poi.locationId} with fetched POI: ${poi.id}"
              )
            }

            poi
          } else {
            throw Exception("Unable to fetch POI")
          }
        } else if (response.status.value == 404) {
          null
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw ApiException(response.status.value, error, error.message)
        }
      }
    }
  }

  override suspend fun createTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    dayType: DayType,
    routeId: String?,
    travellingWithId: String?,
  ): Result<DailyReport> {
    return safeApiCall(TAG) {
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
            report
          } else {
            Logger.e(TAG, "Unable to create daily report. ${response.status.description}")
            throw Exception("Unable to create daily report")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw ApiException(response.status.value, error, error.message)
        }
      }
    }
  }

  override suspend fun createVisit(
    today: LocalDate,
    employeeId: String,
    dailyReportId: String,
    visitCreateDto: VisitCreateDto,
  ): Result<Visit> {
    return safeApiCall(TAG) {
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
            visit
          } else {
            throw Exception("Unable to create visit")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw ApiException(response.status.value, error, error.message)
        }
      }
    }
  }

  override suspend fun lockReport(reportId: String): Result<Boolean> {
    return safeApiCall(TAG) {
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
        true
      }
    }
  }

  override suspend fun deleteVisit(visitId: String): Result<Boolean> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Deleting visit:$visitId")

        postgrest.from(TABLE_VISIT).delete {
          filter {
            VisitDto::id eq visitId
          }
        }

        reportCache.clear()
        true
      }
    }
  }

  override suspend fun updateVisit(
    visitId: String,
    visitUpdateDto: VisitUpdateDto,
  ): Result<Visit> {
    return safeApiCall(TAG) {
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
            visit
          } else {
            throw Exception("Unable to update visit")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw ApiException(response.status.value, error, error.message)
        }
      }
    }
  }
}
