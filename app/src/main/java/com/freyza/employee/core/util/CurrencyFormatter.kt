package com.freyza.employee.core.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Serializer for BigDecimal to handle monetary values in DTOs
 */
object BigDecimalSerializer : KSerializer<BigDecimal> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): BigDecimal {
    return if (decoder is JsonDecoder) {
      val element = decoder.decodeJsonElement() as? JsonPrimitive
      // content handles both quoted strings and unquoted numbers seamlessly
      BigDecimal(element?.content ?: "0")
    } else {
      BigDecimal(decoder.decodeString())
    }
  }

  override fun serialize(encoder: Encoder, value: BigDecimal) =
    encoder.encodeString(value.toPlainString()) // toPlainString() prevents scientific notation (e.g., 1E+2)
}

/**
 * Money value class for type-safe monetary operations
 */
@JvmInline
value class Money(val amount: BigDecimal) : Comparable<Money> {

  // Kotlin supports +, -, *, / directly on BigDecimal
  operator fun plus(other: Money) = (amount + other.amount).toMoney()
  operator fun minus(other: Money) = (amount - other.amount).toMoney()
  operator fun times(multiplier: BigDecimal) = (amount * multiplier).toMoney()
  operator fun times(multiplier: Int) = (amount * multiplier.toBigDecimal()).toMoney()
  operator fun div(divisor: BigDecimal) = (amount.divide(divisor, 2, DEFAULT_ROUNDING)).toMoney()
  operator fun div(divisor: Int) =
    (amount.divide(divisor.toBigDecimal(), 2, DEFAULT_ROUNDING)).toMoney()

  override operator fun compareTo(other: Money): Int = amount.compareTo(other.amount)

  fun format() = CurrencyFormatter.format(amount)

  companion object {
    val DEFAULT_ROUNDING = RoundingMode.HALF_EVEN
    val ZERO = Money(BigDecimal.ZERO.setScale(2, DEFAULT_ROUNDING))

    fun fromDouble(value: Double?): Money {
      if (value == null) return ZERO
      return Money(BigDecimal.valueOf(value).setScale(2, DEFAULT_ROUNDING))
    }

    fun fromString(value: String?): Money {
      if (value.isNullOrBlank()) return ZERO
      return try {
        Money(BigDecimal(value).setScale(2, DEFAULT_ROUNDING))
      } catch (_: Exception) {
        ZERO
      }
    }
  }
}

/**
 * Currency formatter for consistent money display
 */
object CurrencyFormatter {
  private val locale = Locale.forLanguageTag("en-IN")

  // ThreadLocal caches the formatter per-thread, avoiding expensive recreation while maintaining thread safety
  private val formatter = ThreadLocal.withInitial {
    NumberFormat.getCurrencyInstance(locale).apply {
      minimumFractionDigits = 2
      maximumFractionDigits = 2
    }
  }

  fun format(amount: BigDecimal?): String {
    if (amount == null) return "-"
    return formatter.get()?.format(amount) ?: "N/A"
  }
}

/**
 * Extensions for easy conversion and formatting.
 */
fun Money?.toCurrencyString(): String = this?.format() ?: "-"
fun Double?.toMoney(): Money = Money.fromDouble(this)
fun String?.toMoney(): Money = Money.fromString(this)

private fun BigDecimal.toMoney() = Money(this.setScale(2, Money.DEFAULT_ROUNDING))
