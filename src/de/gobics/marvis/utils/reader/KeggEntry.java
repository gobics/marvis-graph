package de.gobics.marvis.utils.reader;

import java.util.logging.Logger;

/** Helper class to handle KEGG bget requests. Fields in the result
 * can be accessed using simple functions. There is no need to implement the
 * text-parsing of the results over and over again.
 * 
 * @author manuel
 */
public class KeggEntry extends AbstractEntry {

	private static final Logger logger = Logger.getLogger(KeggEntry.class.getName());

	/**
	 * Create a new result
	 * @param content The content of the request
	 */
	public KeggEntry(String content) {
		super(content);
	}

	/**
	 * Parse the content string and store all informations
	 * @param content
	 */
	protected void parseContent() {
		String[] lines = getContent().split("\\n");

		for (int i = 0; i < lines.length; i++) {
			if (lines[i].equals("///")
					|| lines[i].length() < 12
					|| !lines[i].contains(" ")) {
				continue;
			}

			String tag, def;
			tag = lines[i].substring(0, lines[i].indexOf(' '));
			def = lines[i].substring(12);
			// Check for concatenation of "multi-lines"
			while (i + 1 < lines.length && lines[i + 1].startsWith(" ")) {
				i++;
				if (lines[i].startsWith("           ")) {
					def += "\n" + lines[i].substring(12);
				} else {
					def += "\n" + lines[i].substring(1);
				}
			}
			add(tag, def);
		}
	}

	/**
	 * Returns the Id of the entry.
	 *
	 * @return
	 */
	public String getId() {
		String id = getAsLine("ENTRY");
		if (id == null) {
			return null;
		}

		return id.substring(0, id.indexOf(' '));
	}
}
