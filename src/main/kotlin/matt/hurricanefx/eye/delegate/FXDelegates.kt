package matt.hurricanefx.eye.delegate

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lib.onChange
import matt.kjlib.delegate.SuperDelegate
import matt.kjlib.delegate.SuperDelegateBase
import matt.kjlib.delegate.SuperListDelegate
import matt.klibexport.klibexport.go
import java.util.WeakHashMap
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KProperty

abstract class FXDelegateBase {
  companion object {
	val instances = WeakHashMap<Any, MutableMap<String, FXDelegateBase>>()
  }

  fun initialize(
	thisRef: Any,
	name: String
  ) {
	val thisRefMap = instances[thisRef]
					 ?: mutableMapOf<String, FXDelegateBase>()
						 .also { instances[thisRef] = it }
	thisRefMap[name] = this
  }

  abstract fun onChange(op: ()->Unit)
}

@ExperimentalContracts
class FXB(
  default: Boolean? = null,
  bind: KProperty<*>? = null
): FX<Boolean, BooleanProperty>(default, bind) {
  val boolProp = SimpleBooleanProperty().apply { bindBidirectional(fxProp) }
  override operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): BooleanProperty {
	return boolProp
  }
}

@ExperimentalContracts
class FXI(
  default: Int? = null,
  bind: KProperty<*>? = null
): FX<Number, IntegerProperty>(default, bind) {
  val intProp = SimpleIntegerProperty().apply { bindBidirectional(fxProp) }
  override operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): IntegerProperty {
	return intProp
  }
}

@ExperimentalContracts
class FXS(
  default: String? = null,
  bind: KProperty<*>? = null
): FX<String, StringProperty>(default, bind) {
  val stringProp = SimpleStringProperty().apply { bindBidirectional(fxProp) }
  override operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): StringProperty {
	return stringProp
  }
}

@ExperimentalContracts
class FXL(
  default: Long? = null,
  bind: KProperty<*>? = null
): FX<Number, LongProperty>(default, bind) {
  val lProp = SimpleLongProperty().apply { bindBidirectional(fxProp) }
  override operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): SimpleLongProperty {
	return lProp
  }
}

@ExperimentalContracts
class FXD(
  default: Double? = null,
  bind: KProperty<*>? = null
): FX<Number, DoubleProperty>(default, bind) {
  val doubleProp = SimpleDoubleProperty().apply { bindBidirectional(fxProp) }
  override operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): DoubleProperty {
	return doubleProp
  }
}

@ExperimentalContracts
class FXO<V>(
  default: V? = null,
  bind: KProperty<*>? = null
): FX<V, ObjectProperty<V>>(default, bind) {
  override operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): ObjectProperty<V> {
	return fxProp
  }
}

@ExperimentalContracts
class FXE<V: Enum<V>>(
  default: V? = null,
  bind: KProperty<*>? = null
): FX<V, ObjectProperty<V>>(default, bind) {
  override operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): ObjectProperty<V> {
	return fxProp
  }
}

@ExperimentalContracts
abstract class FX<V, P: ObservableValue<V>>(
  default: V? = null,
  val bind: KProperty<*>? = null
): FXDelegateBase() {
  val fxProp = SimpleObjectProperty<V>(default)
  operator fun provideDelegate(
	thisRef: Any,
	prop: KProperty<*>
  ): FX<V, P> {
	initialize(thisRef, prop.name)
	val search = bind?.name ?: prop.name
	SuperDelegateBase.instances[thisRef]?.get(search)?.go { d ->
	  @Suppress("UNCHECKED_CAST")
	  val setfun = d.setfun as ((V)->V)?

	  @Suppress("UNCHECKED_CAST")
	  val getfun = d.getfun as ((V)->V)?
	  require(getfun == null) {
		"need more dev. getfun currently doesnt work in fx prop. would need a lot more work, powerful property delegates"
	  }
	  require(d is SuperDelegate<*, *>)
	  if (d.was_set) {
		@Suppress("UNCHECKED_CAST")
		fxProp.set(d.get() as V?)
	  }
	  var sending = false
	  fxProp.onChange {
		sending = true
		d.set(it)
		sending = false
		if (setfun != null) {
		  require(it != null) {
			"need more dev to specify which props are nullable (I think I did the json side but not yet the FX side)"
		  }
		  @Suppress("UNCHECKED_CAST")
		  val s = setfun(it as V)
		  if (s != it) {
			fxProp.set(s)
		  }
		}
	  }
	  d.onChange {
		if (!sending) {
		  @Suppress("UNCHECKED_CAST")
		  if (fxProp.get() != it as V?) { /*might have reloaded json*/
			@Suppress("UNCHECKED_CAST")
			fxProp.set(it as V?)
		  }
		}
	  }
	}
	return this
  }

  abstract operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): P

  override fun onChange(op: ()->Unit) {
	fxProp.onChange {
	  op()
	}
  }
}

@ExperimentalContracts
class FXList<V>(
  vararg default: V,
  val bind: KProperty<*>? = null
): FXDelegateBase() {
  private val fxProp = default.toList().toObservable()
  operator fun provideDelegate(
	thisRef: Any,
	prop: KProperty<*>
  ): FXList<V> {
	initialize(thisRef, prop.name)
	val search = bind?.name ?: prop.name
	SuperDelegateBase.instances[thisRef]?.get(search)?.go { d ->
	  require(d.setfun == null) { "would need more dev and to specify if I'm setting the elements or the list" }
	  require(d.getfun == null) { "would need more dev and to specify if I'm setting the elements or the list" }
	  require(d is SuperListDelegate<*, *>)
	  if (d.was_set) {
		@Suppress("UNCHECKED_CAST")
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
		  @Suppress("UNCHECKED_CAST")
		  fxProp.setAll(it as List<V>)
		}
	  }
	}
	return this
  }

  operator fun getValue(
	thisRef: Any,
	property: KProperty<*>
  ): ObservableList<V> {
	return fxProp
  }

  override fun onChange(op: ()->Unit) {
	fxProp.onChange {
	  op()
	}
  }

}