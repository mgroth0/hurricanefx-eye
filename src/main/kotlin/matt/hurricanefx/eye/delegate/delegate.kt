@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package matt.hurricanefx.eye.delegate

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lang.listen
import matt.hurricanefx.eye.lib.onChange
import matt.json.custom.Json
import matt.json.custom.JsonModel
import matt.kjlib.delegate.SuperDelegate
import matt.kjlib.delegate.SuperListDelegate
import matt.kjlib.delegate.SuperSetDelegate
import matt.klib.lang.err
import matt.klib.lang.go
import matt.klib.lang.setAll
import matt.klib.lang.whileTrue
import java.util.WeakHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class FXDelegateBase {
  companion object {
	val instances = WeakHashMap<Any, MutableMap<String, FXDelegateBase>>()
  }

  fun initialize(
	thisRef: Any, name: String
  ) {
	val thisRefMap = instances[thisRef] ?: mutableMapOf<String, FXDelegateBase>().also { instances[thisRef] = it }
	thisRefMap[name] = this
  }

  abstract fun onChange(op: ()->Unit)
}

class FXB(
  default: Boolean? = null, bind: KProperty<*>? = null
): FX<Boolean, BooleanProperty>(default, bind) {
  val boolProp by lazy { SimpleBooleanProperty().apply { bindBidirectional(fxProp) } }
  override operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): BooleanProperty {
	return boolProp
  }
}

class FXI(
  default: Int? = null, bind: KProperty<*>? = null
): FX<Number, IntegerProperty>(default, bind) {
  val intProp by lazy { SimpleIntegerProperty().apply { bindBidirectional(fxProp) } }
  override operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): IntegerProperty {
	return intProp
  }
}

class FXS(
  default: String? = null, bind: KProperty<*>? = null
): FX<String, StringProperty>(default, bind) {
  val stringProp by lazy { SimpleStringProperty().apply { bindBidirectional(fxProp) } }
  override operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): StringProperty {
	return stringProp
  }
}

class FXL(
  default: Long? = null, bind: KProperty<*>? = null
): FX<Number, LongProperty>(default, bind) {
  val lProp by lazy { SimpleLongProperty().apply { bindBidirectional(fxProp) } }
  override operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): SimpleLongProperty {
	return lProp
  }
}

class FXD(
  default: Double? = null, bind: KProperty<*>? = null
): FX<Number, DoubleProperty>(default, bind) {
  val doubleProp by lazy { SimpleDoubleProperty().apply { bindBidirectional(fxProp) } }
  override operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): DoubleProperty {
	return doubleProp
  }
}

class FXO<V>(
  default: V? = null, bind: KProperty<*>? = null
): FX<V, ObjectProperty<V>>(default, bind) {
  override operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): ObjectProperty<V> {
	return fxProp
  }
}

class FXE<V: Enum<V>>(
  default: V? = null, bind: KProperty<*>? = null
): FX<V, ObjectProperty<V>>(default, bind) {
  override operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): ObjectProperty<V> {
	return fxProp
  }
}

abstract class FX<V, P: ObservableValue<V>>(
  default: V? = null, val bind: KProperty<*>? = null
): FXDelegateBase() {

  lateinit var thisRefVar: Any
  lateinit var propVar: KProperty<*>

  val fxProp by lazy {
	initialize(thisRefVar, propVar.name)
	SimpleObjectProperty<V>(default).apply {
	  bindToJsonProp(o = thisRefVar, prop = bind?.name ?: propVar.name)
	}
  }

  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FX<V, P> {
	thisRefVar = thisRef
	propVar = prop
	return this
  }

  abstract operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): P

  override fun onChange(op: ()->Unit) {
	fxProp.onChange {
	  op()
	}
  }
}


fun <V> SimpleObjectProperty<V>.bindToJsonProp(o: Any, prop: String) {
  ((o as? Json<*>)?.json as? JsonModel)?.props?.firstOrNull { it.key == prop }?.d?.go { d ->
	@Suppress("UNCHECKED_CAST") val setFun = d.setfun as ((V)->V)?

	@Suppress("UNCHECKED_CAST") val getFun = d.getfun as ((V)->V)?
	require(getFun == null) {
	  "need more dev. getFun currently doesnt work in fx prop. would need a lot more work, powerful property delegates"
	}
	require(d is SuperDelegate<*, *>)
	if (d.wasSet) {
	  @Suppress("UNCHECKED_CAST") set(d.get() as V?)
	}
	var sending = false
	onChange {
	  sending = true
	  d.set(it)
	  sending = false
	  if (setFun != null) {
		require(it != null) {
		  "need more dev to specify which props are nullable (I think I did the json side but not yet the FX side)"
		}
		@Suppress("UNCHECKED_CAST") val s = setFun(it as V)
		if (s != it) {
		  set(s)
		}
	  }
	}
	d.onChange {
	  if (!sending) {
		@Suppress("UNCHECKED_CAST") if (get() != it as V?) { /*might have reloaded json*/
		  @Suppress("UNCHECKED_CAST") set(it as V?)
		}
	  }
	}
  }
}


