package de.gobics.marvis.graph.gui.intensityprofilehistogram;

import de.gobics.marvis.graph.IntensityProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 * rows -> samples = 1
 * cols -> conditions
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class IPHistogramDatasetConditions implements CategoryDataset {

	private static final Logger logger = Logger.getLogger(IPHistogramDatasetConditions.class.
			getName());
	private final IntensityProfile intensity_profile;
	private DatasetGroup group = new DatasetGroup();
	private static final List<Integer> ROW_KEYS = new ArrayList<Integer>(Arrays.asList(new Integer[]{1}));

	public IPHistogramDatasetConditions(IntensityProfile profile) {
		this.intensity_profile = profile;
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
		return ROW_KEYS;
	}

	@Override
	public Number getValue(Comparable row_sample, Comparable col_condition) {
		return intensity_profile.getConditionIntensity(col_condition.toString());
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
