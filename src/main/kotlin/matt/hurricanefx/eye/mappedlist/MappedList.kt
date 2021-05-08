package matt.hurricanefx.eye.mappedlist


import javafx.beans.InvalidationListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lib.onChange
import matt.kjlib.NEVER
import matt.kjlib.err

fun <O, E> ObservableList<O>.toMappedList(mapfun: (O)->E) = MappedList(this, mapfun)




class MappedList<O, E>(
  sourceList: ObservableList<O>,
  mapfun: (O)->E
): ObservableList<E> {

  private val list = sourceList.map(mapfun).toObservable()

  init {
	/*TODO: see tornadofx ListConversionListener thing for ideas on optimization, both in terms of weak references and editing sublists*/
	sourceList.onChange {
	  //	  println("SOURCE LIST CHANGED")
	  /*individual changes seem impossible to track down since observableList listeners dont use indices?*/
	  list.setAll(sourceList.map(mapfun))
	}
  }


  override val size: Int
	get() = list.size

  override fun add(element: E): Boolean {
	NEVER
  }

  override fun add(index: Int, element: E) {
	NEVER
  }

  override fun addAll(vararg elements: E): Boolean {
	NEVER
  }

  override fun containsAll(elements: Collection<E>): Boolean {
	return list.containsAll(elements)
  }

  override fun indexOf(element: E): Int {
	return list.indexOf(element)
  }

  override fun iterator(): MutableIterator<E> {
	return ReadOnlyMutableIterator(list.iterator())
  }

  override fun addListener(listener: InvalidationListener?) {
	return list.addListener(listener)
  }

  override fun removeListener(listener: InvalidationListener?) {
	return list.removeListener(listener)
  }

  override fun addAll(index: Int, elements: Collection<E>): Boolean {
	NEVER
  }

  override fun addAll(elements: Collection<E>): Boolean {
	NEVER
  }

  override fun clear() {
	NEVER
  }

  override fun listIterator(): MutableListIterator<E> {
	NEVER
  }

  override fun listIterator(index: Int): MutableListIterator<E> {
	NEVER
  }

  override fun remove(from: Int, to: Int) {
	NEVER
  }

  override fun remove(element: E): Boolean {
	NEVER
  }

  override fun removeAll(vararg elements: E): Boolean {
	NEVER
  }

  override fun removeAll(elements: Collection<E>): Boolean {
	NEVER
  }

  override fun removeAt(index: Int): E {
	NEVER
  }

  override fun retainAll(vararg elements: E): Boolean {
	NEVER
  }

  override fun contains(element: E): Boolean {
	return list.contains(element)
  }

  override fun get(index: Int): E {
	return list.get(index)
  }

  override fun isEmpty(): Boolean {
	return list.isEmpty()
  }

  override fun lastIndexOf(element: E): Int {
	return list.lastIndexOf(element)
  }

  override fun retainAll(elements: Collection<E>): Boolean {
	NEVER
  }

  override fun set(index: Int, element: E): E {
	NEVER
  }

  override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
	NEVER
  }

  override fun addListener(listener: ListChangeListener<in E>?) {
	return list.addListener(listener)
  }

  override fun removeListener(listener: ListChangeListener<in E>?) {
	return list.removeListener(listener)
  }

  override fun setAll(vararg elements: E): Boolean {
	NEVER
  }

  override fun setAll(col: MutableCollection<out E>?): Boolean {
	NEVER
  }

}

class ReadOnlyMutableIterator<E>(private val itr: MutableIterator<E>): MutableIterator<E> {
  override fun hasNext(): Boolean {
	return itr.hasNext()
  }

  override fun next(): E {
	return itr.next()
  }

  override fun remove() {
	NEVER
  }

}