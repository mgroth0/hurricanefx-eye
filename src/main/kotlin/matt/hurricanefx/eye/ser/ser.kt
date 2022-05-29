package matt.hurricanefx.eye.ser

import javafx.beans.property.Property
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import matt.hurricanefx.eye.delegate.createFxProp
import matt.hurricanefx.eye.lib.onActualChange
import matt.kjlib.map.lazyMap
import matt.kjlib.weak.bag.WeakBag
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

inline fun <reified T: Any> fx(default: T? = null, autosave: Boolean = false) =
  FXPropProvider(default, T::class, autosave = autosave)

inline fun <reified T: Any> autoFX(default: T? = null) = FXPropProvider(default, T::class, autosave = true)

val blockAutoSavingOfThese = WeakBag<Any>()

class FXPropProvider<T: Any>(val default: T?, val vCls: KClass<T>, val autosave: Boolean = false) {
  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FXProp<T> = FXProp(
	default, vCls = vCls,
	autosave = autosave, thisRef
  )
}

interface SavableObj {
  fun save()
}

val serialProps = lazyMap<KClass<*>, MutableSet<String>> {
  mutableSetOf<String>()
}

class FXProp<V>(
  default: V?, vCls: KClass<*>,
  autosave: Boolean, thisRef: Any
) {
  @Suppress("UNCHECKED_CAST") val fxProp: Property<V> = (vCls.createFxProp() as Property<V>).also {
	if (default != null) it.value = default
	if (autosave) it.onActualChange {
	  if (thisRef !in blockAutoSavingOfThese) (thisRef as SavableObj).save()
	}
  }

  operator fun getValue(
	thisRef: Any?,
	property: KProperty<*>,
  ): V = fxProp.value

  operator fun setValue(
	thisRef: Any?, property: KProperty<*>, value: V
  ) {
	fxProp.value = value
  }

}

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.fx
  get() = (this.apply {
	/*	*
		* Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible
		*
		* */
	isAccessible = true
  }.getDelegate() as FXProp<V>).fxProp


@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.fx(t: T) = (this.apply {
  /*  *
	* Caused by: kotlin.reflect.full.IllegalPropertyDelegateAccessException: Cannot obtain the delegate of a non-accessible property. Use "isAccessible = true" to make the property accessible
	*
	* */
  isAccessible = true
}.getDelegate(t) as FXProp<V>).fxProp


abstract class JsonSerializer<T>(qname: String): KSerializer<T> {
  final override val descriptor = buildClassSerialDescriptor(qname)

  final override fun deserialize(decoder: Decoder): T {
	return deserialize(jsonElement = (decoder as JsonDecoder).decodeJsonElement())
  }

  final override fun serialize(encoder: Encoder, value: T) {
	(encoder as JsonEncoder).encodeJsonElement(serialize(value))
  }

  abstract fun deserialize(jsonElement: JsonElement): T

  abstract fun serialize(value: T): JsonElement

}

