@file:Suppress("unused")

/*slightly modified code I stole from tornadofx*/

package matt.hurricanefx.eye.collect

import javafx.beans.Observable
import javafx.beans.WeakListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.ObservableFloatArray
import javafx.collections.ObservableIntegerArray
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import matt.klib.lang.setAll
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.Random
import java.util.WeakHashMap

/**
 * Returns an empty new [ObservableIntegerArray].
 */
fun observableIntArrayOf(): ObservableIntegerArray = FXCollections.observableIntegerArray()

/**
 * Returns a new [ObservableIntegerArray] with the given [elements].
 */
fun observableIntArrayOf(vararg elements: Int): ObservableIntegerArray = FXCollections.observableIntegerArray(*elements)

/**
 * Returns an empty new [ObservableFloatArray].
 */
fun observableFloatArrayOf(): ObservableFloatArray = FXCollections.observableFloatArray()

/**
 * Returns a new [ObservableFloatArray] with the given [elements].
 */
fun observableFloatArrayOf(vararg elements: Float): ObservableFloatArray = FXCollections.observableFloatArray(*elements)


/**
 * Returns an empty new [ObservableList].
 */
fun <T> observableListOf(): ObservableList<T> = FXCollections.observableArrayList()

/**
 * Returns a new [ObservableList] with the given [elements].
 */
fun <T> observableListOf(vararg elements: T): ObservableList<T> = FXCollections.observableArrayList(*elements)

/**
 * Returns a new [ObservableList] containing all elements from the given [collection].
 */
fun <T> observableListOf(collection: Collection<T>): ObservableList<T> = FXCollections.observableArrayList(collection)

/**
 * Returns an empty new [ObservableList] with the given [extractor]. This list reports element updates.
 */
fun <T> observableListOf(extractor: (T)->Array<Observable>): ObservableList<T> =
	FXCollections.observableArrayList(extractor)

/**
 * Returns an empty new [ObservableSet]
 */
fun <T> observableSetOf(): ObservableSet<T> = FXCollections.observableSet()

/**
 * Returns a new [ObservableSet] with the given elements.
 */
fun <T> observableSetOf(vararg elements: T): ObservableSet<T> = FXCollections.observableSet(*elements)

/**
 * Returns an empty new [ObservableMap]
 */
fun <K, V> observableMapOf(): ObservableMap<K, V> = FXCollections.observableHashMap()

/**
 * Returns a new [ObservableMap] with the specified contents, given as a list of pairs
 * where the first component is the key and the second is the value.
 */
fun <K, V> observableMapOf(vararg pairs: Pair<K, V>): ObservableMap<K, V> =
	FXCollections.observableMap(pairs.toMap(hashMapOf()))


/**
 * Returns a new [ObservableIntegerArray] with the elements from the original array.
 */
fun IntArray.toObservable(): ObservableIntegerArray = FXCollections.observableIntegerArray(*this)

/**
 * Returns a new [ObservableIntegerArray] with the elements from the original array.
 */
fun Array<Int>.toObservable(): ObservableIntegerArray = FXCollections.observableIntegerArray(*this.toIntArray())

/**
 * Returns a new [ObservableFloatArray] with the elements from the original array.
 */
fun FloatArray.toObservable(): ObservableFloatArray = FXCollections.observableFloatArray(*this)

/**
 * Returns a new [ObservableFloatArray] with the elements from the original array.
 */
fun Array<Float>.toObservable(): ObservableFloatArray = FXCollections.observableFloatArray(*this.toFloatArray())


/**
 * Returns a new [ObservableList] with the elements from the original list.
 */
fun <T> List<T>.toObservable(): ObservableList<T> = FXCollections.observableList(toMutableList())


/**
 * matt was here
 */
fun <T> Array<T>.toObservable(): ObservableList<T> = FXCollections.observableList(toMutableList())

/**
 * Returns a new [ObservableSet] with the elements from the original set.
 */
