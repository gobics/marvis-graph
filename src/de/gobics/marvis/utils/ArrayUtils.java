/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author manuel
 */
public class ArrayUtils {

	public static int indexOf(Object[] array, Object o) {
		if (o == null || array == null) {
			return -1;
		}
		for (int idx = 0; idx < array.length; idx++) {
			if (o.equals(array[idx])) {
				return idx;
			}
		}
		return -1;
	}

	public static int lastIndexOf(Object[] array, Object o) {
		if (o == null || array == null) {
			return -1;
		}
		for (int idx = array.length - 1; idx >= 0; idx--) {
			if (o.equals(array[idx])) {
				return idx;
			}
		}
		return -1;
	}

	public static int indexOf(int[] array, int i) {
		if (array == null) {
			return -1;
		}
		for (int idx = 0; idx < array.length; idx++) {
			if (i == array[idx]) {
				return idx;
			}
		}
		return -1;
	}

	public static <T> T[] fillNull(T[] input, T value) {
		T[] array = input.clone();
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = value;
			}
		}
		return array;
	}

	public static String[] toStringArray(Object[] objects) {
		return map(new MapPredicate<Object, String>() {
			public String map(int index, Object obj) {
				return obj.toString();
			}
		}, objects).toArray(new String[objects.length]);
	}

	public static int[] toNativeArray(Integer[] source) {
		int[] native_array = new int[source.length];
		for (int i = 0; i < source.length; i++) {
			native_array[i] = (int) source[i];
		}
		return native_array;
	}

	public static int[] toNativeArray(Collection<Integer> source) {
		int[] native_array = new int[source.size()];
		int i = 0;
		for (Integer obj : source) {
			native_array[i++] = obj.intValue();
		}
		return native_array;
	}

	public static int[] toNativeArray(Integer[] source, Integer default_value) {
		int[] native_array = new int[source.length];
		for (int i = 0; i < source.length; i++) {
			native_array[i] = source[i] == null ? default_value : source[i];
		}
		return native_array;
	}

	public static double[] toNativeArray(Double[] source) {
		double[] native_array = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			native_array[i] = (double) source[i];
		}
		return native_array;
	}

	public static double[] toNativeArray(Double[] source, Double default_value) {
		double[] native_array = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			native_array[i] = source[i] == null ? default_value : source[i];
		}
		return native_array;
	}

	public static float[] toNativeArray(Float[] source) {
		float[] native_array = new float[source.length];
		for (int i = 0; i < source.length; i++) {
			native_array[i] = (float) source[i];
		}
		return native_array;
	}

	public static float[] toNativeArray(Float[] source, Float default_value) {
		float[] native_array = new float[source.length];
		for (int i = 0; i < source.length; i++) {
			native_array[i] = source[i] == null ? default_value : source[i];
		}
		return native_array;
	}

	public static <T> List<T> grep(GrepPredicate<T> predicate, T[] data) {
		return grep(predicate, Arrays.asList(data));
	}

	public static <T> List<T> grep(GrepPredicate<T> predicate, List<T> data) {
		List<T> data2 = new LinkedList<T>();
		for (int idx = 0; idx < data.size(); idx++) {
			if (predicate.accept(idx, data.get(idx))) {
				data2.add(data.get(idx));
			}
		}
		return data2;
	}

	public static Object[] reverse(Object[] input) {
		Object[] clone = new Object[input.length];
		for (int idx = 0; idx < clone.length; idx++) {
			clone[idx] = input[input.length - idx - 1];
		}
		return clone;
	}

	public static String[] reverse(String[] input) {
		String[] clone = new String[input.length];
		for (int idx = 0; idx < clone.length; idx++) {
			clone[idx] = input[input.length - idx - 1];
		}
		return clone;
	}

	public static double[] reverse(double[] input) {
		double[] clone = new double[input.length];
		for (int idx = 0; idx < clone.length; idx++) {
			clone[idx] = input[input.length - idx - 1];
		}
		return clone;
	}

	public static void reverseInplace(Object[] input) {
		Object tmp = null;
		for (int idx = 0; idx < input.length / 2; idx++) {
			tmp = input[idx];
			input[idx] = input[input.length - idx - 1];
			input[input.length - idx - 1] = tmp;
		}
	}

	public static double sum(double[] scores) {
		return sum(scores, false);
	}

	public static double sum(double[] scores, boolean use_absolute_values) {
		double sum = 0;
		for (double d : scores) {
			sum += use_absolute_values ? Math.abs(d) : d;
		}
		return sum;
	}

	public static <T> List<T> permute(Collection<T> input) {
		List<T> clone = new ArrayList<>(input);
		List<T> permutation = new ArrayList<>(input.size());

		Random rand = new Random(System.currentTimeMillis());
		while (!clone.isEmpty()) {
			permutation.add(clone.remove(rand.nextInt(clone.size())));
		}

		return permutation;
	}

	public static double mean(double[] scores) {
		double sum = 0;
		for (double s : scores) {
			sum += s;
		}
		return sum / scores.length;
	}

	public static double median(double[] scores) {
		double sum = 0;
		for (double s : scores) {
			sum += s;
		}
		return sum / scores.length;
	}

	/**
	 * Performs in-place shuffling of the given array. Implemented after <a
	 * href="http://stackoverflow.com/questions/1519736/random-shuffling-of-an-array">post
	 * from StackOverflow.com</a>.
	 *
	 * @param ar
	 */
	public static void shuffle(double[] ar) {
		// Implementing Fisher–Yates shuffle
		Random rnd = new Random(System.currentTimeMillis());
		for (int i = ar.length - 1; i >= 0; i--) {
			int index = rnd.nextInt(i + 1);
			double temp = ar[index];
			ar[index] = ar[i];
			ar[i] = temp;
		}
	}

	/**
	 * Interface for a predicate for grep
	 *
	 * @param <T>
	 */
	public interface GrepPredicate<T> {

		/**
		 * Return true if the object should be kept in the list
		 *
		 * @param index of the object given
		 * @param obj the actual object
		 * @return true if the object should be kept
		 */
		public boolean accept(int index, T obj);
	}

	public static <T, R> List<R> map(MapPredicate<T, R> predicate, T[] data) {
		return map(predicate, Arrays.asList(data));
	}

	public static <T, R> List<R> map(MapPredicate<T, R> predicate, Collection<T> data) {
		return map(predicate, new ArrayList<>(data));
	}

	public static <T, R> List<R> map(MapPredicate<T, R> predicate, List<T> data) {
		LinkedList<R> result = new LinkedList<R>();

		for (int idx = 0; idx < data.size(); idx++) {
			result.add(idx, predicate.map(idx, data.get(idx)));
		}

		return result;














	}

	/**
	 * Interface to map/transform objects of type T to type R
	 *
	 * @param <T>
	 * @param <R>
	 */
	public interface MapPredicate<T, R> {

		/**
		 * Return true if the object should be kept in the list
		 *
		 * @param index of the object given
		 * @param obj the actual object
		 * @return true if the object should be kept
		 */
		public R map(int index, T obj);
	}

	/**
	 * Sorts the first array and keeps the second array in the order of the
	 * sorting (in-place).
	 */
	public static void sortMulti(double[] tosort, Object[] keep_order) {
		sortMulti(tosort, keep_order, 0, tosort.length - 1);
	}

	/**
	 * Sorts the first array and keeps the second array in the order of the
	 * sorting.
	 *
	 * @param tosort
	 * @param keep_order
	 * @param start_idx
	 * @param end_idx
	 */
	public static void sortMulti(double[] tosort, Object[] keep_order, int start_idx, int end_idx) {
		if (tosort.length != keep_order.length) {
			throw new RuntimeException("Arrays have different size: " + Arrays.toString(tosort) + " vs " + Arrays.toString(keep_order));
		}

		// Create an auxiliary stack
		int stack[] = new int[end_idx - start_idx + 1];

		// initialize top of stack
		int top = -1;

		// push initial values of l and h to stack
		stack[ ++top] = start_idx;
		stack[ ++top] = end_idx;

		// Keep popping from stack while is not empty
		while (top >= 0) {
			// Pop h and l
			end_idx = stack[ top--];
			start_idx = stack[ top--];

			// Set pivot element at its correct position in sorted array
			double x = tosort[end_idx];
			int i = (start_idx - 1);
			for (int j = start_idx; j <= end_idx - 1; j++) {
				if (tosort[j] <= x) {
					i++;
					swap(tosort, keep_order, i, j);
				}
			}
			swap(tosort, keep_order, i + 1, end_idx);
			int p = (i + 1);

			// If there are elements on left side of pivot, then push left
			// side to stack
			if (p - 1 > start_idx) {
				stack[ ++top] = start_idx;
				stack[ ++top] = p - 1;
			}

			// If there are elements on right side of pivot, then push right
			// side to stack
			if (p + 1 < end_idx) {
				stack[ ++top] = p + 1;
				stack[ ++top] = end_idx;
			}
		}
	}

	private static void swap(double[] tosort, Object[] keep_order, int i, int j) {
		double d_tmop = tosort[i];
		tosort[i] = tosort[j];
		tosort[j] = d_tmop;
		Object obj = keep_order[i];
		keep_order[i] = keep_order[j];
		keep_order[j] = obj;
	}
}
