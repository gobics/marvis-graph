/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class SparseDoubleMatrix1D extends cern.colt.matrix.impl.SparseDoubleMatrix1D implements AnnotatedMatrix1D {

	private final Object[] label;

	public SparseDoubleMatrix1D(double[] doubles) {
		super(doubles);
		label = new String[size()];

	}

	public SparseDoubleMatrix1D(int i) {
		super(i);
		label = new String[size()];
	}

	public SparseDoubleMatrix1D(int i, int i1, double d, double d1) {
		super(i, i1, d, d1);
		label = new String[size()];

	}

	public SparseDoubleMatrix1D(Object[] labels) {
		super(labels.length);
		label = labels.clone();
	}

	public SparseDoubleMatrix1D(cern.colt.matrix.DoubleMatrix1D template) {
		this(template.toArray());
	}

	public LabelLookup getLookup() {
		HashMap<Object, HashSet<Integer>> lookup = new HashMap<Object, HashSet<Integer>>();
		for (int idx = 0; idx < size(); idx++) {
			if (label[idx] != null) {
				if (lookup.containsKey(label[idx])) {
					lookup.get(label[idx]).add(idx);
				}
				else {
					HashSet<Integer> set = new HashSet<Integer>();
					set.add(idx);
					lookup.put(label[idx], set);
				}
			}
		}
		return new LabelLookup(lookup);
	}

	public void setLabel(int idx, Object label) {
		this.label[idx] = label;
	}

	public Object getLabel(int idx) {
		return label[idx];
	}

	public int getIndexForLabel(Object label) {
		return getLookup().getFirst(label);
	}

	public void setLabels(Object[] new_labels) {
		if (new_labels.length > size()) {
			throw new IndexOutOfBoundsException("New string array is longer than length of vector");
		}

		System.arraycopy(new_labels, 0, label, 0, new_labels.length);
	}

	public Object[] getLabels() {
		return label.clone();
	}
}
