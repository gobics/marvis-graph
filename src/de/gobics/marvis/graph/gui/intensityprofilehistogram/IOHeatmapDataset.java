package de.gobics.marvis.graph.gui.intensityprofilehistogram;

import de.gobics.marvis.graph.InputObject;
import de.gobics.marvis.graph.IntensityProfile;
import de.gobics.marvis.utils.ArrayUtils;
import java.util.Arrays;
import java.util.TreeMap;
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
public class IOHeatmapDataset implements XYZDataset {

	private static final Logger logger = Logger.getLogger(IOHeatmapDataset.class.
			getName());
	public final static Double DEFAULT_VALUE = Double.NaN;
	public final InputObject[] objects;
	public final String[] raw_condition_names;
	private DatasetGroup group = new DatasetGroup();

	public IOHeatmapDataset(final InputObject[] objects) {
		this.objects = objects;

		// Calculate the needed size for the heatmap (raw conditions)
		TreeMap<String, Integer> names = new TreeMap<String, Integer>();
		for (InputObject o : objects) {
			IntensityProfile ip = o.getIntensityProfile();
			for (String condition_name : ip.getConditionNames()) {
				if (!names.containsKey(condition_name)) {
					names.put(condition_name, ip.countIntensities(condition_name));
				}
				else {
					names.put(condition_name, Math.max(names.get(condition_name), ip.
							countIntensities(condition_name)));
				}
			}
		}
		int length = 0;
		for (String condition : names.keySet()) {
			length += names.get(condition);
		}
		raw_condition_names = new String[length];
		int idx = 0;
		for (String condition : names.keySet()) {
			for (int i = 0; i < names.get(condition); i++) {
				raw_condition_names[idx++] = condition;
			}
		}
		
		logger.info(Arrays.toString(raw_condition_names));
	}

	@Override
	public Number getZ(int x, int y) {
		x = trueRow(x);
		logger.info("Wanted is "+x+"x"+y);
		
		IntensityProfile ip = objects[y].getIntensityProfile();
		int sample_nr = x - ArrayUtils.indexOf(raw_condition_names, raw_condition_names[x]);
		float[] values = ip.getRawIntensities(raw_condition_names[x]);
		logger.info("Returning value for " + objects[y] + " position " + x);
		logger.info("Require sample " + sample_nr + " in: " + Arrays.toString(values));
		if (sample_nr >= values.length) {
			return DEFAULT_VALUE;
		}
		return (Float) values[sample_nr];
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
		return raw_condition_names.length;
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
		return raw_condition_names[i];
	}

	@Override
	public int indexOf(Comparable cmprbl) {
		System.err.println("Searching for: "+cmprbl);
		return ArrayUtils.indexOf(raw_condition_names, cmprbl);
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
		return raw_condition_names.length - row - 1;
	}

}
