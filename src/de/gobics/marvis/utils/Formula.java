package de.gobics.marvis.utils;

import de.gobics.marvis.utils.exception.ChemicalElementUnkownException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Represents a sum formula
 *
 * @author manuel
 */
public class Formula implements Comparable<Formula> {

	private static final Logger logger = Logger.getLogger(Formula.class.getName());
	private static final Pattern formula_split_first = Pattern.compile("(?<=[a-zA-Z\\d])\\s*(?=[A-Z])");
	private static final Pattern formula_split_second = Pattern.compile("(?<=[a-zA-Z])(?=\\d)");
	public static final String[] known_elements = new String[]{"H", "He", "Li", "Be", "B", "C",
		"N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K",
		"Ca", "Mn", "Fe", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb",
		"Sr", "Ag", "In", "Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "Tl", "Pb", "Bi", "Co", "Cd", "Mo"};
	public static final double[] known_masses = new double[]{1.007825,
		4.002603, 7.016005, 9.012183, 11.009305, 12.000000, 14.003074, 15.994915,
		18.998403, 19.992439, 22.989770, 23.985045, 26.981541, 27.976928, 30.973763,
		31.972072, 34.968853, 39.962383, 38.963708, 39.962591, 54.938046, 55.934939,
		62.929599, 63.929145, 68.925581, 73.921179, 74.921596, 79.916521, 78.918336,
		83.911506, 84.911800, 87.905625, 106.905095, 114.903875, 119.902199,
		120.903824, 129.906229, 126.904477, 131.904148, 132.905433, 137.905236,
		204.974410, 207.976641, 208.980388, 58.933198, 113.903361, 97.905405};
	public static final int[] known_valences = new int[]{1, 4, 1, 2, 3, 4, 3, 2, 4, 4, 1, 2, 3, 4,
		5, 6, 5, 4, 1, 2, 2, 2, 2, 2, 4, 4, 5, 6, 5, 4, 1, 3, 3, 4, 4, 5, 6, 7,
		6, 4, 4, 3, 4, 5, 2, 2, 6};
	/**
	 * The string representation of the formula
	 */
	protected final String as_string;
	/**
	 * The exact mass
	 */
	protected final double mass;
	/** All elements in the formula ordered by a {@link ElementSorter}.
	 */
	private final String[] elements;
	/** The corresponding amount of elements in the formula.
	 */
	private final int[] count;

	public Formula() {
		as_string = "";
		mass = 0d;
		elements = new String[0];
		count = new int[0];
	}

	/** New formula with the given elements and their corresponding count.
	 *
	 * @param elements
	 * @param count
	 */
	public Formula(String[] elements, int[] count) throws ChemicalElementUnkownException {
		TreeMap<String, Integer> map = new TreeMap<String, Integer>(new ElementSorter());
		String f_string = "";
		double f_mass = 0d;

		if (elements.length != count.length) {
			throw new RuntimeException("Element and count length does not equal");
		}

		for (int idx = 0; idx < elements.length; idx++) {
			if (indexOf(elements[idx]) < 0) {
				throw new ChemicalElementUnkownException(elements[idx]);
			}
			map.put(elements[idx], count[idx]);
		}

		this.count = new int[elements.length];
		this.elements = new String[elements.length];

		int i = 0;
		for (String e : map.keySet()) {
			this.elements[i] = e;
			this.count[i] = map.get(e);

			if (count[i] > 0) {
				f_string += e + count[i] + " ";
				f_mass += count[i] * getMass(e);
			}
			i++;
		}

		this.as_string = f_string.substring(0, f_string.length() - 1);
		this.mass = f_mass;
	}

	/**
	 * Parse the string and create a new formula from it.
	 * @param fs String representation of the formula
	 * @return may return {@code null} on failure
	 */
	public static Formula createFormulaFromString(String fs) throws ChemicalElementUnkownException {
		if (fs.isEmpty()) {
			throw new RuntimeException("Given formula string is empty");
		}

		TreeMap<String, Integer> foo = splitFormula(fs);

		String[] elements = foo.keySet().toArray(new String[0]);
		Integer[] icount = foo.values().toArray(new Integer[0]);
		int[] count = new int[icount.length];
		for (int i = 0; i < icount.length; i++) {
			if (indexOf(elements[i]) == -1) {
				throw new ChemicalElementUnkownException(elements[i]);
			}
			count[i] = icount[i].intValue();
		}

		return new Formula(elements, count);
	}

	public static Formula createFormulaFromTreemap(TreeMap<String, Integer> definition) throws ChemicalElementUnkownException {
		TreeMap<String, Integer> foo = new TreeMap(new ElementSorter());
		foo.putAll(definition);

		String[] elements = foo.keySet().toArray(new String[0]);
		Integer[] count = foo.values().toArray(new Integer[0]);
		int[] icount = new int[count.length];
		for (int idx = 0; idx < count.length; idx++) {
			icount[idx] = count[idx].intValue();
		}
		return new Formula(elements, icount);
	}
	
	public static Formula createFormulaFromInChIString(String fs) throws ChemicalElementUnkownException {
		int idx1 = fs.indexOf('/')+1;
		int idx2 = fs.indexOf('/', idx1);
		if( idx1 < 0 || idx2 < 0)
			return null;
		return createFormulaFromString( fs.substring(idx1, idx2) );
	}
	

	/** Returns the mass of the formula
	 *
	 * @return
	 */
	public double getMass() {
		return mass;
	}

	/** Return the string representation of the formula.
	 *
	 * @return
	 */
	public String getAsString() {
		return as_string;
	}

	/** Returns the elements of the formula
	 * 
	 * @return
	 */
	public String[] getElements() {
		return elements.clone();
	}

	@Override
	public String toString() {
		return "Formula [" + getAsString() + ";"+getMass()+"]";
	}

	/** Returns the formula as TreeMap with a mapping from element name to
	 * count.
	 * @return
	 */
	public TreeMap<String, Integer> split() {
		return splitFormula(as_string);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Formula)) {
			return false;
		}
		return as_string.equals(((Formula) other).as_string);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.as_string != null ? this.as_string.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(Formula t) {
		return getAsString().compareTo(t.getAsString());
	}

