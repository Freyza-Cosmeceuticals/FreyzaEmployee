package com.freyza.employee.core

/**
 * Global Application Constants
 */
object Constants {
  const val TIMEZONE: String = "Asia/Kolkata"

  const val NUM_RECENT_DAILY_REPORTS = 5
  const val NUM_VISITS_DAILY_REPORT_CARD = 3

  const val MAX_VISIT_PRODUCT_QUANTITY = 2000
  const val MAX_PRODUCT_ENTRIES = 20

  // TTL Constants in Milliseconds
  const val TTL_VERSION_CHECK_MS: Long = 12 * 3600 * 1000L // 12 hours
  const val TTL_TIME_SYNC_MS: Long = 5 * 3600 * 1000L      // 5 hours
  const val TTL_MASTER_DATA_MS: Long = 24 * 3600 * 1000L   // 24 hours
}
