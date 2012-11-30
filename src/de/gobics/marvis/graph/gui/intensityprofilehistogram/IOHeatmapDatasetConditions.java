package de.gobics.marvis.graph.gui.intensityprofilehistogram;

import de.gobics.marvis.graph.InputObject;
import de.gobics.marvis.graph.IntensityProfile;
import de.gobics.marvis.utils.ArrayUtils;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

/**
 * IOHeatmapDataset
 *
 * x -> objects y -> conditions z -> value
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class IOHeatmapDatasetConditions implements XYZDataset {

	private static final Logger logger = Logger.getLogger(IOHeatmapDatasetConditions.class.
			getName());
	public final static Double DEFAULT_VALUE = Double.NaN;
	public final InputObject[] objects;
	public final String[] condition_names;
	private DatasetGroup group = new DatasetGroup();

	public IOHeatmapDatasetConditions(final InputObject[] objects) {
		this.objects = objects;

		// Calculate the needed size for the heatmap (raw conditions)
		TreeSet<String> names = new TreeSet<String>();
		for (InputObject o : objects) {
			IntensityProfile ip = o.getIntensityProfile();
			names.addAll(Arrays.asList(ip.getConditionNames()));
		}
		condition_names = names.toArray(new String[names.size()]);
	}

	@Override
	public Number getZ(int x, int y) {
		String condition = condition_names[trueRow(x)];
		IntensityProfile ip = objects[y].getIntensityProfile();
		return (Float)ip.getConditionIntensity(condition);
	}

	@Override
	public double getZValue(int i, int i1) {
		return getZ(i, i1).doubleValue();
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.NONE;
	}

	@Override
	public int getItemCount(int i) {
		return objects.length;
	}

	@Override
	public int getSeriesCount() {
		return condition_names.length;
	}

	@Override
	public Number getX(int row, int col) {
		return (Integer) col;
	}

	@Override
	public double getXValue(int row, int col) {
		return (double) col;
	}

	@Override
	public Number getY(int row, int col) {
		return (Integer) trueRow(row);
	}

	@Override
	public double getYValue(int row, int col) {
		return trueRow(row);
	}

	@Override
	public Comparable getSeriesKey(int i) {
		return condition_names[i];
	}

	@Override
	public int indexOf(Comparable cmprbl) {
		return ArrayUtils.indexOf(condition_names, cmprbl);
	}

	@Override
	public void addChangeListener(DatasetChangeListener dl) {
		// ignore
	}

	@Override
	public void removeChangeListener(DatasetChangeListener dl) {
		// ignore
	}

	@Override
	public DatasetGroup getGroup() {
		return group;
	}

	@Override
	public void setGroup(DatasetGroup dg) {
		this.group = dg;
	}

	private int trueRow(int row) {
		return condition_names.length - row - 1;
	}
}
