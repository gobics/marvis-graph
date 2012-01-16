
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksReaction;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.graph.gui.tasks.SortNetworksTask;
import de.gobics.marvis.graph.sort.NetworkSorterLongestShortestPath;
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
		
		
		LoadNetwork loader = new LoadNetwork("/gobics/home/manuel/marvis-graph/testdata/wound1/wound1.complete.cut5000.intNames.xml.gz");
		MetabolicNetwork network = loader.load();
		
		CalculateNetworksReaction process = new CalculateNetworksReaction(network);
		process.execute();
		MetabolicNetwork[] sub_networks = process.get();
		
		Thread.sleep(1000);
		System.out.println("Array Length: "+sub_networks.length);
		System.out.println(Arrays.toString(sub_networks));
		
		NetworkSorterLongestShortestPath sort = new NetworkSorterLongestShortestPath(network);
		SortNetworksTask sorter = new SortNetworksTask(sort, sub_networks);
		sub_networks = sorter.sortNetworks();
		
		
		Thread.sleep(1000);
		System.out.println("Array Length: "+sub_networks.length);
		System.out.println(Arrays.toString(sub_networks));
	}
}