	/** Return the numbers of the elements. Indexes of the {@link #getElements()}
	 * method  correspond to the indexes here.
	 */
	public int[] getElementCount() {
		return count;
	}

	public int getElementCount(String e) {
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].equals(e)) {
				return count[i];
			}
		}
		return 0;
	}

	public Formula addElement(String element) throws ChemicalElementUnkownException {
		return addElement(element, 1);
	}

	public Formula addElement(String element, int count) throws ChemicalElementUnkownException {
		if (indexOf(element) < 0) {
			throw new ChemicalElementUnkownException(element);
		}

		TreeMap<String, Integer> map = split();
		if (map.containsKey(element)) {
			map.put(element, map.get(element) + count);
		} else {
			map.put(element, count);
		}

		return createFormulaFromTreemap(map);
	}

	public Formula removeElement(String element) throws ChemicalElementUnkownException {
		return addElement(element, 1);
	}

	public Formula removeElement(String element, int count) throws ChemicalElementUnkownException {
		if (indexOf(element) < 0) {
			throw new ChemicalElementUnkownException(element);
		}

		TreeMap<String, Integer> map = split();
		if (map.containsKey(element)) {
			map.put(element, map.get(element) - count);
		}


		return createFormulaFromTreemap(map);
	}

	/**
	 * Returns the mass of the element if the element is known. Will return -1 on failure.
	 * @param element
	 * @return
	 */
	public static double getMass(String element) {
		int idx = indexOf(element);
		if (idx == -1) {
			return -1;
		}
		return known_masses[idx];
	}

	/**
	 * Returns the valence of the element if the element is known. Will return -1 on failure.
	 * @param element
	 * @return
	 */
	public static int getValence(String element) {
		int idx = indexOf(element);
		if (idx == -1) {
			return -1;
		}
		return known_valences[idx];
	}

	private static TreeMap<String, Integer> splitFormula(String formula) {
		TreeMap<String, Integer> els = new TreeMap<String, Integer>(new ElementSorter());
		String e = null;
		String n = null;

		while (formula.startsWith(" ")) {
			formula = formula.substring(1);
		}
		while (formula.endsWith(" ")) {
			formula = formula.substring(0, formula.length() - 1);
		}

		for (String part : formula_split_first.split(formula)) {
			String[] tks = formula_split_second.split(part);
			if (tks.length == 2) {
				els.put(tks[0], new Integer(tks[1]));
			} else {
				els.put(tks[0], 1);
			}
		}

		return els;
	}

	private static int indexOf(String e) {
		if (e == null) {
			throw new NullPointerException("Given element string is: null");
		}
		e = e.toLowerCase();
		for (int i = 0; i < known_elements.length; i++) {
			if (known_elements[i].toLowerCase().equals(e)) {
				return i;
			}
		}
		return -1;
	}

	public Formula addFormula(Formula other) {
		logger.finer("Adding " + other + " to " + this);
		if (other.isEmpty()) {
			return this;
		}
		if (this.isEmpty()) {
			return other;
		}

		Formula ret = this;
		try {
			for (String e : other.getElements()) {
				ret = ret.addElement(e, other.getElementCount(e));
			}
		} catch (ChemicalElementUnkownException ex) {
			// This can be ignored because an exception would have been thrown at
			// creation of this formula or the other
			Logger.getLogger(Formula.class.getName()).log(Level.SEVERE, null, ex);
		}
		return ret;
	}

	/**
	 * Returns true if and only if there is at least one element with a count
	 * greater than zero is in this formula.
	 * @return 
	 */
	private boolean isEmpty() {
		for (int c : getElementCount()) {
			if (c > 0) {
				return false;
			}
		}
		return true;
	}
}
