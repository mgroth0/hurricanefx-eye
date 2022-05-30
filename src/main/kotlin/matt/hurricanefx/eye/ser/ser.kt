package matt.hurricanefx.eye.ser

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

//inline fun <reified T: Any> fx(default: T? = null, autosave: Boolean = false) =
//  FXPropProvider(default, T::class, autosave = autosave)
//
//inline fun <reified T: Any> autoFX(default: T? = null) = FXPropProvider(default, T::class, autosave = true)

//val blockAutoSavingOfThese = WeakBag<Any>()

//class FXPropProvider<T: Any>(val default: T?, private val vCls: KClass<T>, val autosave: Boolean = false) {
//  operator fun provideDelegate(
//	thisRef: Any, prop: KProperty<*>
//  ): FXProp<T> = FXProp(
//	default, vCls = vCls,
//	autosave = autosave, thisRef
//  )
//}

//interface SavableObj {
//  fun save()
//}

//class FXProp<V>(
//  default: V?, vCls: KClass<*>,
//  autosave: Boolean, thisRef: Any
//) {
//  @Suppress("UNCHECKED_CAST") val fxProp: Property<V> = (vCls.createFxProp() as Property<V>).also {
//	if (default != null) it.value = default
//  }
//
//  operator fun getValue(
//	thisRef: Any?,
//	property: KProperty<*>,
//  ): V = fxProp.value
//
//  operator fun setValue(
//	thisRef: Any?, property: KProperty<*>, value: V
//  ) {
//	fxProp.value = value
//  }
//
//}




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

abstract class JsonObjectSerializer<T>(qname: String): JsonSerializer<T>(qname) {
  override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonObject)
  abstract fun deserialize(jsonObject: JsonObject): T
  abstract override fun serialize(value: T): JsonObject
}

abstract class JsonArraySerializer<T>(qname: String): JsonSerializer<T>(qname) {
  override fun deserialize(jsonElement: JsonElement): T = deserialize(jsonElement.jsonArray)
  abstract fun deserialize(jsonArray: JsonArray): T
  abstract override fun serialize(value: T): JsonArray
}


