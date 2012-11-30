package de.gobics.marvis.graph.gui.intensityprofilehistogram;

import de.gobics.marvis.graph.IntensityProfile;
import de.gobics.marvis.utils.ArrayUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 * The awesome new IPHistogramDataset
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class IPHistogramDataset implements CategoryDataset {

	private static final Logger logger = Logger.getLogger(IPHistogramDataset.class.
			getName());
	private final IntensityProfile intensity_profile;
	private final int max_samples;
	private DatasetGroup group = new DatasetGroup();

	public IPHistogramDataset(IntensityProfile profile) {
		this.intensity_profile = profile;
		int max = 0;

		for (String cn : intensity_profile.getConditionNames()) {
			max = Math.max(max, intensity_profile.getRawIntensities(cn).length);
		}
		max_samples = max;
	}

	@Override
	public Comparable getColumnKey(int i) {
		return (Comparable) getColumnKeys().get(i);
	}

	@Override
	public int getColumnIndex(Comparable cmprbl) {
		return getColumnKeys().indexOf(cmprbl);
	}

	@Override
	public List getColumnKeys() {
		return Arrays.asList(intensity_profile.getConditionNames());
	}

	@Override
	public Comparable getRowKey(int i) {
		return (Comparable) getRowKeys().get(i);
	}

	@Override
	public int getRowIndex(Comparable cmprbl) {
		return getRowKeys().indexOf(cmprbl);
	}

	@Override
	public List getRowKeys() {
		List<Integer> list = new ArrayList<Integer>(max_samples);
		for (int idx = 0; idx < max_samples; idx++) {
			list.add(idx, idx + 1);
		}
		return list;
	}

	@Override
	public Number getValue(Comparable row_sample, Comparable col_condition) {
		float[] values = intensity_profile.getRawIntensities(col_condition.
				toString());
		int idx = new Integer(row_sample.toString()) - 1;
		if (idx < values.length) {
			return values[idx];
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return getColumnKeys().size();
	}

	@Override
	public int getRowCount() {
		return getRowKeys().size();
	}

	@Override
	public Number getValue(int i, int i1) {
		return getValue(getRowKey(i), getColumnKey(i1));
	}

	@Override
	public void addChangeListener(DatasetChangeListener dl) {
		//Ignore
	}

	@Override
	public void removeChangeListener(DatasetChangeListener dl) {
		//Ignore
	}

	@Override
	public DatasetGroup getGroup() {
		return group;
	}

	@Override
	public void setGroup(DatasetGroup dg) {
		this.group = dg;
	}
}
