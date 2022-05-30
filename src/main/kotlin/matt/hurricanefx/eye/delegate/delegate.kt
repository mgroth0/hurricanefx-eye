package matt.hurricanefx.eye.delegate

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lang.BProp
import matt.hurricanefx.eye.lang.DProp
import matt.hurricanefx.eye.lang.IProp
import matt.hurricanefx.eye.lang.LProp
import matt.hurricanefx.eye.lang.SProp
import matt.hurricanefx.eye.lang.listen
import matt.hurricanefx.eye.lib.onChange
import matt.json.custom.Json
import matt.json.custom.JsonModel
import matt.kjlib.delegate.SuperDelegate
import matt.kjlib.delegate.SuperListDelegate
import matt.kjlib.delegate.SuperSetDelegate
import matt.klib.lang.B
import matt.klib.lang.D
import matt.klib.lang.I
import matt.klib.lang.L
import matt.klib.lang.S
import matt.klib.lang.err
import matt.klib.lang.go
import matt.klib.lang.setAll
import matt.klib.lang.whileTrue
import matt.klib.log.warn
import matt.reflect.access
import java.util.WeakHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringExpression;

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

  abstract fun onChange(op: ()->Unit): Any
}

fun FXB(default: B? = null, bind: KProperty<*>? = null) = FX<B, BooleanProperty>(default, bind, BProp::class)
fun FXI(default: I? = null, bind: KProperty<*>? = null) = FX<I, IntegerProperty>(default, bind, IProp::class)
fun FXS(default: S? = null, bind: KProperty<*>? = null) = FX<S, StringProperty>(default, bind, SProp::class)
fun FXL(default: L? = null, bind: KProperty<*>? = null) = FX<L, LongProperty>(default, bind, LProp::class)
fun FXD(default: D? = null, bind: KProperty<*>? = null) = FX<D, DoubleProperty>(default, bind, DProp::class)
fun <V: Any> FXO(default: V? = null, bind: KProperty<*>? = null) = FX<V, ObjectProperty<V>>(default, bind)
fun <V: Enum<V>> FXE(default: V? = null, bind: KProperty<*>? = null) = FX<V, ObjectProperty<V>>(default, bind)

fun FXBN(default: B? = null, bind: KProperty<*>? = null) = FX<B?, BooleanProperty>(default, bind, BProp::class)
fun FXIN(default: I? = null, bind: KProperty<*>? = null) = FX<I?, IntegerProperty>(default, bind, IProp::class)
fun FXSN(default: S? = null, bind: KProperty<*>? = null) = FX<S?, StringProperty>(default, bind, SProp::class)
fun FXLN(default: L? = null, bind: KProperty<*>? = null) = FX<L?, LongProperty>(default, bind, LProp::class)
fun FXDN(default: D? = null, bind: KProperty<*>? = null) = FX<D?, DoubleProperty>(default, bind, DProp::class)
fun <V> FXON(default: V? = null, bind: KProperty<*>? = null) = FX<V?, ObjectProperty<V>>(default, bind)
fun <V: Enum<V>> FXEN(default: V? = null, bind: KProperty<*>? = null) = FX<V?, ObjectProperty<V>>(default, bind)

