/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.graph.graphview.GraphViewReactions;
import de.gobics.marvis.utils.RandomWalkWithRestart;
import de.gobics.marvis.utils.matrix.DenseDoubleMatrix1D;
import de.gobics.marvis.utils.swing.AbstractTask;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class CalculateNetworksRWR extends AbstractTask<MetabolicNetwork[], Void> {

    private static final Logger logger = Logger.getLogger(CalculateNetworksRWR.class.
            getName());
    private final MetabolicNetwork root_network;
    private final GraphViewReactions reactionview;
    private double restart_probability = 0.8;

    public CalculateNetworksRWR(MetabolicNetwork network) {
        this.root_network = network;
        this.reactionview = new GraphViewReactions(root_network, true);
    }

    @Override
    protected MetabolicNetwork[] performTask() throws Exception {
        return calculateNetworks();
    }

    public MetabolicNetwork[] calculateNetworks() throws Exception {
        // Find explainable nodes
        sendDescription("Search start nodes");
        setProgressMax(5);
        setProgress(1);
        logger.finer("Calculating reaction start probabilities");
        Map<Reaction, Double> initial = calculateInitialScores();

        sendDescription("Performing random walk for reaction scoring");
        setProgress(2);
        logger.log(Level.FINER, "Perfoming random walk process with {0} initial nodes", initial.size());
        RandomWalkWithRestart process = new RandomWalkWithRestart(reactionview, restart_probability, 0.0000001);
        DenseDoubleMatrix1D result = process.walk(initial);
        logger.log(Level.FINER, "Process finished with {0} reactions with non-zero probability", result.cardinality());
        System.gc();

        LinkedList<Reaction> reactions_for_networks = new LinkedList<>();
        setProgress(3);
        for (int i = 0; i < result.size(); i++) {
            if (result.getQuick(i) >= (1 - restart_probability)) {
                reactions_for_networks.add((Reaction) result.getLabel(i));
            }
        }
        logger.log(Level.FINER, "Found {0} reactions for the subnetworks", reactions_for_networks.size());

        sendDescription("Generating new metabolic sub-networks");
        setProgressMax(4);
        Collection<MetabolicNetwork> subs = getSubnetworks(reactions_for_networks);
        logger.log(Level.FINER, "Found {0} subnetworks", subs.size());
        setProgress(5);

        return subs.toArray(new MetabolicNetwork[subs.size()]);
    }

    private Collection<MetabolicNetwork> getSubnetworks(LinkedList<Reaction> reactions) {
        LinkedList<MetabolicNetwork> subs = new LinkedList<>();

        TreeSet<Reaction> visited = new TreeSet<>();
        while (!reactions.isEmpty()) {
            TreeSet<Reaction> reactions_for_subnet = new TreeSet<>();
            LinkedList<Reaction> to_visit = new LinkedList<>();
            to_visit.add(reactions.poll());

            while (!to_visit.isEmpty()) {
                Reaction cur = to_visit.poll();
                reactions_for_subnet.add(cur);

                for (Reaction nr : getNeighbors(cur)) {
                    if (reactions.contains(nr) && !visited.contains(nr)) {
                        to_visit.add(nr);
                    }
                    visited.add(nr);
                }
            }

            MetabolicNetwork sub = generate_network(reactions_for_subnet);
            subs.add(sub);

            for (Reaction r : reactions_for_subnet) {
                reactions.remove(r);
            }
        }

        return subs;
    }

    private Set<Reaction> getNeighbors(Reaction cur) {
        TreeSet<Reaction> neighbors = new TreeSet<>();
        for (Compound c : root_network.getSubstrates(cur)) {
            if (!root_network.isExplainable(c)) {
                continue;
            }

            for (Reaction r : root_network.getSubstrateToReaction(c)) {
                neighbors.add(r);
            }
            for (Reaction r : root_network.getProductToReaction(c)) {
                neighbors.add(r);
            }
        }
        for (Compound c : root_network.getProducts(cur)) {
            if (!root_network.isExplainable(c)) {
                continue;
            }

            for (Reaction r : root_network.getSubstrateToReaction(c)) {
                neighbors.add(r);
            }
            for (Reaction r : root_network.getProductToReaction(c)) {
                neighbors.add(r);
            }
        }
        // logger.log(Level.FINER, "Found {0} neighbors for {1}", new Object[]{neighbors.size(), cur});
        return neighbors;
    }

    private MetabolicNetwork generate_network(Set<Reaction> neighbor_nodes) {
        MetabolicNetwork network = new MetabolicNetwork(root_network);
        network.setName("Subnetwork: " + neighbor_nodes.iterator().next().getName());

        // Iterate over all objects and get there environments
        for (Reaction o : neighbor_nodes) {
            for (Relation r : root_network.getEnvironment(o)) {
                network.addRelation(r);
            }
        }

        return network;
    }

    public void setRestartProbability(double probability) {
        this.restart_probability = probability;
    }

    private Map<Reaction, Double> calculateInitialScores() {
        Map<Reaction, Double> reaction_scores = new TreeMap<>();

        for (Marker marker : root_network.getMarkers()) {
            LinkedList<Compound> compounds = root_network.getAnnotations(marker);

            for (Compound compound : compounds) {
                LinkedList<Reaction> reactions = new LinkedList<>();
                reactions.addAll(root_network.getSubstrateToReaction(compound));
                reactions.addAll(root_network.getProductToReaction(compound));

                double addscore = (1d / compounds.size()) / reactions.size();
                for (Reaction r : reactions) {

                    if (!reaction_scores.containsKey(r)) {
                        reaction_scores.put(r, addscore);
                    } else {
                        reaction_scores.put(r, reaction_scores.get(r) + addscore);
                    }
                }
            }
        }

        for (Transcript transcript : root_network.getTranscripts()) {
            LinkedList<Gene> genes = root_network.getGenes(transcript);

            for (Gene gene : genes) {
                LinkedList<Enzyme> enzymes = root_network.getEncodedEnzymes(gene);
                for (Enzyme enzyme : enzymes) {
                    LinkedList<Reaction> reactions = root_network.getReactions(enzyme);
                    double addscore = ((1d / genes.size()) / enzymes.size()) / reactions.size();
                    for (Reaction r : reactions) {

                        if (!reaction_scores.containsKey(r)) {
                            reaction_scores.put(r, addscore);
                        } else {
                            reaction_scores.put(r, reaction_scores.get(r) + addscore);
                        }
                    }
                }
            }
        }

        System.out.println(reaction_scores);
        return reaction_scores;
    }
}