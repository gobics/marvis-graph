package de.gobics.marvis.graph.gui;

import de.gobics.marvis.graph.ExperimentalMarker;
import de.gobics.marvis.graph.IntensityProfile;
import de.gobics.marvis.graph.gui.intensityprofilehistogram.IOHeatmapDataset;
import de.gobics.marvis.graph.gui.intensityprofilehistogram.IOHeatmapDatasetConditions;
import de.gobics.marvis.graph.gui.intensityprofilehistogram.IPHistogramDataset;
import de.gobics.marvis.graph.gui.intensityprofilehistogram.IPHistogramDatasetConditions;
import de.gobics.marvis.utils.ArrayUtils;
import de.gobics.marvis.utils.ColorUtils;
import de.gobics.marvis.utils.matrix.Algebra;
import de.gobics.marvis.utils.swing.HeatmapPaintScale;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * The awesome new IntensityProfileHistogram
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class IntensityProfileHistogram {

	private static final Logger logger = Logger.getLogger(IntensityProfileHistogram.class.
			getName());

	public static ChartPanel createHistogram(IntensityProfile profile, boolean condition_based) {
		CategoryDataset dataset = condition_based ? new IPHistogramDatasetConditions(profile) : new IPHistogramDataset(profile);

		// create the chart...
		JFreeChart chart = ChartFactory.createBarChart(
				null, // chart title
				condition_based ? "Condition" : "Sample", // domain axis label
				"Abundance", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				true, // tooltips?
				false // URLs?
				);
		ChartUtilities.applyCurrentTheme(chart);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

		BarRenderer renderer = new CustomBarRenderer(dataset);
		renderer.setDrawBarOutline(true);
		renderer.setShadowVisible(false);
		plot.setRenderer(renderer);


		return new ChartPanel(chart);
	}

	public static ChartPanel createHeatmap(ExperimentalMarker[] objects) {
		IOHeatmapDataset dataset = new IOHeatmapDataset(objects);
		SymbolAxis yAxis = new SymbolAxis("Condition", dataset.raw_condition_names);
		SymbolAxis xAxis = new SymbolAxis("Marker/Transcripts", ArrayUtils.
				toStringArray(dataset.objects));
		
		XYBlockRenderer renderer = new XYBlockRenderer();

		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);


		JFreeChart chart = new JFreeChart(plot);
		chart.removeLegend();


		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (ExperimentalMarker o : objects) {
			for (float f : o.getRawIntensities()) {
				min = Math.min(min, f);
				max = Math.max(max, f);
			}
		}
		HeatmapPaintScale scale = new HeatmapPaintScale(min, max > min ? max : min + 1, Color.BLUE, Color.RED, Color.YELLOW);
		renderer.setPaintScale(scale);

		// Add a legend explaining the color
		NumberAxis scaleAxis = new NumberAxis();
		scaleAxis.setAxisLinePaint(Color.white);
		scaleAxis.setTickMarkPaint(Color.white);
		scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));
		PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
		legend.setStripOutlineVisible(true);
		legend.setSubdivisionCount(30);
		legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		legend.setAxisOffset(5.0);
		legend.setMargin(new org.jfree.ui.RectangleInsets(5, 5, 5, 5));
		legend.setPadding(new org.jfree.ui.RectangleInsets(10, 10, 10, 10));
		legend.setStripWidth(10);
		legend.setPosition(org.jfree.ui.RectangleEdge.RIGHT);
		chart.clearSubtitles();
		chart.addSubtitle(legend);

		ChartPanel panel = new ChartPanel(chart);
		JPopupMenu menu = panel.getPopupMenu();
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new ActionDisplayNormal(objects, plot)));
		menu.add(new JMenuItem(new ActionDisplayMean(objects, plot)));


		return panel;
	}

	private static class CustomBarRenderer extends BarRenderer {

		private final List<Color> colors;

		public CustomBarRenderer(CategoryDataset ds) {
			colors = ColorUtils.getUniqueColors(ds.getColumnCount());
		}

		@Override
		public Paint getItemPaint(int row, int column) {
			return colors.get(column);
		}

		@Override
		public double getMaximumBarWidth() {
			return 1d;
		}
	}

	private static class ActionDisplayNormal extends AbstractAction {

		private final IOHeatmapDataset dataset;
		private final XYPlot plot;

		public ActionDisplayNormal(ExperimentalMarker[] objects, XYPlot plot) {
			super("Display all samples");
			this.dataset = new IOHeatmapDataset(objects);
			this.plot = plot;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			plot.setDataset(dataset);
			plot.setRangeAxis(new SymbolAxis("Condition", dataset.raw_condition_names));
		}
	}

	private static class ActionDisplayMean extends AbstractAction {

		private final IOHeatmapDatasetConditions dataset;
		private final XYPlot plot;

		public ActionDisplayMean(ExperimentalMarker[] objects, XYPlot plot) {
			super("Display condition mean");
			this.dataset = new IOHeatmapDatasetConditions(objects);
			this.plot = plot;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			plot.setDataset(dataset);
			plot.setRangeAxis(new SymbolAxis("Condition", dataset.condition_names));
		}
	}
}
