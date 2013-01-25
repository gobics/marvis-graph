package de.gobics.marvis.graph.gui.evaluation;

import de.gobics.marvis.graph.gui.tasks.PermutationTestResult;
import java.util.Set;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author manuel
 */
public class TableModelResults implements TableModel {

	private static final String[] COL_NAMES = new String[]{
		"Network name", "Score", "Number of permutations", "Family-Wise-Error-Rate", "False-Discovery-Rate"
	};
	private static final int COL_NAME = 0;
	private static final int COL_SCORE = 1;
	private static final int COL_PERMUTES = 2;
	private static final int COL_FWER = 3;
	private static final int COL_FDR = 3;
	private final PermutationTestResult[] results;

	public TableModelResults(PermutationTestResult[] results) {
		this.results = results;
	}

	public TableModelResults(Set<PermutationTestResult> results) {
		this(results.toArray(new PermutationTestResult[results.size()]));
	}

	@Override
	public int getRowCount() {
		return results.length;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int i) {
		return COL_NAMES[i];
	}

	@Override
	public Class<?> getColumnClass(int i) {
		if (i == COL_NAME) {
			return String.class;
		}
		if( i == COL_SCORE){
			return Comparable.class;
		}
		if (i == COL_PERMUTES) {
			return Integer.class;
		}
		return Double.class;
	}

	@Override
	public boolean isCellEditable(int i, int i1) {
		return true;
	}

	@Override
	public Object getValueAt(int i, int col) {
		if (col == COL_NAME) {
			return results[i].network.getName();
		}
		if (col == COL_SCORE) {
			return results[i].score;
		}
		if (col == COL_PERMUTES) {
			return results[i].num_permutations;
		}
		if (col == COL_FWER) {
			return results[i].fwer;
		}
		if( col == COL_FDR){
			return results[i].fdr;
		}
		throw new RuntimeException("Unkown column: " + col);
	}

	@Override
	public void setValueAt(Object o, int i, int i1) {
		//ignore
	}

	@Override
	public void addTableModelListener(TableModelListener tl) {
		//ignore
	}

	@Override
	public void removeTableModelListener(TableModelListener tl) {
		//ignore
	}
}
