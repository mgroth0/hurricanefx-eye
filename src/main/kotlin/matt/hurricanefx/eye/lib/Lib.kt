package matt.hurricanefx.eye.lib

/*slightly modified code I stole from tornadofx*/

import javafx.beans.property.*
import javafx.beans.value.*
import javafx.collections.*

@Deprecated("Use `asObservable()` instead.", ReplaceWith("this.asObservable()", "tornadofx.asObservable"))
fun <T> List<T>.observable(): ObservableList<T> = FXCollections.observableList(this)

@Deprecated("Use `asObservable()` instead.", ReplaceWith("this.asObservable()", "tornadofx.asObservable"))
fun <T> Set<T>.observable(): ObservableSet<T> = FXCollections.observableSet(this)

@Deprecated("Use `asObservable()` instead.", ReplaceWith("this.asObservable()", "tornadofx.asObservable"))
fun <K, V> Map<K, V>.observable(): ObservableMap<K, V> = FXCollections.observableMap(this)


inline fun <T> ChangeListener(crossinline listener: (observable: ObservableValue<out T>?, oldValue: T, newValue: T)->Unit): ChangeListener<T> =
	javafx.beans.value.ChangeListener<T> { observable, oldValue, newValue -> listener(observable, oldValue, newValue) }

/**
 * Listen for changes to this observable. Optionally only listen x times.
 * The lambda receives the changed value when the change occurs, which may be null,
 */
fun <T> ObservableValue<T>.onChangeTimes(times: Int, op: (T?)->Unit) {
  var counter = 0
  val listener = object: ChangeListener<T> {
	override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
	  if (++counter == times) {
		removeListener(this)
	  }
	  op(newValue)
	}
  }
  addListener(listener)
}

/*matt was here*/
fun <T> ObservableValue<T>.onChangeUntilAfterFirst(untilThis: T, op: (T?)->Unit) {
  val listener = object: ChangeListener<T> {
	override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
	  if (newValue == untilThis) {
		removeListener(this)
	  }
	  op(newValue)
	}
  }
  addListener(listener)
}

/*matt was here*/
fun <T> ObservableValue<T>.onFirstNonNullChange(op: (T)->Unit) {
  val listener = object: ChangeListener<T> {
	override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
	  if (newValue != null) {
		removeListener(this)
		op(newValue)
	  }

	}
  }
  addListener(listener)
}

/*matt was here*/
fun <T> ObservableValue<T>.whenNotNull(op: (T)->Unit) {
  if (value != null) op(value)
  else onFirstNonNullChange(op)
}


fun <T> ObservableValue<T>.onChangeOnce(op: (T?)->Unit) = onChangeTimes(1, op)

fun <T> ObservableValue<T>.onChange(op: (T?)->Unit) = apply { addListener { _, _, newValue -> op(newValue) } }
fun <T> ObservableValue<T>.onNonNullChange(op: (T)->Unit) = apply {
  addListener { _, _, newValue -> if (newValue != null) op(newValue) }
}


fun ObservableBooleanValue.onChange(op: (Boolean)->Unit) = apply { addListener { _, _, new -> op(new ?: false) } }
fun ObservableIntegerValue.onChange(op: (Int)->Unit) = apply { addListener { _, _, new -> op((new ?: 0).toInt()) } }
fun ObservableLongValue.onChange(op: (Long)->Unit) = apply { addListener { _, _, new -> op((new ?: 0L).toLong()) } }
fun ObservableFloatValue.onChange(op: (Float)->Unit) = apply {
  addListener { _, _, new ->
	op((new ?: 0f).toFloat())
  }
}

fun ObservableDoubleValue.onChange(op: (Double)->Unit) = apply {
  addListener { _, _, new ->
	op((new ?: 0.0).toDouble())
  }
}

fun <T> ObservableList<T>.onChange(op: (ListChangeListener.Change<out T>)->Unit) = apply {
  addListener(ListChangeListener { op(it) })
}

/**
 * Create a proxy property backed by calculated data based on a specific property. The setter
 * must return the new value for the backed property.
 * The scope of the getter and setter will be the receiver property
 */
fun <R, T> proxyprop(receiver: Property<R>, getter: Property<R>.()->T, setter: Property<R>.(T)->R): ObjectProperty<T> =
	object: SimpleObjectProperty<T>() {
	  init {
		receiver.onChange {
		  fireValueChangedEvent()
		}
	  }

	  override fun invalidated() {
		receiver.value = setter(receiver, super.get())
	  }

	  override fun get() = getter.invoke(receiver)
	  override fun set(v: T) {
		receiver.value = setter(receiver, v)
		super.set(v)
	  }
	}

/**
 * Create a proxy double property backed by calculated data based on a specific property. The setter
 * must return the new value for the backed property.
 * The scope of the getter and setter will be the receiver property
 */
fun <R> proxypropDouble(
  receiver: Property<R>,
  getter: Property<R>.()->Double,
  setter: Property<R>.(Double)->R
): DoubleProperty = object: SimpleDoubleProperty() {
  init {
	receiver.onChange {
	  fireValueChangedEvent()
	}
  }

  override fun invalidated() {
	receiver.value = setter(receiver, super.get())
  }

  override fun get() = getter.invoke(receiver)
  override fun set(v: Double) {
	receiver.value = setter(receiver, v)
	super.set(v)
  }
}




