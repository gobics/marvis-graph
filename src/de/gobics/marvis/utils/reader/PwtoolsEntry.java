/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.reader;

/**
 *
 * @author manuel
 */
public class PwtoolsEntry extends AbstractEntry {

	public PwtoolsEntry(String entry) {
		super(entry);
	}

	protected void parseContent() {
		String[] lines = getContent().split("\n");
		for (int line_idx = 0; line_idx < lines.length; line_idx++) {
			String[] token = lines[line_idx].split(" - ");
			String tag = token[0].toLowerCase();

			// Concatenate content
			StringBuilder content = new StringBuilder();
			for (int i = 1; i < token.length; i++) {
				content.append(token[i]);
			}

			while (line_idx + 1 < lines.length && lines[line_idx + 1].startsWith("/")) {
				content.append("\n").append(lines[line_idx + 1].substring(1));
				line_idx++;
			}

			add(tag, content.toString());
		}
	}

	@Override
	public String getId() {
		return getAsLine("UNIQUE-ID");
	}

}