class FXList<V>(
  vararg default: V, val bind: KProperty<*>? = null
): FXDelegateBase() {
  private val fxProp = default.toList().toObservable()
  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FXList<V> {
	initialize(thisRef, prop.name)
	val search = bind?.name ?: prop.name
	((thisRef as? Json<*>)?.json as? JsonModel)?.props?.firstOrNull { it.key == search }?.d?.go { d ->
	  require(
		d.setfun == null && d.getfun == null
	  ) { "would need more dev and to specify if I'm setting the elements or the list" }
	  require(d is SuperListDelegate<*, *>)
	  if (d.wasSet) {
		@Suppress("UNCHECKED_CAST") fxProp.setAll(d.get() as List<V>)
	  }
	  var sending = false
	  fxProp.onChange {
		sending = true
		d.setAll(fxProp.toList())
		sending = false
	  }
	  d.onChange {
		require(it is List<*>)
		if (!sending) {
		  @Suppress("UNCHECKED_CAST") fxProp.setAll(it as List<V>)
		}
	  }
	}
	return this
  }

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): ObservableList<V> {
	return fxProp
  }

  override fun onChange(op: ()->Unit) {
	fxProp.onChange {
	  op()
	}
  }

  @Suppress("unused")
  fun afterChange(op: ()->Unit) {
	fxProp.onChange {
	  whileTrue { it.next() }
	  op()
	}
  }

  @Suppress("unused")
  fun listen(onAdd: (V)->Unit, onRemove: (V)->Unit) {
	fxProp.listen(onAdd, onRemove)
  }


}


class FXSet<V>(
  vararg default: V, val bind: KProperty<*>? = null
): FXDelegateBase() {
  private val fxProp = default.toSet().toObservable()
  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FXSet<V> {
	initialize(thisRef, prop.name)
	val search = bind?.name ?: prop.name
	((thisRef as? Json<*>)?.json as? JsonModel)?.props?.firstOrNull { it.key == search }?.d?.go { d ->
	  require(
		d.setfun == null && d.getfun == null
	  ) { "would need more dev and to specify if I'm setting the elements or the list" }
	  require(d is SuperSetDelegate<*, *>)
	  if (d.wasSet) {
		@Suppress("UNCHECKED_CAST") fxProp.setAll(d.get() as Set<V>)
	  }
	  var sending = false
	  var sendingToFxProp = false
	  fxProp.listen(onAdd = {
		if (!sendingToFxProp) {            //                        println("fxProp.onAdd1:${it}")
		  sending = true
		  d.add(it)
		  sending = false
		}
	  }, onRemove = {
		if (!sendingToFxProp) {            //                        println("fxProp.onRemove1:${it}")
		  sending = true
		  d.remove(it)
		  sending = false
		}
	  })    /*fxProp.onChange {
		  sending = true
		  d.setAll(fxProp.toList())
		  sending = false
	  }*/
	  d.onChange {        //                println("d.change it=${it} sending=${sending}")
		//                if (it is Collection<*>) {
		//                    taball("d.change",it)
		//                }
		require(it is Set<*>)
		if (!sending) {
		  @Suppress("UNCHECKED_CAST")
		  sendingToFxProp = true
		  @Suppress("UNCHECKED_CAST") fxProp.setAll(it as Set<V>)
		  sendingToFxProp = false
		}
	  }

	}
	return this
  }

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): ObservableSet<V> {
	return fxProp
  }

  override fun onChange(op: ()->Unit) {
	err(
	  "onChange is broken for sets :( and no, you cant just listen. Issue is that the listener is run BEFORE the actual change which is not what is ever expected"
	)
	@Suppress("UNREACHABLE_CODE") fxProp.onChange {
	  op()

	}    /*fxProp.listen({

	  op() }, { op() })*/
  }

  @Suppress("unused")
  fun listen(onAdd: (V)->Unit, onRemove: (V)->Unit) {
	fxProp.listen(onAdd, onRemove)
  }


}


/*
abstract class SavableFX {


//  private val savableFXPropsM = mutableMapOf<String, Property<*>>()
//  val savableFXProps: Map<String, Property<*>> get() = savableFXPropsM


}
*/