fun <T> Set<T>.toObservable(): ObservableSet<T> = FXCollections.observableSet(toMutableSet())

/**
 * Returns a new [ObservableMap] with the elements from the original map.
 */
fun <K, V> Map<K, V>.toObservable(): ObservableMap<K, V> = FXCollections.observableMap(toMutableMap())


/**
 * Returns a new [ObservableList] that is backed by the original list.
 *
 * **Note:** If the original list is read-only, attempting to modify the returned list will result in an [UnsupportedOperationException]
 */
fun <T> List<T>.asObservable(): ObservableList<T> = FXCollections.observableList(this)

/**
 * Returns a new [ObservableSet] that is backed by the original set.
 *
 * **Note:** If the original set is read-only, attempting to modify the returned set will result in an [UnsupportedOperationException]
 */
fun <T> Set<T>.asObservable(): ObservableSet<T> = FXCollections.observableSet(this)

/**
 * Returns a new [ObservableMap] that is backed by the original map.
 *
 * **Note:** If the original map is read-only, attempting to modify the returned map will result in an [UnsupportedOperationException]
 */
fun <K, V> Map<K, V>.asObservable(): ObservableMap<K, V> = FXCollections.observableMap(this)


/**
 * Returns an unmodifiable [ObservableList] that wraps the original list.
 */
fun <T> ObservableList<T>.asUnmodifiable(): ObservableList<T> = FXCollections.unmodifiableObservableList(this)

/**
 * Returns an unmodifiable [ObservableSet] that wraps the original set.
 */
fun <T> ObservableSet<T>.asUnmodifiable(): ObservableSet<T> = FXCollections.unmodifiableObservableSet(this)

/**
 * Returns an unmodifiable [ObservableMap] that wraps the original map.
 */
fun <K, V> ObservableMap<K, V>.asUnmodifiable(): ObservableMap<K, V> = FXCollections.unmodifiableObservableMap(this)


/**
 * Fills the observable list with the provided [value].
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.fill(value: T): Unit = FXCollections.fill(this, value)

/**
 * Reverse the order in the observable list.
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.reverse(): Unit = FXCollections.reverse(this)

/**
 * Randomly shuffles elements in this observable list.
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.shuffle(): Unit = FXCollections.shuffle(this)

/**
 * Randomly shuffles elements in this observable list using the specified [random] instance as the source of randomness.
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.shuffle(random: Random): Unit = FXCollections.shuffle(this, random)

/**
 * Sorts elements in the observable list according to their natural sort order.
 * Fires only **one** change notification on the list.
 */
fun <T: Comparable<T>> ObservableList<T>.sort() {
  if (size > 1) FXCollections.sort(this)
}

/**
 * Sorts elements in the observable list according to the order specified with [comparator].
 * Fires only **one** change notification on the list.
 */
fun <T> ObservableList<T>.sortWith(comparator: Comparator<in T>) {
  if (size > 1) FXCollections.sort(this, comparator)
}

/**
 * Sorts elements in the observable list according to natural sort order of the value returned by specified [selector] function.
 * Fires only **one** change notification on the list.
 */
inline fun <T, R: Comparable<R>> ObservableList<T>.sortBy(crossinline selector: (T)->R?) {
  if (size > 1) sortWith(compareBy(selector))
}

/**
 * Sorts elements in the observable list descending according to natural sort order of the value returned by specified [selector] function.
 * Fires only **one** change notification on the list.
 */
inline fun <T, R: Comparable<R>> ObservableList<T>.sortByDescending(crossinline selector: (T)->R?) {
  if (size > 1) sortWith(compareByDescending(selector))
}


/**
 * Moves the given **T** item to the specified index
 */
fun <T> MutableList<T>.move(item: T, newIndex: Int) {
  check(newIndex in 0 until size)
  val currentIndex = indexOf(item)
  if (currentIndex < 0) return
  removeAt(currentIndex)
  add(newIndex, item)
}

