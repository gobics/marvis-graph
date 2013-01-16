package de.gobics.marvis.graph;

import de.gobics.marvis.graph.gui.tasks.CalculateNetworksRWR;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.utils.LoggingUtils;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

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
	LoadNetwork loader = new LoadNetwork("/home/manuel/marvis-graph/testdata/wound1/wound1.complete.cut5000.intNames.xml.gz");
	MetabolicNetwork network = loader.load();

	CalculateNetworksRWR process = new CalculateNetworksRWR(network);
	
	int size = process.calculateInitialScores().values().size();
	Double[] values1 = process.calculateInitialScores(/*false*/).values().toArray(new Double[size]);
	Double[] values2 = process.calculateInitialScores(/*true*/).values().toArray(new Double[size]);

	System.out.println(Arrays.toString(values1));


	double[] v1 = new double[values1.length];
	double[] v2 = new double[values1.length];

	for (int i = 0; i < values1.length; i++) {
	    v1[i] = values1[i].doubleValue();
	    v2[i] = values2[i].doubleValue();
	}
	PearsonsCorrelation cor = new PearsonsCorrelation();
	double corval = cor.correlation(v1, v2);

	System.out.println("Correlation: " + corval);

    }
}