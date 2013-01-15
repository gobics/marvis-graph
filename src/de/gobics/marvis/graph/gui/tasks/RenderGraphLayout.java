/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

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
public class RenderGraphLayout extends SwingWorker<Layout, Void> {

    private final static Logger logger = Logger.getLogger(RenderGraphLayout.class.
            getName());
    private final Layout layout_to_render;

    public RenderGraphLayout(final Layout layout) {
        layout_to_render = layout;
    }

    @Override
    protected Layout doInBackground() throws Exception {
        return render(layout_to_render);
    }

    private Layout render(final Layout layout) {
        logger.finer("Starting rendering");
        if (!(layout instanceof IterativeContext)) {
            return layout;
        }

        getPropertyChangeSupport().firePropertyChange("description", null, "Rendering graph layout");
        getPropertyChangeSupport().firePropertyChange("title", null, "Layout");
        setProgress(0);

        logger.finer("Processing iterative calculation of the layout");

        IterativeContext iterative_layout = (IterativeContext) layout;
        while (!iterative_layout.done()) {
            iterative_layout.step();
        }

        // Copy the information to the new layout
        StaticLayout rendered = new StaticLayout(layout.getGraph(), layout.
                getSize());
        for (Object o : layout.getGraph().getVertices()) {
            rendered.setLocation(o, (Point2D) layout.transform(o));
        }

        logger.finer("Returning new layout");
        return rendered;
    }
}
