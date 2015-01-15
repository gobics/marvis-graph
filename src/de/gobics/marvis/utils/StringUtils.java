/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.security.MessageDigest;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author manuel
 */
public class StringUtils {

	private static final Logger logger = Logger.getLogger(StringUtils.class.
			getName());

	public static String join(String separator, String... parts) {
		Object[] os = new Object[parts.length];
		System.arraycopy(parts, 0, os, 0, parts.length);
		return join(separator, os);
	}

	public static String join(String separator, Collection<Object> parts) {
		return join(separator, parts.toArray(new Object[parts.size()]));
	}

	public static String join(String separator, List<Object> parts) {
		return join(separator, parts.toArray(new Object[parts.size()]));
	}

	public static String join(String separator, Object[] parts) {
		if (parts == null) {
			return null;
		}
		if (parts.length == 0) {
			return null;
		}
		if (separator == null) {
			separator = "";
		}

		//System.out.println(Arrays.toString(parts));
		StringBuilder sb = new StringBuilder(parts[0] != null ? parts[0].
				toString() : "NULL");
		for (int i = 1; i < parts.length; i++) {
			if (parts[i] == null) {
				sb.append(separator).append("NULL");
			}
			else {
				sb.append(separator).append(parts[i].toString());
			}
		}
		if (separator.isEmpty()) {
			return sb.toString();
		}
		return sb.toString();
	}

	public static String join(String separator, float[] parts) {
		Float[] objects = new Float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			objects[i] = new Float(parts[i]);
		}
		return join(separator, objects);
	}

	public static String join(String separator, int[] parts) {
		Integer[] objects = new Integer[parts.length];
		for (int i = 0; i < parts.length; i++) {
			objects[i] = new Integer(parts[i]);
		}
		return join(separator, objects);
	}

	public static String join(String separator, double[] parts) {
		Double[] objects = new Double[parts.length];
		for (int i = 0; i < parts.length; i++) {
			objects[i] = new Double(parts[i]);
		}

		return join(separator, objects);
	}

	public static String join_unique(String separator, Collection<Object> parts) {
		if (parts == null) {
			return null;
		}
		TreeSet<Object> objects = new TreeSet<Object>(parts);
		return join(separator, objects.toArray(new Object[objects.size()]));
	}

	public static String join_unique(String separator, Object[] parts) {
		if (parts == null) {
			return null;
		}
		return join_unique(separator, Arrays.asList(parts));
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				}
				else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String SHA1(String string) {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(string.getBytes("iso-8859-1"), 0, string.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		}
		catch (Exception ex) {
			Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static String SHA1(String[] strings) {
		return SHA1(join("", strings));
	}

	public static String ucfirst(String string) {
		if (string.isEmpty()) {
			return string;
		}
		if (string.length() == 1) {
			return string.toUpperCase();
		}
		return string.substring(0, 1).toUpperCase() + string.substring(1).
				toLowerCase();
	}

	public static String ucfirst(String string, String split) {
		if (string == null || string.isEmpty()) {
			return string;
		}
		if (string.length() == 1) {
			return string.toUpperCase();
		}
		if (split == null || split.isEmpty()) {
			return ucfirst(string);
		}

		String[] parts = string.split(split);
		for (int idx = 0; idx < parts.length; idx++) {
			parts[idx] = ucfirst(parts[idx]);
		}

		return join(split, parts);
	}

	public static Map<String, Integer> count(String[] strings) {
		Map<String, Integer> result = new TreeMap<String, Integer>();
		for (int idx = 0; idx < strings.length; idx++) {
			if (!result.containsKey(strings[idx])) {
				result.put(strings[idx], new Integer(1));
			}
			else {
				result.put(strings[idx], 1 + result.get(strings[idx]));
			}
		}
		return result;
	}
}