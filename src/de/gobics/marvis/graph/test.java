/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.SortNetworksTask;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.utils.LoggingUtils;
import java.util.Arrays;
import java.util.logging.Level;

/**
 *
 * @author manuel
 */
public class test {

	public static void main(String[] args) throws Exception {
		LoggingUtils.initLogger(Level.FINER);
		MetabolicNetwork network = new LoadNetwork("testdata/wound1/wound1.complete.cut5000.intNames.xml.gz").perform();
		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setCofactorThreshold(10);
		process.setRestartProbability(0.8);
		MetabolicNetwork[] results = process.perform();
		NetworkSorterSumOfWeights sorter = new NetworkSorterSumOfWeights(network);
		results = new SortNetworksTask(sorter, network, results).perform();

		for (int idx = 0; idx < results.length; idx++) {
			System.out.println(results[idx].getName() + ": " + sorter.calculateScore(results[idx]));
		}
	}
}
