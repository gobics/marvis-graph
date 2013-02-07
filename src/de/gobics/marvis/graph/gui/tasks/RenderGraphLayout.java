/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.utils.task.AbstractTask;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author manuel
 */
public class RenderGraphLayout extends AbstractTask<ISOMLayout, Void> {

	private final static Logger logger = Logger.getLogger(RenderGraphLayout.class.
			getName());
	private final ISOMLayout layout_to_render;

	public RenderGraphLayout(final ISOMLayout layout) {
		layout_to_render = layout;
	}

	@Override
	protected ISOMLayout doTask() throws Exception {
		return render(layout_to_render);
	}

	private ISOMLayout render(final ISOMLayout layout) {
		logger.finer("Starting rendering");
		setTaskDescription("Rendering graph layout");
		setTaskTitle("Layout");
		
		logger.finer("Processing iterative calculation of the layout");
		while (!layout.done()) {
			layout.step();
		}
		
		logger.finer("Returning new layout");
		return layout;
	}
}
