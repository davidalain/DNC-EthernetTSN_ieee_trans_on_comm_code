package org.networkcalculus.dnc.ethernet.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class DataPrinterUtil {

	public static <T> String toString(T item) {
		final StringBuilder sb = new StringBuilder();
		return toString(sb, "", item);
	}

	public static <T> String toString(T[] array) {
		final StringBuilder sb = new StringBuilder();
		return toString(sb, "", array);
	}

	public static <T> String toString(List<T> list) {
		final StringBuilder sb = new StringBuilder();
		return toString(sb, "", list);
	}

	public static <T> String toString(Set<T> set) {
		final StringBuilder sb = new StringBuilder();
		return toString(sb, "", set);
	}

	public static <K,V> String toString(Entry<K,V> entry) {
		final StringBuilder sb = new StringBuilder();
		return toString(sb, "", entry.getKey(), entry.getValue());
	}

	public static <K,V> String toString(K k, V v) {
		final StringBuilder sb = new StringBuilder();
		return toString(sb, "", k, v);
	}

	public static <K,V> String toString(Map<K,V> map) {
		final StringBuilder sb = new StringBuilder();
		return toString(sb, "", map);
	}
	
	public static <K,V> String toString2(Map<K,V> map) {
		final StringBuilder sb = new StringBuilder();
		return toString2(sb, "", map);
	}

	public static <T> String toString(StringBuilder sb, String prefix, T item) {
		return sb.append(prefix).append(item).toString();
	}

	public static <K,V> String toString(StringBuilder sb, String prefix, K k, V v) {
		sb.append(prefix).append(k);
		if(v instanceof Map<?, ?>) {
			sb.append("\n").append(prefix);
			return toString(sb, "\t", (Map<?, ?>) v);
		} else if (v instanceof List<?>){
			sb.append("\n").append(prefix);
			return toString(sb, "\t", (List<?>) v);
		} else if (v instanceof Set<?>){
			sb.append("\n").append(prefix);
			return toString(sb, "\t", (Set<?>) v);
		} else if (v instanceof Object[]){
			sb.append("\n").append(prefix);
			return toString(sb, "\t", (Object[]) v);
		} else {
			return toString(sb, " -> ", v);
		}
	}

	public static <K,V> String toString2(StringBuilder sb, String prefix, Map<K,V> map) {
		if(map != null) {
			final List<K> keys = new LinkedList<>(map.keySet());
			keys.sort(new Comparator<K>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(K o1, K o2) {
					if(o1 instanceof Comparable && o2 instanceof Comparable) {
						return ((Comparable<K>) o1).compareTo(o2);
					}
					// TODO Auto-generated method stub
					return 0;
				}
			});
			for(K k : keys) {
				V v = map.get(k);
				toString(sb, prefix, k, v);
				sb.append("\n");
			}
			return sb.toString();
		}else {
			return "(null)";
		}
	}
	
	public static <K,V> String toString(StringBuilder sb, String prefix, Map<K,V> map) {
		if(map != null) {
			map.forEach((k,v) -> {
				toString(sb, prefix, k, v);
				sb.append("\n");
			});
			return sb.toString();
		}else {
			return "(null)";
		}
	}

	public static <T> String toString(StringBuilder sb, String prefix, List<T> list) {
		if(list != null) {
			list.forEach(item -> {
				sb.append(prefix).append(item).append("\n");
			});
			return sb.toString();
		} else {
			return "(null)";
		}
	}

	public static <T> String toString(StringBuilder sb, String prefix, Set<T> set) {
		if(set != null) {
			set.forEach(item -> {
				sb.append(prefix).append(item).append("\n");
			});
			return sb.toString();
		} else {
			return "(null)";
		}
	}

	public static <T> String toString(StringBuilder sb, String prefix, T[] array) {
		if(array != null) {
			Arrays.stream(array).forEach(item -> {
				sb.append(prefix).append(item).append("\n");
			});
			return sb.toString();
		} else {
			return "(null)";
		}
	}

	//------------------------------------------------------------------------------

	public static <T> void print(T item) {
		print("", item);
	}

	public static <T> void print(String prefix, T item) {
		System.out.println(prefix + item);
	}

	public static <K,V> void print(Map<K,V> map) {
		print("" , map);
	}

	public static <K,V> void print(Entry<K,V> entry) {
		if(entry != null)
			print(entry.getKey(), entry.getValue());
		else
			System.out.println("(null)");
	}

	public static <K,V> void print(K k, V v) {
		System.out.print(k);
		if(v instanceof Map<?, ?>) {
			System.out.println();
			print("t", (Map<?, ?>) v);
		} else if (v instanceof List<?>){
			System.out.println();
			print("\t", (List<?>) v);
		} else if (v instanceof Set<?>){
			System.out.println();
			print("\t", (Set<?>) v);
		} else if (v instanceof Object[]){
			System.out.println();
			print("\t", (Object[]) v);
		} else {
			print(" -> ", v);
		}
	}

	public static <K,V> void print(String prefix, Map<K,V> map) {
		if(map != null)
			map.forEach((k,v) -> {
				print(k, v);
			});
		else
			System.out.println("(null)");
	}

	public static <T> void print(List<T> list) {
		print("", list);
	}

	public static <T> void print(String prefix, List<T> list) {
		if(list != null)
			list.forEach(item -> {
				System.out.println(prefix + item);
			});
		else
			System.out.println("(null)");
	}

	public static <T> void print(Set<T> set) {
		print("", set);
	}

	public static <T> void print(String prefix, Set<T> set) {
		if(set != null)
			set.forEach(item -> {
				System.out.println(prefix + item);
			});
		else
			System.out.println("(null)");
	}

	public static <T> void print(T[] array) {
		print("", array);
	}

	public static <T> void print(String prefix, T[] array) {
		if(array != null)
			Arrays.stream(array).forEach(item -> {
				System.out.println(prefix + item);
			});
		else
			System.out.println("(null)");
	}


	public static <T> String toStringStaticLength(T obj, int length, char fill) {

		final StringBuilder sb = new StringBuilder();
		sb.append(obj);

		if(sb.length() > length)
			sb.setLength(length);
		else
			while(sb.length() < length)
				sb.append(fill);

		return sb.toString();
	}

}
