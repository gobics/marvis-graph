package de.gobics.marvis.utils.swing;

import cern.colt.matrix.DoubleMatrix2D;
import de.gobics.marvis.utils.*;
import de.gobics.marvis.utils.matrix.DenseDoubleMatrix2D;
import java.awt.Color;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class Test {

	public static void main(String[] args) throws Throwable {
		LoggingUtils.initLogger(Level.FINER);
		try {
			testTextPopup();
			//testMetagroupSelection();
			//testHistogram();
			//testHeatmap();
			//testSaveGraphics();
			//testStatusDialog();
			//testTaskWrapper();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testMetagroupSelection() {
		String[] mgs_all = new String[]{"kegg/reactions", "kegg/pathways", "metacyc/pathways"};
		String[] mgs_description = new String[]{"KEGG Reactions", "KEGG Pathways", "Pathways"};
		String[] mgs_selected = new String[]{"kegg/pathways", "metacyc/pathways"};
		String[] ids = DialogMetagroupSelection.show(mgs_all, mgs_description, mgs_selected);
		System.out.println(Arrays.toString(ids));
	}

	private static void testHistogram() throws Exception {
		double[] data = new double[]{1d, 20d, -30d, 3d, -1d};
		Object[] label = new Object[]{"first", new Double(2), new Double(2), new Double(3), "third"};

		CategoryDataset dataset = new DefaultCategoryDataset();

		ChartFactory.createBarChart("Test", "Category", "Data", null, PlotOrientation.HORIZONTAL, true, true, true);
		//Histogram("test", "Category", "Value", null, PlotOrientation.HORIZONTAL, true, true, true);




		final Histogram hist = new Histogram(data);
		hist.setLabel(label);
		hist.setBarColors(new Color[]{Color.YELLOW, Color.RED});


		JFrame frame = new JFrame();
		frame.setTitle("Test Histogram");
		frame.add(hist);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
		System.out.println("Frame started");

	}

	private static void testHeatmap() throws Exception {
		double[][] data = new double[][]{
			new double[]{1d, 2d, 3d},
			new double[]{4d, 5d, 6d},
			new double[]{5d, 6d, 7d}
		};

		Object[] labelx = new Object[]{"first", "second", new Double(2)};
		Object[] labely = new Object[]{new Integer(1), "first", "first"};

		final DenseDoubleMatrix2D matrix = new DenseDoubleMatrix2D(data);
		matrix.setRowLabels(labelx);
		matrix.setColumnLabels(labely);
		final MatrixHeatmap heatmap = new MatrixHeatmap(matrix);

		HeatmapPaintScale scale = new HeatmapPaintScale(matrix.min(), matrix.max());
		scale.setBounds(
				matrix.min(), matrix.max(),
				new Color[]{Color.blue, Color.cyan, Color.yellow, Color.MAGENTA, Color.RED});
		heatmap.setHeatmapPaintScale(scale);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				JFrame frame = new JFrame();
				frame.setTitle("Test heatmap");
				frame.add(heatmap);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLocationRelativeTo(null);
				frame.pack();
				frame.setVisible(true);
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				HeatmapPaintScale hps = HeatmapPaintScale.showOptionDialog(matrix.min(), matrix.max());
				if (hps != null) {
					heatmap.setHeatmapPaintScale(hps);
				}
			}
		});


	}

	private static void testSaveGraphics() throws IOException {
		double[][] data = new double[][]{
			new double[]{1d, 2d, 3d},
			new double[]{4d, 5d, 6d},
			new double[]{5d, 6d, 7d}
		};

		String[] labelx = new String[]{"first", "second", "second"};
		String[] labely = new String[]{"1", "1", "2"};

		System.out.println("Creating heatmap");
		final Heatmap heatmap = new Heatmap(data);
		heatmap.setLabelX(labelx);
		heatmap.setLabelY(labely);
		System.out.println("Heatmap created");

		DialogSaveGraphic dialog = new DialogSaveGraphic(null, heatmap);
		//dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (dialog.showDialog()) {
			dialog.writeToFile();
		}
	}

	private static void testStatusDialog() throws Throwable {
		//	JOptionPane.showMessageDialog(null, "foobar", "foobar", JOptionPane.INFORMATION_MESSAGE);

		Statusdialog2 dialog = new Statusdialog2(null);

		de.gobics.marvis.utils.task.AbstractTask t1 = new de.gobics.marvis.utils.task.AbstractTask() {
			@Override
			protected Object doTask() throws Exception {
				int cur = 0, max = 100;
				setTaskTitle("My background task");
				setProgressMax(max);

				while (cur < max) {
					Thread.sleep(1000);
					cur++;
					setTaskDescription("Just counting for some time. I think it will be until " + max + "\n\nBy the way: currently I am at: " + cur + ". Nonetheless, you can also cancel me, if you like. Maybe I will also respond to it. Ha! Ha!");
					incrementProgress();
					if (isCanceled()) {
						return null;
					}
				}

				return null;
			}
		};

		de.gobics.marvis.utils.task.AbstractTask t2 = new de.gobics.marvis.utils.task.AbstractTask() {
			@Override
			protected Object doTask() throws Exception {
				int cur = 0, max = 100;
				setTaskTitle("My background task 2");
				setProgressMax(max);

				while (cur < max) {
					Thread.sleep(1000);
					cur++;
					setTaskDescription("I am not a pussy!!!");
					incrementProgress();
					if (isCanceled()) {
						return null;
					}
				}

				return null;
			}
		};

		dialog.monitorTask(t1);

		t1.perform();

		dialog.monitorTask(t2);
		t2.perform();
		System.exit(0);
	}

	private static void testTaskWrapper() throws Throwable {
		final de.gobics.marvis.utils.task.AbstractTask task1 = new de.gobics.marvis.utils.task.AbstractTask<Object, Object>() {
			@Override
			protected Object doTask() throws Exception {
				int max = 5;
				setTaskTitle("Example task 1");
				setTaskDescription("Will sleep while counting to: " + max);
				setProgressMax(max);
				for (int idx = 0; idx < max; idx++) {
					incrementProgress();
					System.out.println("Process run another iteration");
					Thread.sleep(1000);
					if (isCanceled()) {
						System.out.println("Task got cancel notification");
						return null;
					}
					setTaskDescription("New counter is: " + idx);
				}
				return null;
			}
		};

		final de.gobics.marvis.utils.task.AbstractTask task2 = new de.gobics.marvis.utils.task.AbstractTask<Object, Object>() {
			@Override
			protected Object doTask() throws Exception {
				System.err.println("Second thread started");
				int max = 3;
				setTaskTitle("Example task 2");
				setTaskDescription("Will sleep while counting to: " + max);
				setProgressMax(max);
				for (int idx = 0; idx < max; idx++) {
					incrementProgress();
					System.out.println("Process run another iteration");
					Thread.sleep(1000);
					if (isCanceled()) {
						System.out.println("Task got cancel notification");
						return null;
					}
					setTaskDescription("New counter is: " + idx);
				}
				return null;
			}
		};

		Statusdialog2 dialog = new Statusdialog2(null);
		dialog.monitorTask(task1);
		dialog.monitorTask(task2);
		task1.perform();
		Thread.sleep(1000);
		task2.perform();
	}

	private static void testTextPopup() {
		final JFrame frame = new JFrame("Test");
		JTextField field = new JTextField("Field with some text");
		frame.add(field);
		
		TextPopupMenu.create(field);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				frame.pack();
				frame.setVisible(true);
			}
		});
		
	}
}
