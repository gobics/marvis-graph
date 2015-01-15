package de.gobics.marvis.utils.reader;

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LipidmapsEntry extends AbstractEntry {

	private static final Logger logger = Logger.getLogger(LipidmapsEntry.class.getName());
	private static final Pattern match_tag_name = Pattern.compile("^> <(\\w+)>\\s*$");

	public LipidmapsEntry(String chunk) {
		super(chunk);
	}

	@Override
	public String getId() {
		return getAsLine("LM_ID");
	}

	protected void parseContent() {
		String current_tag = null;
		String[] lines = getContent().split("\n");
		int start_idx = 0;
		while (!match_tag_name.matcher(lines[start_idx]).matches()
				&& start_idx < lines.length - 1) {
			start_idx++;
		}

		logger.finer("First line to process is: " + start_idx);
		for (int idx = start_idx; idx < lines.length; idx++) {
			Matcher match = match_tag_name.matcher(lines[idx]);
			if (match.matches()) {
				current_tag = match.group(1);
				logger.finer("Found tag: "+current_tag);
				
			} else if (!lines[idx].isEmpty()) {
				add(current_tag, lines[idx]);
			}
		}
	}
}