/**
 * Moves the given item at the `oldIndex` to the `newIndex`
 */
fun <T> MutableList<T>.moveAt(oldIndex: Int, newIndex: Int) {
  check(oldIndex in 0 until size)
  check(newIndex in 0 until size)
  val item = this[oldIndex]
  removeAt(oldIndex)
  add(newIndex, item)
}

/**
 * Moves all items meeting a predicate to the given index
 */
fun <T> MutableList<T>.moveAll(newIndex: Int, predicate: (T)->Boolean) {
  check(newIndex in 0 until size)
  val split = partition(predicate)
  clear()
  addAll(split.second)
  addAll(if (newIndex >= size) size else newIndex, split.first)
}

/**
 * Moves the given element at specified index up the **MutableList** by one increment
 * unless it is at the top already which will result in no movement
 */
fun <T> MutableList<T>.moveUpAt(index: Int) {
  if (index == 0) return
  check(index in indices, { "Invalid index $index for MutableList of size $size" })
  val newIndex = index - 1
  val item = this[index]
  removeAt(index)
  add(newIndex, item)
}

/**
 * Moves the given element **T** up the **MutableList** by one increment
 * unless it is at the bottom already which will result in no movement
 */
fun <T> MutableList<T>.moveDownAt(index: Int) {
  if (index == size - 1) return
  check(index in indices, { "Invalid index $index for MutableList of size $size" })
  val newIndex = index + 1
  val item = this[index]
  removeAt(index)
  add(newIndex, item)
}

/**
 * Moves the given element **T** up the **MutableList** by an index increment
 * unless it is at the top already which will result in no movement.
 * Returns a `Boolean` indicating if move was successful
 */
fun <T> MutableList<T>.moveUp(item: T): Boolean {
  val currentIndex = indexOf(item)
  if (currentIndex == -1) return false
  val newIndex = (currentIndex - 1)
  if (currentIndex <= 0) return false
  remove(item)
  add(newIndex, item)
  return true
}

/**
 * Moves the given element **T** up the **MutableList** by an index increment
 * unless it is at the bottom already which will result in no movement.
 * Returns a `Boolean` indicating if move was successful
 */
fun <T> MutableList<T>.moveDown(item: T): Boolean {
  val currentIndex = indexOf(item)
  if (currentIndex == -1) return false
  val newIndex = (currentIndex + 1)
  if (newIndex >= size) return false
  remove(item)
  add(newIndex, item)
  return true
}


/**
 * Moves first element **T** up an index that satisfies the given **predicate**, unless its already at the top
 */
inline fun <T> MutableList<T>.moveUp(crossinline predicate: (T)->Boolean) = find(predicate)?.let { moveUp(it) }

/**
 * Moves first element **T** down an index that satisfies the given **predicate**, unless its already at the bottom
 */
inline fun <T> MutableList<T>.moveDown(crossinline predicate: (T)->Boolean) = find(predicate)?.let { moveDown(it) }

/**
 * Moves all **T** elements up an index that satisfy the given **predicate**, unless they are already at the top
 */
inline fun <T> MutableList<T>.moveUpAll(crossinline predicate: (T)->Boolean) = asSequence().withIndex()
	.filter { predicate.invoke(it.value) }
	.forEach { moveUpAt(it.index) }

/**
 * Moves all **T** elements down an index that satisfy the given **predicate**, unless they are already at the bottom
 */
inline fun <T> MutableList<T>.moveDownAll(crossinline predicate: (T)->Boolean) = asSequence().withIndex()
	.filter { predicate.invoke(it.value) }
	.forEach { moveDownAt(it.index) }


fun <T> MutableList<T>.moveToTopWhere(predicate: (T)->Boolean) {
  asSequence().filter(predicate).toList().asSequence().forEach {
	remove(it)
	add(0, it)
  }
}

