package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.IntensityProfileHistogram;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author manuel
 */
public class test {

	private final static Logger logger = Logger.getLogger(test.class.getName());

	public static void main(String[] args) throws Exception {
		InputObject obj = new Marker("m1");
		obj.setIntensity(new String[]{"c1","c1","c2"}, new float[]{0.1f,0.3f,0.2f});
		InputObject obj2 = new Marker("m2");
		obj2.setIntensity(new String[]{"c1","c1","c2"}, new float[]{0.9f,0.7f,0.8f});
		
		ChartPanel p = IntensityProfileHistogram.createHeatmap(new InputObject[]{obj,obj2});
		JFrame frame = new JFrame();
		frame.add(p);
		frame.pack();
		frame.setVisible(true);	
	}
}