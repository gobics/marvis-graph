package de.gobics.marvis.utils.matrix;

import cern.colt.matrix.DoubleMatrix1D;
import de.gobics.marvis.utils.ArrayUtils;

/**
 * The awesome new DenseDoubleMatrix1D
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class DenseDoubleMatrix1D extends cern.colt.matrix.impl.DenseDoubleMatrix1D implements AnnotatedMatrix1D {

	private final Object[] label;

	public DenseDoubleMatrix1D(int size) {
		super(size);
		label = new Object[size()];
	}

	public DenseDoubleMatrix1D(double[] values) {
		super(values);
		label = new Object[size()];
	}

	public DenseDoubleMatrix1D(DoubleMatrix1D template) {
		this(template.size());
		for (int idx = 0; idx < size(); idx++) {
			setQuick(idx, template.getQuick(idx));
		}
		if (template instanceof AnnotatedMatrix1D) {
			setLabels(((AnnotatedMatrix1D) template).getLabels());
		}
	}

	public DenseDoubleMatrix1D(Object[] labels) {
		super(labels.length);
		label = labels.clone();
	}

	public void setLabel(int idx, Object new_label) {
		label[idx] = new_label;
	}

	public Object getLabel(int idx) {
		return label[idx];
	}

	public int getIndexForLabel(Object search_label) {
		return ArrayUtils.indexOf(label, search_label);
	}

	public void setLabels(Object[] new_labels) {
		System.arraycopy(new_labels, 0, label, 0, new_labels.length);
	}

	public Object[] getLabels() {
		return label.clone();
	}

	public LabelLookup getLookup() {
		return new LabelLookup(label);
	}
}
