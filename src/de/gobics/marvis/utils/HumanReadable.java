/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class HumanReadable {

	public static String bytes(long bytes) {
		if (bytes < 1024) {
			return bytes + "b";
		}
		return kilobyte((long) Math.floor(bytes / 1024));
	}

	public static String kilobyte(long kb) {
		if (kb < 1024) {
			return kb + "kb";
		}
		return megabyte((long) Math.floor(kb / 1024));
	}

	public static String megabyte(long mb) {
		if (mb < 1024) {
			return mb + "mb";
		}
		return gigabyte((long) Math.floor(mb / 1024));
	}

	public static String gigabyte(long gb) {
		if (gb < 1024) {
			return gb + "gb";
		}
		return terabyte((long) Math.floor(gb / 1024));
	}

	public static String terabyte(long tb) {
		return tb + "tb";
	}

	public static String seconds(long seconds) {
		if (seconds < 60) {
			return seconds + "sec";
		}
		long minutes = (long) Math.floor(seconds / 60);
		return minutes(minutes, seconds - minutes * 60);
	}

	public static String hours(long hours) {
		return hours(hours, 0, 0);
	}

	public static String hours(long hours, long minutes) {
		return hours(hours, minutes, 0);
	}

	public static String minutes(long minutes) {
		return minutes(minutes, 0);
	}

	public static String minutes(long minutes, long seconds) {
		if (minutes < 60) {
			if (seconds == 0) {
				return minutes + "min";
			}
			return minutes + ":" + printfd(seconds, 2) + "min";
		}
		long hours = (long) Math.floor(minutes / 60);
		return hours(hours, minutes - hours * 60, seconds);
	}

	public static String hours(long hours, long minutes, long seconds) {
		if (seconds > 0) {
			return hours + ":" + printfd(minutes, 2) + ":" + printfd(seconds, 2) + "h";
		}
		if (minutes > 0) {
			return hours + ":" + printfd(minutes, 2) + "h";
		}
		return hours + "h";
	}

	private static String printfd(long value, int length_including_zeros) {
		String v = Long.toString(value);
		if (v.length() >= length_including_zeros) {
			return v;
		}

		for (int idx = v.length(); idx < length_including_zeros; idx++) {
			v = "0" + v;
		}
		return v;
	}
}
