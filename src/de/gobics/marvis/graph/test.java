/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph;

import de.gobics.marvis.graph.downloader.BiocycCreateNetworkProcess;
import de.gobics.marvis.graph.gui.tasks.MetabolicNetworkReport;
import de.gobics.marvis.graph.gui.tasks.AbstractNetworkCalculation;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksPathway;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.ImportMetabolicMarker;
import de.gobics.marvis.graph.gui.tasks.ImportTranscriptomics;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.ReduceNetwork;
import de.gobics.marvis.graph.sort.NetworkSorterDiameter;
import de.gobics.marvis.graph.sort.NetworkSorterSumOfWeights;
import de.gobics.marvis.utils.HumanReadable;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.io.CsvDataReader;
import de.gobics.marvis.utils.io.ExcelDataReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
		listNetworks();
	}

	private static void listNetworks() throws Exception {
		MetabolicNetwork network = new LoadNetwork("/home/manuel/marvis-graph-paper-data/graph.aracyc.cut25.cut10.xml.gz").load();
		CalculateNetworksRWR process = new CalculateNetworksRWR(network);
		process.setRestartProbability(0.8);
		process.setThreshold(0.2);
		process.setCofactorThreshold(10);
		MetabolicNetwork[] subnetworks = process.calculateNetworks();

		NetworkSorterDiameter dia = new NetworkSorterDiameter(network);
		NetworkSorterSumOfWeights sow = new NetworkSorterSumOfWeights(network);

		for (MetabolicNetwork n : subnetworks) {
			String name = n.getName().length() < 112 ? n.getName() : n.getName().substring(12, 100) + "...";
			System.out.println(name + "\t" + n.getReactions().size() + "\t" + n.getMarkers().size() + "\t" + n.getTranscripts().size() + "\t" +n.getReactions().size() +"\t" + dia.calculateScore(n) + "\t" + sow.calculateScore(n));

		}

	}

	private static void testTabularDataReader() throws Exception {
		MetabolicNetwork network = new LoadNetwork("/home/manuel/marvis-graph-paper-data/graph.aracyc.cut25.cut10.xml.gz").load();

		// Metabolic data
		CsvDataReader r1 = new CsvDataReader(new File("/home/manuel/marvis-graph-paper-data/mg-metabolic-data.cut25.csv"));
		ImportMetabolicMarker process1 = new ImportMetabolicMarker(network, r1);
		process1.setIdColumn(1);
		process1.setRetentiontimeColumn(2);
		process1.setMassColumn(3);
		process1.setFirstIntensityColumn(4);
		process1.setIntensityMapping(new String[]{"1", "1", "1", "2"});
		process1.doTask();
		
		// Metabolic data
		ExcelDataReader r2 = new ExcelDataReader(new File("/home/manuel/marvis-graph-paper-data/e-atmx-9.kw.cut10.aracyc_ids.xls"));
		ImportTranscriptomics process2 = new ImportTranscriptomics(network, r1);
		process2.setIdColumn(1);
		process2.setAnnotationColumn(2);
		process2.setFirstIntensityColumn(3);
		process2.setIntensityMapping(new String[]{"1", "1", "1", "2"});

		process2.doTask();

	}

	private static void matchGenes() throws Exception {

		MetabolicNetwork n_ara = new LoadNetwork("/home/manuel/marvis-graph-paper-data/graph.aracyc.combined.cut10.xml.gz").load();
		n_ara = new ReduceNetwork(n_ara).perform();
		MetabolicNetwork n_ath = new LoadNetwork("/home/manuel/marvis-graph-paper-data/graph.ath.combined.cut10.xml.gz").load();
		n_ath = new ReduceNetwork(n_ath).perform();


		BufferedReader in = new BufferedReader(new FileReader("heilmann_ids.lst"));
		String line;
		int count_ara = 0, count_ath = 0;

		while ((line = in.readLine()) != null) {

			boolean in_ara = n_ara.getGene(line) != null;
			if (in_ara) {
				count_ara++;
			}
			boolean in_ath = n_ath.getGene("ath:" + line) != null;
			if (in_ath) {
				count_ath++;
			}
			if (in_ara || in_ath) {
				System.out.println(line + "\t" + in_ara + "\t" + in_ath);
			}

		}
		System.out.println("In AraCyc:  " + count_ara);
		System.out.println("In AthKEGG: " + count_ath);

		MetabolicNetworkReport t_ara = new MetabolicNetworkReport(n_ara);
		MetabolicNetworkReport t_ath = new MetabolicNetworkReport(n_ath);

		//	System.out.println("Compounds : " + n_ara.getCompounds().size() + " with " + t_ara.countCompoundsWithMarker() + " annotated");
		//	System.out.println("Compounds : " + n_ath.getCompounds().size() + " with " + t_ath.countCompoundsWithMarker() + " annotated");
	}

	private static MetabolicNetwork testBiocycImport() throws Exception {
		File in = new File("/home/manuel/ara.tar.gz");
		BiocycCreateNetworkProcess process = new BiocycCreateNetworkProcess(in);
		MetabolicNetwork network = process.doTask();

		MetabolicNetworkReport tester = new MetabolicNetworkReport(network);
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
