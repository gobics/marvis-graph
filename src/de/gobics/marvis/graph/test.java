/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph;

import de.gobics.marvis.graph.downloader.BiocycCreateNetworkProcess;
import de.gobics.marvis.graph.downloader.MetabolicNetworkTester;
import de.gobics.marvis.graph.gui.tasks.AbstractNetworkCalculation;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksPathway;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.utils.HumanReadable;
import de.gobics.marvis.utils.LoggingUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jblas.DoubleMatrix;

/**
 *
 * @author manuel
 */
public class test {

	private final static Logger logger = Logger.getLogger(test.class.getName());
	private static final int NUM_PERMUTES = 1000;
	private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() - 1;
	private static final int COFACTOR_THRESHOLD = 10;

	public static void main(String[] args) throws Exception, Throwable {
		LoggingUtils.initLogger(Level.FINER);
		MetabolicNetwork n = testBiocycImport();
		testCofactorThreshold(n);
	}

	private static MetabolicNetwork testBiocycImport() throws Exception {
		File in = new File("/home/manuel/ara.tar.gz");
		BiocycCreateNetworkProcess process = new BiocycCreateNetworkProcess(in);
		MetabolicNetwork network = process.doTask();

		MetabolicNetworkTester tester = new MetabolicNetworkTester(network);
		System.out.println(tester.generateReport());
		return network;
	}

	private static void testCofactorThreshold(MetabolicNetwork network) throws Exception {
		if (network == null) {
			network = new LoadNetwork("/home/manuel/marvis-graph-paper-data/graph.ath.e-atmx-9.cut10.xml.gz").load();
		}

		AbstractNetworkCalculation process = new CalculateNetworksPathway(network);

		Collection<MetabolicNetwork> networks = new ArrayList<>(Arrays.asList(new MetabolicNetwork[]{null, null}));
		int cf = 4;
		while (networks.size() > 1) {
			process.setCofactorThreshold(cf);
			networks = process.getSubnetworks(new LinkedList<>(network.getReactions()));
			System.out.println("Number of networks at cofactor " + cf + ": " + networks.size());
			cf++;
		}
	}

	private static void testMatrixSize() {
		Runtime rt = Runtime.getRuntime();
		for (int i = 100; true; i += 100) {
			DoubleMatrix m = new DoubleMatrix(i, i);
			System.out.println(i + " => " + HumanReadable.bytes(rt.totalMemory() - rt.freeMemory()));
			m = null;
			System.gc();
		}

	}

	private static void timeWalker() throws Throwable {
		MetabolicNetwork network = new LoadNetwork("testdata/wound1/wound1.complete.cut5000.intNames.xml.gz").perform();
		Runtime rt = Runtime.getRuntime();

		CalculateNetworksRWR process = new CalculateNetworksRWR(network, true);
		process.setRestartProbability(0.8);
		process.setCofactorThreshold(COFACTOR_THRESHOLD);
		long t0 = System.currentTimeMillis();
		process.perform();
		System.out.println("Sparse took " + (System.currentTimeMillis() - t0));
		System.out.println("Memory: " + (rt.totalMemory() - rt.freeMemory()));

		System.gc();

		process = new CalculateNetworksRWR(network, false);
		process.setRestartProbability(0.8);
		process.setCofactorThreshold(COFACTOR_THRESHOLD);
		t0 = System.currentTimeMillis();
		process.perform();
		System.out.println("Dense took " + (System.currentTimeMillis() - t0));
		System.out.println("Memory: " + (rt.totalMemory() - rt.freeMemory()));

		System.exit(0);
	}
}
