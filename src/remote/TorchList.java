package remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Location;

public class TorchList implements List<Location>{

	private static final long serialVersionUID = 1L;
	private List<Location> l;
	boolean on = true;
	
	public TorchList(){
		l = Collections.synchronizedList(new ArrayList<Location>());
	}
	
	boolean isOn(){
		return on;
	}
	boolean toggle(){
		on = !on;
		return on;
	}
	
	@Override
	public boolean add(Location arg0) {
		return l.add(arg0);
	}
	@Override
	public void add(int arg0, Location arg1) {
		l.add(arg0, arg1);
	}
	@Override
	public boolean addAll(Collection<? extends Location> arg0) {
		return l.addAll(arg0);
	}
	@Override
	public boolean addAll(int arg0, Collection<? extends Location> arg1) {
		return l.addAll(arg0, arg1);
	}
	@Override
	public void clear() {
		l.clear();
	}
	@Override
	public boolean contains(Object arg0) {
		return l.contains(arg0);
	}
	@Override
	public boolean containsAll(Collection<?> arg0) {
		return l.containsAll(arg0);
	}
	@Override
	public Location get(int arg0) {
		return l.get(arg0);
	}
	@Override
	public int indexOf(Object arg0) {
		return l.indexOf(arg0);
	}
	@Override
	public boolean isEmpty() {
		return l.isEmpty();
	}
	@Override
	public Iterator<Location> iterator() {
		return l.iterator();
	}
	@Override
	public int lastIndexOf(Object arg0) {
		return l.lastIndexOf(arg0);
	}
	@Override
	public ListIterator<Location> listIterator() {
		return l.listIterator();
	}
	@Override
	public ListIterator<Location> listIterator(int arg0) {
		return l.listIterator(arg0);
	}
	@Override
	public boolean remove(Object arg0) {
		return l.remove(arg0);
	}
	@Override
	public Location remove(int arg0) {
		return l.remove(arg0);
	}
	@Override
	public boolean removeAll(Collection<?> arg0) {
		return l.removeAll(arg0);
	}
	@Override
	public boolean retainAll(Collection<?> arg0) {
		return l.retainAll(arg0);
	}
	@Override
	public Location set(int arg0, Location arg1) {
		return l.set(arg0, arg1);
	}
	@Override
	public int size() {
		return l.size();
	}
	@Override
	public List<Location> subList(int arg0, int arg1) {
		return l.subList(arg0, arg1);
	}
	@Override
	public Object[] toArray() {
		return l.toArray();
	}
	@Override
	public <T> T[] toArray(T[] arg0) {
		return l.toArray(arg0);
	}
}
