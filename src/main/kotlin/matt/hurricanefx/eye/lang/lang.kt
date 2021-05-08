package matt.hurricanefx.eye.lang


import javafx.beans.property.*
import javafx.collections.ObservableList
import matt.hurricanefx.eye.lib.onChange


infix fun <T> ObjectProperty<T>.set(v: T) = set(v)
fun <T> T.inprop(): SimpleObjectProperty<T> {
  return SimpleObjectProperty<T>(this)
}
typealias PropN<T> = SimpleObjectProperty<T?>
typealias Prop<T> = SimpleObjectProperty<T>
typealias DProp = SimpleDoubleProperty
typealias LProp = SimpleLongProperty
typealias IProp = SimpleIntegerProperty
typealias BProp = SimpleBooleanProperty
typealias SProp = SimpleStringProperty


fun <E> ObservableList<E>.listen(
  onAdd: ((E)->Unit),
  onRemove: ((E)->Unit),
) {
  onChange { c ->
	while (c.next()) {
	  c.addedSubList.forEach {
		onAdd(it)
	  }
	  c.removed.forEach {
		onRemove(it)
	  }
	}
  }
}