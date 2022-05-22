package matt.hurricanefx.eye.lib

/*slightly modified code I stole from tornadofx*/

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableFloatValue
import javafx.beans.value.ObservableIntegerValue
import javafx.beans.value.ObservableLongValue
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import matt.kjlib.str.taball
import kotlin.system.exitProcess

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
fun <T: Any> ObservableValue<T?>.onNonNullChange(op: (T)->Unit) = apply {
  addListener { _, _, newValue -> if (newValue != null) op(newValue) }
}


/*MATT: I HAD THEM ALL AS NON-NULL FOR A LONG TIME. BUT THEN EVENTUALLY I GOT THIS WEIRD ANNOYING EXCEPTION DEEP IN AN FX THREAD WHERE A FOCUSED PROPERTY CHANGE WAS CAUSING A NULL POINTER EXCEPTION. I AM A STRONG HUBCH IT IS BECAUSE KOTLIN ENFORCED IT AS BEING NON NULL HERE*/
/*POSSIBLE SOLUTION: MAKE THIS ALL NULLABLE BUT ADD A LINE OF CODE THROWING AN EXCEPTION HERE IF IT TURNS OUT TO BE NULL. THAT WAY AN EXCEPTION IS THROWN IN MY OWN CODE.*/
fun ObservableBooleanValue.onChange(op: (Boolean?)->Unit) = apply { addListener { _, _, new -> op(new ?: false) } }

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

fun <T> ObservableSet<T>.onChange(op: (SetChangeListener.Change<out T>)->Unit) = apply {
	addListener(SetChangeListener { op(it) })
}

fun <T> ObservableList<T>.onChangeSafe(
  debug: Boolean = false,
  op: ()->Unit
) = apply {
  onChange {
	/*"ListChangeListener.Change requires you to call next() before the other methods"*/
	while (it.next()) {
	  if (debug) {
		/*taball("change.added", it.addedSubList)
		taball("change.removed", it.removed)
		println("change.wasPermutated\t${it.wasPermutated()}")*/
		exitProcess(0)
	  }
	}
	op()
  }
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




