@file:Suppress("UNCHECKED_CAST")

package matt.hurricanefx.eye.delegate

import javafx.beans.Observable
import javafx.beans.binding.BooleanExpression
import javafx.beans.binding.NumberExpression
import javafx.beans.binding.StringExpression
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
import matt.klib.lang.B
import matt.klib.lang.D
import matt.klib.lang.I
import matt.klib.lang.L
import matt.klib.lang.S
import matt.klib.lang.err
import matt.klib.lang.whileTrue
import matt.klib.log.warn
import matt.klib.log.warnOnce
import matt.reflect.access
import java.util.WeakHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1


val Any.fxDelegates get() = FXDelegateBase.instances[this]!!
val Any.fxBools
  get() = fxDelegates
	.filterValues { it is FX<*, *> && it.observable is BooleanProperty }
	.mapValues { (it.value as FX<*, *>).observable as BooleanProperty }
	.values

abstract class FXDelegateBase {
  companion object {
	val instances = WeakHashMap<Any, MutableMap<String, FXDelegateBase>>()
  }

  abstract val observable: Observable

  fun initialize(
	thisRef: Any, name: String
  ) {
	val thisRefMap = instances[thisRef] ?: mutableMapOf<String, FXDelegateBase>().also { instances[thisRef] = it }
	thisRefMap[name] = this
  }

  abstract fun onChange(op: ()->Unit): Any
}

fun FXB(default: B? = null, listener: ((B)->Unit)? = null) = FX<B, BooleanProperty>(default, BProp::class, listener)
fun FXI(default: I? = null, listener: ((I)->Unit)? = null) = FX<I, IntegerProperty>(default, IProp::class, listener)
fun FXS(default: S? = null, listener: ((S)->Unit)? = null) = FX<S, StringProperty>(default, SProp::class, listener)
fun FXL(default: L? = null, listener: ((L)->Unit)? = null) = FX<L, LongProperty>(default, LProp::class, listener)
fun FXD(default: D? = null, listener: ((D)->Unit)? = null) = FX<D, DoubleProperty>(default, DProp::class, listener)
fun <V: Any> FXO(default: V? = null, listener: ((V)->Unit)? = null) =
  FX<V, ObjectProperty<V>>(default, listener = listener)

fun <V: Enum<V>> FXE(default: V? = null, listener: ((V)->Unit)? = null) =
  FX<V, ObjectProperty<V>>(default, listener = listener)

/*need to use object properties here because primitive type properties are not nullable it seems*/
fun FXBN(default: B? = null, listener: ((B?)->Unit)? = null) = FX<B?, ObjectProperty<B?>>(default, listener = listener)
fun FXIN(default: I? = null, listener: ((I?)->Unit)? = null) = FX<I?, ObjectProperty<I?>>(default, listener = listener)
fun FXSN(default: S? = null, listener: ((S?)->Unit)? = null) = FX<S?, ObjectProperty<S?>>(default, listener = listener)
fun FXLN(default: L? = null, listener: ((L?)->Unit)? = null) = FX<L?, ObjectProperty<L?>>(default, listener = listener)
fun FXDN(default: D? = null, listener: ((D?)->Unit)? = null) = FX<D?, ObjectProperty<D?>>(default, listener = listener)
fun <V> FXON(default: V? = null, listener: ((V?)->Unit)? = null) =
  FX<V?, ObjectProperty<V?>>(default, listener = listener)

fun <V: Enum<V>> FXEN(default: V? = null, listener: ((V?)->Unit)? = null) =
  FX<V?, ObjectProperty<V?>>(default, listener = listener)


class FX<V, P: Property<*>> internal constructor(
  default: V? = null,
  private val propClass: KClass<out Property<*>>? = null,
  listener: ((V)->Unit)? = null
): FXDelegateBase() {

  private lateinit var thisRefVar: Any
  private lateinit var propVar: KProperty<*>

  override val observable: P by lazy {


	val prop = propClass?.let { cls ->
	  when (default) {
		null -> cls.constructors.first { it.parameters.size == 2 }.run {
		  @Suppress("UNCHECKED_CAST")
		  call(thisRefVar, propVar.name) as P
		}

		else -> cls.constructors.first { it.parameters.size == 3 }.run {
		  @Suppress("UNCHECKED_CAST")
		  call(thisRefVar, propVar.name, default) as P
		}
	  }
	} ?: run {
	  if (default == null) {
		@Suppress("UNCHECKED_CAST")
		SimpleObjectProperty<V>(thisRefVar, propVar.name) as P
	  } else {
		@Suppress("UNCHECKED_CAST")
		SimpleObjectProperty<V>(thisRefVar, propVar.name, default) as P
	  }
	}
	if (listener != null) {
	  prop.onChange {
		listener(it as V)
	  }
	}
	prop
  }

  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FX<V, P> {
	thisRefVar = thisRef
	propVar = prop
	initialize(thisRefVar, propVar.name)
	return this
  }

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ): V = observable.value as V

  operator fun setValue(
	thisRef: Any,
	property: KProperty<*>,
	value: V
  ) {
	observable.value = value
  }

  override fun onChange(op: ()->Unit) {
	observable.onChange {
	  op()
	}
  }
}


class FXList<V>(
  vararg default: V, val bind: KProperty<*>? = null, val listener: (()->Unit)? = null
): FXDelegateBase() {
  init {
	warnOnce("does this have to a by? can it not just be a regular val instead of a property delegate?")
  }

  override val observable = default.toList().toObservable()
  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FXList<V> {
	initialize(thisRef, prop.name)
	onChange {
	  listener?.invoke()
	}
	return this
  }

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ) = observable

  override fun onChange(op: ()->Unit) = observable.onChange { op() }

  fun afterChange(op: ()->Unit) = observable.onChange {
	whileTrue { it.next() }
	op()
  }

  fun listen(onAdd: (V)->Unit, onRemove: (V)->Unit) = observable.listen(onAdd, onRemove)


}


class FXSet<V>(
  vararg default: V, val bind: KProperty<*>? = null
): FXDelegateBase() {
  override val observable = default.toSet().toObservable()
  operator fun provideDelegate(
	thisRef: Any, prop: KProperty<*>
  ): FXSet<V> {
	initialize(thisRef, prop.name)
	return this
  }

  operator fun getValue(
	thisRef: Any, property: KProperty<*>
  ) = observable

  override fun onChange(op: ()->Unit) = err(
	"onChange is broken for sets :( and no, you cant just listen. Issue is that the listener is run BEFORE the actual change which is not what is ever expected"
  )

  fun listen(onAdd: (V)->Unit, onRemove: (V)->Unit) {
	observable.listen(onAdd, onRemove)
  }
}

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.fx
  get() = access {
	(getDelegate() as FX<V, Property<V>>)
  }.observable


@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.fx(t: T) = access {
  (getDelegate(t) as FX<V, Property<V>>)
}.observable


@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.num get() = fx as NumberExpression
@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.num(t: T) = fx(t) as NumberExpression

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.bool get() = fx as BooleanExpression
@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.bool(t: T) = fx(t) as BooleanExpression

@Suppress("UNCHECKED_CAST") val <V> KProperty0<V>.str get() = fx as StringExpression
@Suppress("UNCHECKED_CAST") fun <T, V> KProperty1<T, V>.str(t: T) = fx(t) as StringExpression
