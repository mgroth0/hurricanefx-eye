package matt.hurricanefx.eye.bind

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableNumberValue
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.ObservableValue
import javafx.beans.value.WeakChangeListener
import javafx.beans.value.WritableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.util.StringConverter
import javafx.util.converter.BigDecimalStringConverter
import javafx.util.converter.BigIntegerStringConverter
import javafx.util.converter.BooleanStringConverter
import javafx.util.converter.DateStringConverter
import javafx.util.converter.DoubleStringConverter
import javafx.util.converter.FloatStringConverter
import javafx.util.converter.IntegerStringConverter
import javafx.util.converter.LocalDateStringConverter
import javafx.util.converter.LocalDateTimeStringConverter
import javafx.util.converter.LocalTimeStringConverter
import javafx.util.converter.LongStringConverter
import javafx.util.converter.NumberStringConverter
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lib.onChange
import java.math.BigDecimal
import java.math.BigInteger
import java.text.Format
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import java.util.concurrent.Callable

fun <T> ObservableObjectValue<T>.boundList(
  op: (T)->Iterable<T>
): Pair<ObservableList<T>, ChangeListener<in T>> {
  val list = op(get()).toList().toObservable()
  val listenerRef = ChangeListener<T> { _, _, n ->
	list.setAll(op(n).toList())
  }
  addListener(WeakChangeListener(listenerRef))
  return list to listenerRef
}

fun ObservableNumberValue.boundList(
  op: (Double)->Iterable<Number>
): Pair<ObservableList<Double>, ChangeListener<in Number>> {
  val list = op(doubleValue()).map { it.toDouble() }.toList().toObservable()
  val listenerRef = ChangeListener<Number> { _, _, n ->
	list.setAll(op(n.toDouble()).map { it.toDouble() }.toList())
  }
  addListener(WeakChangeListener(listenerRef))
  return list to listenerRef
}


//fun BooleanExpression.isFalse() = this.eq(false)
//fun BooleanExpression.isTrue() = this.eq(true)
//fun BooleanProperty.isFalse() = this.eq(false)
//fun BooleanProperty.isTrue() = this.eq(true)


fun <T> Property<T>.internalBind(property: ObservableValue<T>, readonly: Boolean) {
  if (readonly || (property !is Property<*>)) bind(property) else bindBidirectional(property as Property<T>)
}





inline fun <reified S : T, reified T : Any> bindStringProperty(
  stringProperty: StringProperty,
  converter: StringConverter<T>?,
  format: Format?,
  property: ObservableValue<S>,
  readonly: Boolean
) {
  if (stringProperty.isBound) stringProperty.unbind()
  val effectiveReadonly = readonly || property !is Property<S> || S::class != T::class

  if (S::class == String::class) when {
    effectiveReadonly -> stringProperty.bind(property as ObservableValue<String>)
    else -> stringProperty.bindBidirectional(property as Property<String>)
  } else {
    val effectiveConverter = if (format != null) null else converter ?: getDefaultConverter<S>()
    if (effectiveReadonly) {
      val toStringConverter = Callable {
        when {
          converter != null -> converter.toString(property.value)
          format != null -> format.format(property.value)
          else -> property.value?.toString()
        }
      }
      val stringBinding = Bindings.createStringBinding(toStringConverter, property)
      stringProperty.bind(stringBinding)
    } else when {
      effectiveConverter != null -> stringProperty.bindBidirectional(property as Property<S>, effectiveConverter as StringConverter<S>)
      format != null -> stringProperty.bindBidirectional(property as Property<S>, format)
      else -> throw IllegalArgumentException("Cannot convert from ${S::class} to String without an explicit converter or format")
    }
  }
}

inline fun <reified T : Any> getDefaultConverter() = when (T::class.javaPrimitiveType ?: T::class) {
  Int::class.javaPrimitiveType -> IntegerStringConverter()
  Long::class.javaPrimitiveType -> LongStringConverter()
  Double::class.javaPrimitiveType -> DoubleStringConverter()
  Float::class.javaPrimitiveType   -> FloatStringConverter()
  Date::class                      -> DateStringConverter()
  BigDecimal::class                -> BigDecimalStringConverter()
  BigInteger::class                -> BigIntegerStringConverter()
  Number::class                    -> NumberStringConverter()
  LocalDate::class                 -> LocalDateStringConverter()
  LocalTime::class                 -> LocalTimeStringConverter()
  LocalDateTime::class             -> LocalDateTimeStringConverter()
  Boolean::class.javaPrimitiveType -> BooleanStringConverter()
  else                             -> null
} as StringConverter<T>?

fun ObservableValue<Boolean>.toBinding() = object : BooleanBinding() {
  init {
    super.bind(this@toBinding)
  }

  override fun dispose() {
    super.unbind(this@toBinding)
  }

  override fun computeValue() = this@toBinding.value

  override fun getDependencies(): ObservableList<*> = FXCollections.singletonObservableList(this@toBinding)
}

fun <T, N> ObservableValue<T>.select(nested: (T) -> ObservableValue<N>): Property<N> {
  fun extractNested(): ObservableValue<N>? = value?.let(nested)

  var currentNested: ObservableValue<N>? = extractNested()

  return object : SimpleObjectProperty<N>() {
    val changeListener = ChangeListener<Any?> { _, _, _ ->
      invalidated()
      fireValueChangedEvent()
    }

    init {
      currentNested?.addListener(changeListener)
      this@select.addListener(changeListener)
    }

    override fun invalidated() {
      currentNested?.removeListener(changeListener)
      currentNested = extractNested()
      currentNested?.addListener(changeListener)
    }

    override fun get() = currentNested?.value

    override fun set(v: N?) {
      (currentNested as? WritableValue<N>)?.value = v
      super.set(v)
    }

  }

}

fun <T> ObservableValue<T>.selectBoolean(nested: (T) -> BooleanExpression): BooleanExpression {
  fun extractNested() = nested(value)

  val dis = this
  var currentNested = extractNested()

  return object : SimpleBooleanProperty() {
    val changeListener = ChangeListener<Boolean> { _, _, _ ->
      currentNested = extractNested()
      fireValueChangedEvent()
    }

    init {
      dis.onChange {
        fireValueChangedEvent()
        invalidated()
      }
    }

    override fun invalidated() {
      currentNested.removeListener(changeListener)
      currentNested = extractNested()
      currentNested.addListener(changeListener)
    }

    override fun getValue() = currentNested.value

    override fun setValue(v: Boolean?) {
      (currentNested as? WritableValue<*>)?.value = v
      super.setValue(v)
    }

  }

}
