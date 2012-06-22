package de.gobics.marvis.graph;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compare the names of conditions. 
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class ConditionComparator implements Comparator<String> {

	/**
	 * {@inheritDoc }
	 */
	@Override
	public int compare(String t1, String t2) {
		String[] t1_token = t1.split("_");
		String[] t2_token = t2.split("_");

		if (t1_token.length != 2 || t2_token.length != 2) {
			return t1.compareTo(t2);
		}

		if (t1_token[0].equals("wt") && !t2_token[0].equals("wt")) {
			return -1;
		}

		if (!t1_token[0].equals("wt") && t2_token[0].equals("wt")) {
			return 1;
		}

		Pattern p = Pattern.compile("^(\\d+)(\\w+)$");
		int m1 = 0, m2 = 0;
		Matcher matcher1 = p.matcher(t1_token[1]);
		Matcher matcher2 = p.matcher(t2_token[1]);

		if (!matcher1.matches() || !matcher2.matches()) {
			return t1.compareTo(t2);
		}

		if (matcher1.group(2).equals("h")) {
			m1 = new Double(matcher1.group(1)).intValue() * 60;
		}
		else {
			m1 = new Double(matcher1.group(1)).intValue();
		}
		if (matcher2.group(2).equals("h")) {
			m2 = new Double(matcher2.group(1)).intValue() * 60;
		}
		else {
			m2 = new Double(matcher2.group(1)).intValue();
		}

		return new Integer(m1).compareTo(m2);
	}
}
