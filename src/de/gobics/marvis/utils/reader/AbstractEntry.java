package de.gobics.marvis.utils.reader;

import de.gobics.marvis.utils.StringUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Represents an abstract entry in some kind of database dump file
 *
 * @author manuel
 */
public abstract class AbstractEntry {

	private final String content;

	public AbstractEntry(String c) {
		if (c != null) {
			content = c;
		}
		else {
			content = "";
		}
		parseContent();
	}

	public boolean equals(AbstractEntry other) {
		String[] mytags = getTags();
		String[] othertags = other.getTags();
		Arrays.sort(mytags);
		Arrays.sort(othertags);

		if (!Arrays.equals(mytags, othertags)) {
			return false;
		}

		for (String tag : mytags) {
			String[] mycontent = get(tag);
			String[] othercontent = other.get(tag);
			Arrays.sort(mycontent);
			Arrays.sort(othercontent);
			if (!Arrays.equals(mycontent, othercontent)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Return the content from which the entry has been created.
	 *
	 * @return
	 */
	public String getContent() {
		return content;
	}
	/**
	 * Stores the entries as 1:n mapping. One "tag" can be mapped to zero or
	 * more content string
	 */
	private TreeMap<String, LinkedList<String>> entries = new TreeMap<String, LinkedList<String>>();

	/**
	 * Returns the unique id of this entry
	 *
	 * @return the id of this entry
	 */
	public abstract String getId();

	/**
	 * Returns true if the given tag is available in this entry
	 *
	 * @param tag
	 * @return
	 */
	public boolean hasTag(String tag) {
		return entries.containsKey(tag.toLowerCase());
	}

	/**
	 * Returns an array of tags in this entry
	 *
	 * @return
	 */
	public String[] getTags() {
		return entries.keySet().toArray(new String[entries.size()]);
	}

	/**
	 * Get the contents to which the tag is mapped
	 *
	 * @param tag
	 * @return
	 */
	public final String[] get(String tag) {
		tag = tag.toLowerCase();

		LinkedList<String> content = entries.get(tag);
		if (content == null) {
			return null;
		}
		return content.toArray(new String[content.size()]);
	}

	/**
	 * Concatenates the given contents of the given tags in the order specified.
	 *
	 * @param tags
	 * @return
	 */
	public final String[] get(String[] tags) {
		LinkedList<String> contents = new LinkedList<String>();

		if (tags != null && tags.length > 0) {
			for (String tag : tags) {
				if (hasTag(tag)) {
					contents.addAll(Arrays.asList(get(tag)));
				}
			}
		}

		return contents.toArray(new String[contents.size()]);
	}

	public final String getFirst(String tag) {
		String[] contents = get(tag);
		if (contents != null && contents.length > 0) {
			return contents[0];
		}
		return null;
	}

	public final String getFirst(String[] tags) {
		String[] contents = get(tags);
		if (contents != null && contents.length > 0) {
			return contents[0];
		}
		return null;
	}

	/**
	 * Returns the contents of a tag as concatenated string. The contents are
	 * separated by a simple newline
	 *
	 * @param tag
	 * @return
	 */
	public String getAsString(String tag) {
		return getAsString(tag, "\n");
	}

	public String getAsString(String[] tags) {
		return getAsString(tags, "\n");
	}

	/**
	 * Returns the contents of a tag as concatenated string. The contents are
	 * separated by the given separator string.
	 *
	 * @param tag
	 * @param separator
	 * @return
	 */
	public String getAsString(String tag, String separator) {
		String[] content = get(tag);
		if (content == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (String s : content) {
			sb.append(s).append(separator);
		}
		return sb.toString().substring(0, sb.length() - separator.length());
	}

	/**
	 * Equals to <code>getAsString(tags, "\n")</code>
	 * @param tags
	 * @param separator
	 * @return 
	 */
	public String getAsString(String[] tags, String separator) {
		return StringUtils.join(separator, get(tags));
	}

	/**
	 * Returns the contents as a line (replacing newlines with "; "). This is
	 * equivalent to {@code getAsString(tag, "; ")}.
	 *
	 * @param tag
	 * @return
	 */
	public String getAsLine(String tag) {
		return getAsLine(new String[]{tag});
	}

	public String getAsLine(String[] tags) {
		String as = getAsString(tags, "; ");
		return as != null ? as.replaceAll("\n", "") : null;
	}

	/**
	 * Similar to getAsString() but starts at line {@code startidx}.
	 *
	 * @param tag
	 * @param separator
	 * @param startidx
	 * @return
	 */
	public String getAsString(String tag, String separator, int startidx) {
		return getAsString(tag, separator, startidx, entries.get(tag.toLowerCase()).
				size() - 1);
	}

	/**
	 * Similar to getAsString() but starts at line {@code startidx} and stops
	 * including line {@code endidx}.
	 *
	 * @param tag
	 * @param separator
	 * @param startidx
	 * @param endidx
	 * @return
	 */
	public String getAsString(String tag, String separator, int startidx, int endidx) {
		return getAsString(new String[]{tag}, separator, startidx, endidx);
	}

	public String getAsString(String[] tags, String separator, int startidx, int endidx) {
		StringBuilder buffer = new StringBuilder();

		String[] contents = get(tags);

		for (int idx = startidx; idx <= endidx; idx++) {
			buffer.append(contents[idx]);
			if (idx < endidx) {
				buffer.append(separator);
			}
		}

		return buffer.toString();
	}

	/**
	 * Set the contents of the given tag (Note: add() is preferred)
	 *
	 * @param tag
	 * @param content
	 */
	protected final void set(String tag, String content) {
		tag = tag.toLowerCase();
		entries.put(tag.toLowerCase(), new LinkedList<String>());
		entries.get(tag.toLowerCase()).add(content);
	}

	/**
	 * Set the contents of the given tag (Note: add() is preferred)
	 *
	 * @param tag
	 * @param content
	 */
	protected final void set(String tag, Collection<String> content) {
		entries.put(tag.toLowerCase(), new LinkedList<String>(content));
	}

	/**
	 * Add the content to the contents of the given tags.
	 *
	 * @param tag
	 * @param content
	 */
	protected final void add(String tag, String content) {
		tag = tag.toLowerCase();

		if (!entries.containsKey(tag)) {
			entries.put(tag, new LinkedList<String>());
		}
		entries.get(tag).add(content);
	}

	/**
	 * Add the contents to the contents of the given tags.
	 *
	 * @param tag
	 * @param content
	 */
	protected final void add(String tag, Collection<String> content) {
		tag = tag.toLowerCase();

		if (!entries.containsKey(tag)) {
			entries.put(tag, new LinkedList<String>(content));
		}
		else {
			entries.get(tag).addAll(content);
		}
	}

	protected abstract void parseContent();

	/**
	 * Write the data to System.out
	 */
	public void dump() {
		System.out.println("This is the " + getClass().getSimpleName() + " entry for: " + getId());
		for (String t : getTags()) {
			System.out.println(t + " => " + Arrays.toString(get(t)));
		}
	}
}
