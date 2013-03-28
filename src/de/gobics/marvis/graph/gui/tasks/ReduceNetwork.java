/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Enzyme;
import de.gobics.marvis.graph.Gene;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.utils.task.AbstractTask;
import java.util.Collection;

/**
 *
 * @author manuel
 */
public class ReduceNetwork extends AbstractTask<MetabolicNetwork, Void> {

	private final MetabolicNetwork graph;

	public ReduceNetwork(MetabolicNetwork graph) {
		this.graph = graph;
		setTaskTitle("Cleaning metabolic network");
		setTaskDescription("Cleaning objects without relation to reactions");
	}

	@Override
	protected MetabolicNetwork doTask() throws Exception {
		final MetabolicNetwork clone = graph.clone();
		Collection<Compound> compounds = clone.getCompounds();
		Collection<Enzyme> enzymes = clone.getEnzymes();
		Collection<Gene> genes = clone.getGenes();

		setProgressMax(compounds.size() + enzymes.size() + genes.size());
		setProgress(0);

		for (Compound c : compounds) {
			incrementProgress();
			if (clone.getReactions(c).isEmpty()) {
				clone.remove(c);
			}
		}
		for (Enzyme e : enzymes) {
			incrementProgress();
			if (clone.getReactions(e).isEmpty()) {
				clone.remove(e);
			}
		}
		for (Gene g : genes) {
			incrementProgress();
			if (clone.getEncodedEnzymes(g).isEmpty()) {
				clone.remove(g);
			}
		}
		return clone;
	}
}