/*class SavableFXSerializer<T: SavableFX>(val cls: KClass<T>): KSerializer<T> {

  override val descriptor = object: SerialDescriptor {
	@ExperimentalSerializationApi
	override val elementsCount: Int get() = TODO("Not yet implemented")

	@ExperimentalSerializationApi
	override val kind: SerialKind get() = StructureKind.MAP

	@ExperimentalSerializationApi
	override val serialName: String get() = cls.qualifiedName!!

	@ExperimentalSerializationApi
	override fun getElementAnnotations(index: Int): List<Annotation> = TODO("Not yet implemented")

	@ExperimentalSerializationApi
	override fun getElementDescriptor(index: Int): SerialDescriptor = TODO("Not yet implemented")

	@ExperimentalSerializationApi
	override fun getElementIndex(name: String): Int = TODO("Not yet implemented")

	@ExperimentalSerializationApi
	override fun getElementName(index: Int): String = TODO("Not yet implemented")

	@ExperimentalSerializationApi
	override fun isElementOptional(index: Int): Boolean = false

  }

  override fun serialize(encoder: Encoder, value: T) {
	val map = value.savableFXProps.mapValues { it.value.fxProp.get() }
	encoder.encodeSerializableValue(surrogate, map)
  }

  override fun deserialize(decoder: Decoder): T {
	val map = decoder.decodeSerializableValue(surrogate)
	val instance = cls.createInstance()
	instance.savableFXProps.forEach {
	  it.value.fxProp.set(map[it.key])
	}
	return instance
  }




}*/


val primsOnly = "primitives only for now"


class FXPropertySerializer(val cls: KClass<*>): KSerializer<Property<*>> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Property") {
	element("type", String.serializer().descriptor)
	element("value", cls.serialDescriptor())
  }


  @Suppress("OPT_IN_USAGE")
  override fun serialize(encoder: Encoder, value: Property<*>) {
	val v = value.value!!
	val type = v::class.qualifiedName!!
	encoder.encodeStructure(descriptor) {
	  encodeStringElement(String.serializer().descriptor, 0, type)
	  val d = descriptor.elementDescriptors.toList()[1]
	  when (cls) {
		Boolean::class -> encodeBooleanElement(d, 1, v as Boolean)
		Char::class    -> encodeCharElement(d, 1, v as Char)
		String::class  -> encodeStringElement(d, 1, v as String)
		Int::class     -> encodeIntElement(d, 1, v as Int)
		Long::class    -> encodeLongElement(d, 1, v as Long)
		Float::class   -> encodeFloatElement(d, 1, v as Float)
		Double::class  -> encodeDoubleElement(
		  d, 1, v as Double
		)        /*TODO: List::class -> SimpleListProperty<Any>() as Property<T>*/
		else           -> err(primsOnly)
	  }
	}
  }

  @Suppress("OPT_IN_USAGE")
  override fun deserialize(decoder: Decoder): Property<*> {

	var type: String? = null
	var value: java.io.Serializable? = null /*idk*/

	decoder.decodeStructure(descriptor) {
	  type = decodeStringElement(String.serializer().descriptor, 0)
	  val d = descriptor.elementDescriptors.toList()[1]
	  value = when (type) {
		Boolean::class.qualifiedName -> decodeBooleanElement(d, 1)
		Char::class.qualifiedName    -> decodeCharElement(d, 1)
		String::class.qualifiedName  -> decodeStringElement(d, 1)
		Int::class.qualifiedName     -> decodeIntElement(d, 1)
		Long::class.qualifiedName    -> decodeLongElement(d, 1)
		Float::class.qualifiedName   -> decodeFloatElement(d, 1)
		Double::class.qualifiedName  -> decodeDoubleElement(
		  d, 1
		)        /*TODO: List::class -> SimpleListProperty<Any>() as Property<T>*/
		else                         -> err(primsOnly)
	  }
	}

	return createFxPropFromPrimQClassName(type!!).also {
	  it.value = value!!
	}

  }
}

@Suppress("UNCHECKED_CAST")
fun KClass<*>.createFxProp(): Property<*> = when (this) {
  Boolean::class -> SimpleBooleanProperty()
  String::class  -> SimpleStringProperty()
  Int::class     -> SimpleIntegerProperty()
  Long::class    -> SimpleLongProperty()
  Float::class   -> SimpleFloatProperty()
  Double::class  -> SimpleDoubleProperty()/*TODO: List::class -> SimpleListProperty<Any>() as Property<T>*/
  else           -> SimpleObjectProperty<Any>()
}


@Suppress("UNCHECKED_CAST")
fun KClass<*>.serialDescriptor(): SerialDescriptor = when (this) {
  Boolean::class -> Boolean.serializer().descriptor
  Char::class    -> Char.serializer().descriptor
  String::class  -> String.serializer().descriptor
  Int::class     -> Int.serializer().descriptor
  Long::class    -> Long.serializer().descriptor
  Float::class   -> Float.serializer().descriptor
  Double::class  -> Double.serializer().descriptor/*TODO: List::class -> SimpleListProperty<Any>() as Property<T>*/
  else           -> err("todo: Any.serializer().descriptor")
}

fun createFxPropFromPrimQClassName(qname: String) = when (qname) {
  Boolean::class.qualifiedName -> SimpleBooleanProperty()
  String::class.qualifiedName  -> SimpleStringProperty()
  Int::class.qualifiedName     -> SimpleIntegerProperty()
  Long::class.qualifiedName    -> SimpleLongProperty()
  Float::class.qualifiedName   -> SimpleFloatProperty()
  Double::class.qualifiedName  -> SimpleDoubleProperty()/*TODO: List::class -> SimpleListProperty<Any>() as Property<T>*/
  else                         -> err(primsOnly)
}

