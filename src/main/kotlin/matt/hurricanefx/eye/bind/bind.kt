package matt.hurricanefx.eye.bind

import javafx.beans.binding.BooleanExpression
import javafx.beans.property.BooleanProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableNumberValue
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.WeakChangeListener
import javafx.collections.ObservableList
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.prop.eq

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