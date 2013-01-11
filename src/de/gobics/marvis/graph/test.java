package de.gobics.marvis.graph;

import de.gobics.marvis.graph.graphview.GraphViewReactions;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.RandomWalkWithRestart;
import de.gobics.marvis.utils.matrix.DenseDoubleMatrix1D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class test {

    private final static Logger logger = Logger.getLogger(test.class.getName());
    private static double restart_probability = 0.8;

    public static void main(String[] args) throws Exception {
        LoggingUtils.initLogger(Level.FINER);
        // Load the network
        logger.finer("Loading network");
        LoadNetwork loader = new LoadNetwork("/home/manuel/marvis-graph-paper-data/graph.ath.cut10.xml.gz");
        MetabolicNetwork network = loader.load();

        // Find explainable nodes
        logger.finer("Searching explainable reactions as start nodes");
        Collection<Reaction> initial = new LinkedList<>();
        for (Reaction r : network.getReactions()) {
            if (network.isExplainable(r)) {
                initial.add(r);
            }
        }

        logger.log(Level.FINER, "Perfoming random walk process with {0} initial nodes", initial.size());
        RandomWalkWithRestart process = new RandomWalkWithRestart(new GraphViewReactions(network, true), restart_probability, 0.0000001);
        DenseDoubleMatrix1D result = process.walk(initial);
        logger.log(Level.FINER, "Process finished with {0} reactions with non-zero probability", result.cardinality());
        System.gc();

        LinkedList<Reaction> reactions_for_networks = new LinkedList<>();
        for (int i = 0; i < result.size(); i++) {
            if (result.getQuick(i) > (1-restart_probability)) {
                reactions_for_networks.add((Reaction) result.getLabel(i));
            }
        }
        logger.log(Level.FINER, "Found {0} reactions for the subnetworks", reactions_for_networks.size());

        Collection<MetabolicNetwork> subs = getSubnetworks(network, reactions_for_networks);
        logger.log(Level.FINER, "Found {0} subnetworks", subs.size());
    }

    public static Collection<MetabolicNetwork> getSubnetworks(MetabolicNetwork parent, LinkedList<Reaction> reactions) {
        LinkedList<MetabolicNetwork> subs = new LinkedList<>();

        TreeSet<Reaction> visited = new TreeSet<>();
        while (!reactions.isEmpty()) {
            TreeSet<Reaction> reactions_for_subnet = new TreeSet<>();
            LinkedList<Reaction> to_visit = new LinkedList<>();
            to_visit.add(reactions.poll());

            while (!to_visit.isEmpty()) {
                Reaction cur = to_visit.poll();
                reactions_for_subnet.add(cur);

                for (Reaction nr : getNeighbors(parent, cur)) {
                    if (reactions.contains(nr) && !visited.contains(nr)) {
                        to_visit.add(nr);
                    }
                    visited.add(nr);
                }
            }

            MetabolicNetwork sub = createNetwork(parent, reactions_for_subnet);
            subs.add(sub);

            for (Reaction r : reactions_for_subnet) {
                reactions.remove(r);
            }
        }

        return subs;
    }

    private static MetabolicNetwork createNetwork(MetabolicNetwork parent, Collection<Reaction> reactions_for_subnet) {
        return new MetabolicNetwork(parent);
    }

    private static Iterable<Reaction> getNeighbors(MetabolicNetwork parent, Reaction cur) {
        TreeSet<Reaction> neighbors = new TreeSet<>();
        for (Compound c : parent.getSubstrates(cur)) {
            for (Reaction r : parent.getSubstrateToReaction(c)) {
                neighbors.add(r);
            }
            for (Reaction r : parent.getProductToReaction(c)) {
                neighbors.add(r);
            }
        }
        for (Compound c : parent.getProducts(cur)) {
            for (Reaction r : parent.getSubstrateToReaction(c)) {
                neighbors.add(r);
            }
            for (Reaction r : parent.getProductToReaction(c)) {
                neighbors.add(r);
            }
        }
        // logger.log(Level.FINER, "Found {0} neighbors for {1}", new Object[]{neighbors.size(), cur});
        return neighbors;
    }
}