fun <T> MutableList<T>.moveToBottomWhere(predicate: (T)->Boolean) {
  val end = size - 1
  asSequence().filter(predicate).toList().asSequence().forEach {
	remove(it)
	add(end, it)
  }
}


/**
 * Swaps the position of two items at two respective indices
 */
fun <T> MutableList<T>.swap(indexOne: Int, indexTwo: Int) {
  if (this is ObservableList<*>) {
	if (indexOne == indexTwo) return
	val min = Math.min(indexOne, indexTwo)
	val max = Math.max(indexOne, indexTwo)
	val o2 = removeAt(max)
	val o1 = removeAt(min)
	add(min, o2)
	add(max, o1)
  } else {
	Collections.swap(this, indexOne, indexTwo)
  }
}

/**
 * Swaps the index position of two items
 */
fun <T> MutableList<T>.swap(itemOne: T, itemTwo: T) = swap(indexOf(itemOne), indexOf(itemTwo))


val COOL_STRONG_REFS_TO_IMITATE_NOMRAL_BIND_BEHAVIOR = WeakHashMap<List<*>, Observable>()


/**
 * MATT WAS HERE
 */
fun <SourceType, TargetType> MutableList<TargetType>.bind(
  source: ObservableValue<SourceType>,
  converter: (SourceType)->List<TargetType>
): ObjToListConversionListener<SourceType, TargetType> {


  /*THE FIX: by Matt*/
  COOL_STRONG_REFS_TO_IMITATE_NOMRAL_BIND_BEHAVIOR[this] = source

  val listener = ObjToListConversionListener(this, converter)
  (this as? ObservableList<TargetType>)?.setAll(converter(source.value)) ?: run {
	clear()
	addAll(converter(source.value))
  }
  source.removeListener(listener)
  source.addListener(listener)
  return listener
}


/**
 * Bind this list to the given observable list by converting them into the correct type via the given converter.
 * Changes to the observable list are synced.
 */
fun <SourceType, TargetType> MutableList<TargetType>.bind(
  sourceList: ObservableList<SourceType>,
  converter: (SourceType)->TargetType
): ListConversionListener<SourceType, TargetType> {

  /*  err("Matt was here: don't use this. I'm pretty sure its broken. My understanding is that when a property calls bind(), it stores a strong ref to the dependency, and the dep stores a weak ref to the dependent through a weaklistener. The result is that the dependency wont be randomly garbage collected. But if the dependent is garbage collected, it allows the dependency to be. But now look here: this isn't a classic bind(). Its just an addListener(). This misses the essential component of storing a strong ref to the depndency. Therefore, if used like a normal bind(), this will fail when the dependency is inevitably garbage collected. use bindWithBruteForce for now or find better way?")*/


  /*THE FIX: by Matt*/
  COOL_STRONG_REFS_TO_IMITATE_NOMRAL_BIND_BEHAVIOR[this] = sourceList

  val listener = ListConversionListener(this, converter)
  (this as? ObservableList<TargetType>)?.setAll(sourceList.map(converter)) ?: run {
	clear()
	addAll(sourceList.map(converter))
  }
  sourceList.removeListener(listener)
  sourceList.addListener(listener)
  return listener
}

/**
 * Bind this list to the given observable list by converting them into the correct type via the given converter.
 * Changes to the observable list are synced.
 */
fun <SourceType, TargetType> MutableList<TargetType>.bind(
  sourceSet: ObservableSet<SourceType>,
  converter: (SourceType)->TargetType
): SetConversionListener<SourceType, TargetType> {

  /*THE FIX: by Matt*/
  COOL_STRONG_REFS_TO_IMITATE_NOMRAL_BIND_BEHAVIOR[this] = sourceSet


  val listener = SetConversionListener(this, converter)
  if (this is ObservableList<*>) {
	sourceSet.forEach { source ->
	  val converted = converter(source)
	  listener.sourceToTarget[source] = converted
	}
	(this as ObservableList<TargetType>).setAll(listener.sourceToTarget.values)
  } else {
	clear()
	addAll(sourceSet.map(converter))
  }
  sourceSet.removeListener(listener)
  sourceSet.addListener(listener)
  return listener
}