class FX<V, P: Property<*>> internal constructor(
  default: V? = null,
  val bind: KProperty<*>? = null,
  private val propClass: KClass<out Property<*>>? = null
): FXDelegateBase() {

  private lateinit var thisRefVar: Any
  private lateinit var propVar: KProperty<*>

  val fxProp: P by lazy {
	initialize(thisRefVar, propVar.name)

	val prop = propClass?.let { cls ->
	  when (default) {
		null -> cls.constructors.first { it.parameters.isEmpty() }.run {
		  println("calling ${this} with no params")
		  call() as P
		}
		else -> cls.constructors.first { it.parameters.size == 1 }.run {
		  println("calling ${this} with ${default}")
		  call(default) as P
		}
	  }
	} ?: run {
	  if (default == null) {
		println("creating SimpleObjectProperty with no args")
		SimpleObjectProperty<V>() as P
	  } else {
		println("creating SimpleObjectProperty with $default")
		SimpleObjectProperty<V>(default) as P
	  }
	}
	if (bind != null) {
	  err("no")
	}
	prop.apply {
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

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): V = fxProp.value as V

  operator fun setValue(
	thisRef: Any,
	property: KProperty<*>,
	value: V
  ) {
	fxProp.value = value
  }

  override fun onChange(op: ()->Unit) {
	fxProp.onChange {
	  op()
	}
  }
}


fun <V> Property<V>.bindToJsonProp(o: Any, prop: String) {
  ((o as? Json<*>)?.json as? JsonModel)?.props?.firstOrNull { it.key == prop }?.d?.go { d ->
	val setFun = d.setfun as ((V)->V)?
	val getFun = d.getfun as ((V)->V)?
	require(getFun == null) {
	  "need more dev. getFun currently doesnt work in fx prop. would need a lot more work, powerful property delegates"
	}
	require(d is SuperDelegate<*, *>)
	if (d.wasSet) value = d.get() as V?
	var sending = false
	onChange {
	  sending = true
	  d.set(it)
	  sending = false
	  if (setFun != null) {
		require(it != null) {
		  "need more dev to specify which props are nullable (I think I did the json side but not yet the FX side)"
		}
		val s = setFun(it as V)
		if (s != it) value = s
	  }
	}
	d.onChange {
	  if (!sending && value != it as V?) value = it /*might have reloaded json*/
	}
  }
}


class FXList<V>(
  vararg default: V, val bind: KProperty<*>? = null
): FXDelegateBase() {
  init {
	warn("does this have to a by? can it not just be a regular val instead of a property delegate?")
  }

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
		fxProp.setAll(d.get() as List<V>)
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
		  fxProp.setAll(it as List<V>)
		}
	  }
	}
	return this
  }

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ) = fxProp

  override fun onChange(op: ()->Unit) = fxProp.onChange { op() }

  fun afterChange(op: ()->Unit) = fxProp.onChange {
	whileTrue { it.next() }
	op()
  }

  fun listen(onAdd: (V)->Unit, onRemove: (V)->Unit) = fxProp.listen(onAdd, onRemove)


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
		fxProp.setAll(d.get() as Set<V>)
	  }
	  var sending = false
	  var sendingToFxProp = false
	  fxProp.listen(onAdd = {
		if (!sendingToFxProp) {
		  sending = true
		  d.add(it)
		  sending = false
		}
	  }, onRemove = {
		if (!sendingToFxProp) {
		  sending = true
		  d.remove(it)
		  sending = false
		}
	  })
	  d.onChange {
		require(it is Set<*>)
		if (!sending) {
		  sendingToFxProp = true
		  fxProp.setAll(it as Set<V>)
		  sendingToFxProp = false
		}
	  }

	}
	return this
  }

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ) = fxProp

  override fun onChange(op: ()->Unit) = err(
	"onChange is broken for sets :( and no, you cant just listen. Issue is that the listener is run BEFORE the actual change which is not what is ever expected"
  )

  fun listen(onAdd: (V)->Unit, onRemove: (V)->Unit) {
	fxProp.listen(onAdd, onRemove)
  }
}

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.fx
  get() = access {
	(getDelegate() as FX<V, Property<V>>)
  }.fxProp


@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.fx(t: T) = access {
  (getDelegate(t) as FX<V, Property<V>>)
}.fxProp


@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.num get() = fx as NumberExpression
@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.num(t: T) = fx(t) as NumberExpression

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.bool get() = fx as BooleanExpression
@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.bool(t: T) = fx(t) as BooleanExpression

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.str get() = fx as StringExpression
@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.str(t: T) = fx(t) as StringExpression
