package matt.hurricanefx.eye.lang


import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
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
fun <E> ObservableSet<E>.listen(
	onAdd: ((E)->Unit),
	onRemove: ((E)->Unit),
) {
	onChange { c ->
		if (c.wasAdded()) {
			onAdd(c.elementAdded)
		}
		if (c.wasRemoved()) { /*I'm not sure if its possible for one of these changes to have an add AND a remove. Probably not, but just being safe here.*/
			onRemove(c.elementRemoved)
		}
	}
}