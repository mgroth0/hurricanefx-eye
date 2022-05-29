package matt.hurricanefx.eye.lang


import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import matt.hurricanefx.eye.lib.onChange
import matt.klib.lang.err
import kotlin.reflect.KClass


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
  else                         -> err("primitives only for now")
}

