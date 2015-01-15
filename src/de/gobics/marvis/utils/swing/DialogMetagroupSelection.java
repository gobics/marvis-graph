package de.gobics.marvis.utils.swing;

import de.gobics.marvis.utils.Metagroup;
import de.gobics.marvis.utils.ReferenceDatabase;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class DialogMetagroupSelection extends DialogCentered implements ActionListener {

	private static final Logger logger = Logger.getLogger(DialogMetagroupSelection.class.
			getName());
	private final JTree tree_view = new JTree();
	private final MetagroupTreeModel tree_model;
	private final JButton button_ok = new JButton("OK"),
			button_cancel = new JButton("Cancel");
	private JButton last_clicked_button = null;
	private final JScrollPane spane;

	public DialogMetagroupSelection(Window parent, Metagroup[] metagroups) {
		this(parent, metagroups, null);
	}

	public DialogMetagroupSelection(Window parent, Metagroup[] metagroups, Metagroup[] last_selection) {
		this(parent, metagroups, last_selection, "Select metagroups", "Metagroups");
	}

	public DialogMetagroupSelection(Window parent, Metagroup[] metagroups, Metagroup[] last_selection, String window_title, String border_title) {
		super(parent, DEFAULT_MODALITY_TYPE);
		setTitle(window_title);

		tree_model = new MetagroupTreeModel(metagroups);
		tree_view.setModel(tree_model);
		tree_view.setRootVisible(false);
		tree_view.setShowsRootHandles(true);
		tree_view.setCellRenderer(new CheckRenderer());
		tree_view.setSelectionModel(new MetagroupTreeSelectionModel());

		tree_view.setExpandsSelectedPaths(true);

		JPanel main = new JPanel(new BorderLayout());
		this.add(main);
		JPanel panel_sp = new JPanel(new BorderLayout());
		panel_sp.setBorder(BorderFactory.createTitledBorder(border_title));
		main.add(panel_sp, BorderLayout.CENTER);

		spane = new JScrollPane(tree_view);
		spane.setPreferredSize(new Dimension(400, 300));
		panel_sp.add(spane, BorderLayout.CENTER);

		JPanel button_panel = new JPanel();
		main.add(button_panel, BorderLayout.PAGE_END);
		button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.LINE_AXIS));
		button_panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		button_panel.add(Box.createHorizontalGlue());
		button_panel.add(button_cancel);
		button_panel.add(Box.createHorizontalGlue());
		button_panel.add(button_ok);
		button_panel.add(Box.createHorizontalGlue());

		button_ok.addActionListener(this);
		button_ok.setPreferredSize(button_cancel.getPreferredSize());
		button_cancel.addActionListener(this);

		if (last_selection != null) {
			setSelectedMetagroups(last_selection);
		}
		pack();
	}

	protected TreeSelectionModel getSelectionModel() {
		return tree_view.getSelectionModel();
	}

	public void setSelectedMetagroups(Metagroup[] selection) {
		if (selection == null) {
			return;
		}

		tree_view.getSelectionModel().clearSelection();

		for (Metagroup metagroup : selection) {
			tree_view.getSelectionModel().addSelectionPath(tree_model.getPathFor(metagroup));
		}
	}

	public TreeSet<Metagroup> getSelectedMetagroups() {
		TreePath[] paths = tree_view.getSelectionPaths();
		TreeSet<Metagroup> metagroups = new TreeSet<Metagroup>();

		for (TreePath path : paths) {
			Object select = path.getLastPathComponent();
			if (select instanceof Metagroup) {
				metagroups.add((Metagroup) select);
			}
		}

		return metagroups;
	}

	public String[] getSelectedMetagroupIds() {
		TreeSet<Metagroup> mgs = getSelectedMetagroups();
		String[] ids = new String[mgs.size()];
		int idx = 0;
		for (Metagroup m : mgs) {
			ids[idx++] = m.getIdFull();
		}
		return ids;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(button_ok)) {
			last_clicked_button = button_ok;
			dispose();
		}
		else if (e.getSource().equals(button_cancel)) {
			last_clicked_button = button_cancel;
			dispose();
		}
		else {
			throw new RuntimeException("Can not handle performed action: " + e);
		}
	}

	@Override
	public void setVisible(boolean status) {
		if (!isVisible() && status) {
			last_clicked_button = null;
		}
		super.setVisible(status);
	}

	public boolean closedWithOkButton() {
		return button_ok.equals(last_clicked_button);
	}

	public static void main(String[] args) {
		Metagroup[] mgs = new Metagroup[22];
		for (int i = 1; i < mgs.length; i++) {
			mgs[i] = new Metagroup("mg" + i, new ReferenceDatabase("db1"));
		}

		mgs[  0] = new Metagroup("mgA", new ReferenceDatabase("db2"));
		Metagroup[] selection = new Metagroup[]{new Metagroup("mg1", new ReferenceDatabase("db1"))};


		DialogMetagroupSelection d = new DialogMetagroupSelection(null, mgs, selection);
		d.setModalityType(ModalityType.APPLICATION_MODAL);
		d.setVisible(true);

	}

	public static String[] show(String[] mg_ids, String[] mg_des) {
		return show(mg_ids, mg_des, null);
	}

	public static String[] show(String[] mg_ids, String[] mg_des, String[] last_selection) {
		// Mimic some unvisible frame
		return show(new JFrame(), mg_ids, mg_des, last_selection);
	}

	public static String[] show(Window parent, String[] mg_ids, String[] mg_des, String[] last_selection) {
		return show(parent, mg_ids, mg_des, last_selection, TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	public static String[] show(Window parent, String[] mg_ids, String[] mg_des, String[] last_selection, int treeSelectionModel_SelectionMode) {

		if (mg_ids == null) {
			throw new NullPointerException("Array of metagroup ids can not be null");
		}
		if (mg_des == null) {
			mg_des = mg_ids;
		}

		if (mg_ids.length != mg_des.length) {
			throw new RuntimeException("Length of id array does not equal length of description array");
		}

		if (last_selection == null) {
			last_selection = new String[0];
		}

		Metagroup[] metagroups = convertStringsToMetagroup(mg_ids, mg_des);
		Metagroup[] preselection = convertStringsToMetagroup(last_selection, null);


		Metagroup[] selected = show(parent, metagroups, preselection, treeSelectionModel_SelectionMode);
		if (selected == null) {
			return null;
		}

		String[] ids = new String[selected.length];

		for (int i = 0;
				i < selected.length;
				i++) {
			ids[i] = selected[i].getIdFull();
		}

		return ids;
	}

	public static Metagroup[] convertStringsToMetagroup(String[] mg_ids, String[] mg_des) {
		if (mg_ids == null || mg_ids.length == 0) {
			return new Metagroup[0];
		}
		Metagroup[] metagroups = new Metagroup[mg_ids.length];

		for (int idx = 0;
				idx < mg_ids.length;
				idx++) {
			String[] tokens = mg_ids[idx].split("/");
			if (tokens.length != 2) {
				throw new RuntimeException("Can not parse full metagroup id to database and metagroup simple id: " + mg_ids[idx]);

			}
			metagroups[idx] = new Metagroup(tokens[1], new ReferenceDatabase(tokens[0]));
			if (mg_des != null) {
				metagroups[idx].setDescription(mg_des[idx]);
			}
		}

		return metagroups;
	}

	public static Metagroup[] show(Window parent, Metagroup[] metagroups) {
		return show(parent, metagroups, new Metagroup[0], TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	public static Metagroup[] show(Window parent, Metagroup[] metagroups, Metagroup[] preselection, int treeSelectionModel_SelectionMode) {
		return show(parent, metagroups, preselection, treeSelectionModel_SelectionMode, "Select Metagroups");
	}

	public static Metagroup[] show(Window parent, Metagroup[] metagroups, Metagroup[] preselection, int treeSelectionModel_SelectionMode, String title) {

		DialogMetagroupSelection dms = new DialogMetagroupSelection(parent, metagroups);
		dms.setTitle(title);
		if (preselection != null) {
			dms.setSelectedMetagroups(preselection);
		}
		dms.getSelectionModel().setSelectionMode(treeSelectionModel_SelectionMode);
		dms.setVisible(true);

		if (!dms.closedWithOkButton()) {
			return null;
		}
		return dms.getSelectedMetagroups().toArray(new Metagroup[0]);
	}
}

class MetagroupTreeModel implements TreeModel {

	private static final Logger logger = Logger.getLogger(MetagroupTreeModel.class.
			getName());
	/**
	 * Dummy object to be used as root of the tree.
	 */
	private static Object model_root = new Object();
	/**
	 * Stores the tree data
	 */
	private final TreeMap<ReferenceDatabase, LinkedList<Metagroup>> treedata = new TreeMap<ReferenceDatabase, LinkedList<Metagroup>>();
	private final HashSet<TreeModelListener> listener = new HashSet<TreeModelListener>();

	/**
	 * Create a new model for the given metagroups.
	 * @param metagroups 
	 */
	public MetagroupTreeModel(Metagroup[] metagroups) {
		for (int idx = 0; idx < metagroups.length; idx++) {
			if (!treedata.containsKey(metagroups[idx].getReferenceDatabase())) {
				treedata.put(metagroups[idx].getReferenceDatabase(), new LinkedList<Metagroup>());
			}

			treedata.get(metagroups[idx].getReferenceDatabase()).add(metagroups[idx]);
		}
	}

	public Object getRoot() {
		return model_root;
	}

	public Object getChild(Object parent, int index) {
		// If the parent is the model root return the corresponding reference databases
		if (parent.equals(model_root)) {
			return treedata.keySet().toArray()[index];
		}
		else if (parent instanceof ReferenceDatabase) {
			return treedata.get(parent).toArray()[index];
		}
		else if (parent instanceof Metagroup) {
			return new Object();
		}
		else {
			throw new RuntimeException("Can not determine the cild of '" + parent + "' at index " + index);
		}
	}

	public int getChildCount(Object parent) {
		if (parent.equals(model_root)) {
			return treedata.keySet().size();
		}
		else if (parent instanceof ReferenceDatabase) {
			return treedata.get(parent).size();
		}
		else if (parent instanceof Metagroup) {
			return 0;
		}
		else {
			throw new RuntimeException("Can not determine the number of cildren for: " + parent);
		}
	}

	public boolean isLeaf(Object node) {
		return node instanceof Metagroup;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent.equals(model_root)) {
			return Arrays.binarySearch(treedata.keySet().toArray(), child);
		}
		else if (parent instanceof ReferenceDatabase) {
			return treedata.get(parent).indexOf(child);
		}
		else if (parent instanceof Metagroup) {
			return -1;
		}
		else {
			throw new RuntimeException("Can not determine the index of cildren '" + child + " at node: " + parent);
		}
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		;
	}

	public void addTreeModelListener(TreeModelListener l) {
		listener.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listener.remove(l);

	}

	public TreePath getPathFor(Metagroup mg) {
		if (mg == null) {
			return null;
		}
		return new TreePath(new Object[]{this.model_root, mg.
					getReferenceDatabase(), mg});
	}
}

class CheckRenderer implements TreeCellRenderer {

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		JComponent retval;
		if (value instanceof ReferenceDatabase) {
			ReferenceDatabase rdb = (ReferenceDatabase) value;
			JLabel panel = new JLabel(rdb.getLabel() != null ? rdb.getLabel() : rdb.
					getId());
			retval = panel;
		}
		else if (value instanceof Metagroup) {
			Metagroup mg = (Metagroup) value;
			JCheckBox checkbox = new JCheckBox(mg.getDescription() != null ? mg.
					getDescription() : mg.getId());
			checkbox.setEnabled(tree.isEnabled());
			checkbox.setSelected(isSelected);
			checkbox.setBackground(tree.getBackground());
			retval = checkbox;
		}
		else {
			retval = new JLabel(value.toString());
		}
		retval.setBackground(tree.getBackground());
		return retval;
	}
}

/**
 * Implementation of a TreeSelectionModel. The default selection model removes
 * selections if some parent of the path to the selected object is collapsed. 
 * We like to keep the selections in place even if the parent is collapsed.
 * @author manuel
 */
class MetagroupTreeSelectionModel implements TreeSelectionModel {

	private int selection_mode = DISCONTIGUOUS_TREE_SELECTION;
	private RowMapper row_mapper = null;
	private final TreeSet<TreePath> selected = new TreeSet<TreePath>(new TreePathComparator());
	private final TreeSet<TreeSelectionListener> selection_listener = new TreeSet<TreeSelectionListener>();

	public void setSelectionMode(int mode) {
		selection_mode = mode;
	}

	public int getSelectionMode() {
		return selection_mode;
	}

	public void setSelectionPath(TreePath path) {
		addSelectionPath(path);
	}

	public void setSelectionPaths(TreePath[] paths) {
		for (TreePath p : paths) {
			addSelectionPath(p);
		}
	}

	public void addSelectionPath(TreePath path) {
		if (path == null) {
			return;
		}
		addSelectionPaths(new TreePath[]{path});
	}

	public void addSelectionPaths(TreePath[] paths) {
		//System.out.println("addSelectionPaths("+Arrays.toString(paths)+")");
		for (TreePath path : paths) {
			if (path.getLastPathComponent() instanceof Metagroup) {
				if (selection_mode != DISCONTIGUOUS_TREE_SELECTION) {
					selected.clear();
				}

				if (selected.contains(path)) {
					//System.out.println("Removing path: "+path);
					selected.remove(path);
				}
				else {
					//System.out.println("Adding path: "+path);
					selected.add(path);
				}
			}
		}
		notifyTreeSelectionListner(paths);
	}

	public void removeSelectionPath(TreePath path) {
		//System.out.println("removeSelectionPath("+path+")");
		addSelectionPath(path);
	}

	public void removeSelectionPaths(TreePath[] paths) {
		/* This is called when the tree collapses but we want the selections to
		 * stay thus we need ignore the call
		for (TreePath p : paths) {
		addSelectionPath(p);
		}*/
	}

	public TreePath getSelectionPath() {
		return selected.first();
	}

	public TreePath[] getSelectionPaths() {
		return selected.toArray(new TreePath[0]);
	}

	public int getSelectionCount() {
		return selected.size();
	}

	public boolean isPathSelected(TreePath path) {
		return selected.contains(path);
	}

	public boolean isSelectionEmpty() {
		return selected.isEmpty();
	}

	public void clearSelection() {
		selected.clear();
	}

	public void setRowMapper(RowMapper newMapper) {
		row_mapper = newMapper;
	}

	public RowMapper getRowMapper() {
		return row_mapper;

	}

	public int[] getSelectionRows() {
		return getRowMapper().getRowsForPaths(selected.toArray(new TreePath[selected.size()]));
	}

	public int getMinSelectionRow() {
		int[] rows = getSelectionRows();
		int min = Integer.MAX_VALUE;
		for (int i : rows) {
			if (i < min) {
				min = i;
			}
		}
		return min;
	}

	public int getMaxSelectionRow() {
		int[] rows = getSelectionRows();
		int min = Integer.MIN_VALUE;
		for (int i : rows) {
			if (i > min) {
				min = i;
			}
		}
		return min;
	}

	public boolean isRowSelected(int row) {
		for (int i : getSelectionRows()) {
			if (i == row) {
				return true;
			}
		}
		return false;
	}

	public void resetRowSelection() {
		// Can be ignored as row selection will calculated on the fly
	}

	public int getLeadSelectionRow() {
		return row_mapper.getRowsForPaths(new TreePath[]{getLeadSelectionPath()})[0];
	}

	public TreePath getLeadSelectionPath() {
		if (selected.isEmpty()) {
			return null;
		}
		return selected.last();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public void addTreeSelectionListener(TreeSelectionListener x) {
		selection_listener.add(x);
	}

	public void removeTreeSelectionListener(TreeSelectionListener x) {
		selection_listener.remove(x);
	}

	private void notifyTreeSelectionListner(TreePath[] changedPaths) {
		TreeSelectionEvent event = new TreeSelectionEvent(
				this,
				changedPaths,
				new boolean[changedPaths.length],
				getLeadSelectionPath(),
				getLeadSelectionPath());
		for (TreeSelectionListener l : selection_listener) {
			//System.out.println("NOTIFY: " + l + " for " + event);
			l.valueChanged(event);
		}
	}
}

class TreePathComparator implements Comparator<TreePath> {

	public int compare(TreePath o1, TreePath o2) {
		if (o1.getLastPathComponent() instanceof Metagroup
				&& o2.getLastPathComponent() instanceof Metagroup) {
			return ((Metagroup) o1.getLastPathComponent()).compareTo((Metagroup) o2.
					getLastPathComponent());
		}

		if (o1.getLastPathComponent() instanceof String
				&& o2.getLastPathComponent() instanceof Metagroup) {
			int c = o1.getLastPathComponent().toString().compareTo(o2.
					getParentPath().getLastPathComponent().toString());
			if (c == 0) {
				return -1;
			}
			return c;
		}
		if (o1.getLastPathComponent() instanceof Metagroup
				&& o2.getLastPathComponent() instanceof String) {
			int c = o1.getLastPathComponent().toString().compareTo(o2.
					getParentPath().getLastPathComponent().toString());
			if (c == 0) {
				return 1;
			}
			return c;
		}
		return -1;
	}
}