fun <SourceTypeKey, SourceTypeValue, TargetType> MutableList<TargetType>.bind(
  sourceMap: ObservableMap<SourceTypeKey, SourceTypeValue>,
  converter: (SourceTypeKey, SourceTypeValue)->TargetType
): MapConversionListener<SourceTypeKey, SourceTypeValue, TargetType> {

  /*THE FIX: by Matt*/
  COOL_STRONG_REFS_TO_IMITATE_NOMRAL_BIND_BEHAVIOR[this] = sourceMap


  val listener = MapConversionListener(this, converter)
  if (this is ObservableList<*>) {
	sourceMap.forEach { source ->
	  val converted = converter(source.key, source.value)
	  listener.sourceToTarget[source] = converted
	}
	(this as ObservableList<TargetType>).setAll(listener.sourceToTarget.values)
  } else {
	clear()
	addAll(sourceMap.map { converter(it.key, it.value) })
  }
  sourceMap.removeListener(listener)
  sourceMap.addListener(listener)
  return listener
}


/**
 * MATT WAS HERE
 */
class ObjToListConversionListener<SourceType, TargetType>(
  targetList: MutableList<TargetType>,
  val converter: (SourceType)->List<TargetType>
): ChangeListener<SourceType>, WeakListener {
  internal val targetRef: WeakReference<MutableList<TargetType>> = WeakReference(targetList)


  override fun changed(observable: ObservableValue<out SourceType>?, oldValue: SourceType, newValue: SourceType) {
	//        println("INTERNAL ListConversionListener CHANGE")
	val list = targetRef.get()
	if (list == null) {
	  observable!!.removeListener(this)
	} else {
	  list.setAll(converter(newValue))
	}
  }

  override fun wasGarbageCollected() = targetRef.get() == null

  override fun hashCode() = targetRef.get().hashCode()

  override fun equals(other: Any?): Boolean {
	if (this === other) {
	  return true
	}

	val ourList = targetRef.get() ?: return false

	if (other is ObjToListConversionListener<*, *>) {
	  val otherList = other.targetRef.get()
	  return ourList === otherList
	}
	return false
  }


}


/**
 * Listens to changes on a list of SourceType and keeps the target list in sync by converting
 * each object into the TargetType via the supplied converter.
 */
class ListConversionListener<SourceType, TargetType>(
  targetList: MutableList<TargetType>,
  val converter: (SourceType)->TargetType
): ListChangeListener<SourceType>, WeakListener {
  internal val targetRef: WeakReference<MutableList<TargetType>> = WeakReference(targetList)

  override fun onChanged(change: ListChangeListener.Change<out SourceType>) {
	  /*println("INTERNAL ListConversionListener CHANGE")*/
	val list = targetRef.get()
	if (list == null) {
	  change.list.removeListener(this)
	} else {
	  while (change.next()) {
		if (change.wasPermutated()) {
		  /*println("PERMUTE")*/
		  list.subList(change.from, change.to).clear()
		  list.addAll(change.from, change.list.subList(change.from, change.to).map(converter))
		} else {
		  /*println("RESET ALL")
		  taball("change.addedSubList",change.addedSubList)
		  taball("change.removed",change.removed)*/

		  /*matt was here*/
		  /*change.list.removeListener(this)*/
		  list.setAll(change.list.map(converter))
		  /*change.list.addListener(this)*/

		  /*nice try tornadofx, but this causes a stupid index out of bounds error*/
		  /* if (change.wasRemoved()) {
			 list.subList(change.from, change.from + change.removedSize).clear()
		   }
		   if (change.wasAdded()) {
			 list.addAll(change.from, change.addedSubList.map(converter))
		   }*/
		}
	  }
	}
  }

  override fun wasGarbageCollected() = targetRef.get() == null

  override fun hashCode() = targetRef.get().hashCode()

  override fun equals(other: Any?): Boolean {
	if (this === other) {
	  return true
	}

	val ourList = targetRef.get() ?: return false

	if (other is ListConversionListener<*, *>) {
	  val otherList = other.targetRef.get()
	  return ourList === otherList
	}
	return false
  }
}

/**
 * Listens to changes on a Map of SourceTypeKey to SourceTypeValue and keeps the target list in sync by converting
 * each object into the TargetType via the supplied converter.
 */
class MapConversionListener<SourceTypeKey, SourceTypeValue, TargetType>(
  targetList: MutableList<TargetType>,
  val converter: (SourceTypeKey, SourceTypeValue)->TargetType
): MapChangeListener<SourceTypeKey, SourceTypeValue>, WeakListener {

  internal val targetRef: WeakReference<MutableList<TargetType>> = WeakReference(targetList)
  internal val sourceToTarget = HashMap<Map.Entry<SourceTypeKey, SourceTypeValue>, TargetType>()
  override fun onChanged(change: MapChangeListener.Change<out SourceTypeKey, out SourceTypeValue>) {
	val list = targetRef.get()
	if (list == null) {
	  change.map.removeListener(this)
	} else {
	  if (change.wasRemoved()) {
		list.remove(converter(change.key, change.valueRemoved))
	  }
	  if (change.wasAdded()) {
		list.add(converter(change.key, change.valueAdded))
	  }
	}
  }

  override fun wasGarbageCollected() = targetRef.get() == null

  override fun hashCode() = targetRef.get().hashCode()

  override fun equals(other: Any?): Boolean {
	if (this === other) {
	  return true
	}

	val ourList = targetRef.get() ?: return false

	if (other is MapConversionListener<*, *, *>) {
	  val otherList = other.targetRef.get()
	  return ourList === otherList
	}
	return false
  }
}

/**
 * Listens to changes on a set of SourceType and keeps the target list in sync by converting
 * each object into the TargetType via the supplied converter.
 */
class SetConversionListener<SourceType, TargetType>(
  targetList: MutableList<TargetType>,
  val converter: (SourceType)->TargetType
): SetChangeListener<SourceType>, WeakListener {
  internal val targetRef: WeakReference<MutableList<TargetType>> = WeakReference(targetList)
  internal val sourceToTarget = HashMap<SourceType, TargetType>()

  override fun onChanged(change: SetChangeListener.Change<out SourceType>) {
	val list = targetRef.get()
	if (list == null) {
	  change.set.removeListener(this)
	  sourceToTarget.clear()
	} else {
	  if (change.wasRemoved()) {
		list.remove(sourceToTarget[change.elementRemoved])
		sourceToTarget.remove(change.elementRemoved)
	  }
	  if (change.wasAdded()) {
		val converted = converter(change.elementAdded)
		sourceToTarget[change.elementAdded] = converted
		list.add(converted)
	  }
	}
  }

  override fun wasGarbageCollected() = targetRef.get() == null

  override fun hashCode() = targetRef.get().hashCode()

  override fun equals(other: Any?): Boolean {
	if (this === other) {
	  return true
	}

	val ourList = targetRef.get() ?: return false

	if (other is SetConversionListener<*, *>) {
	  val otherList = other.targetRef.get()
	  return ourList === otherList
	}
	return false
  }
}

fun <T> ObservableList<T>.invalidate() {
  if (isNotEmpty()) this[0] = this[0]
}

@Deprecated("Use `observableListOf()` instead.", ReplaceWith("observableListOf(entries)", "tornadofx.observableListOf"))
fun <T> observableList(vararg entries: T): ObservableList<T> = FXCollections.observableArrayList(entries.toList())
