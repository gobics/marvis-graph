/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author manuel
 */
public class ColorUtils {

	public static enum ScalingType {

		Normal, Quadratic, Logarithm
	}

	public static Color getScaledColor(double value, double min, double max, Color min_color, Color max_color, ScalingType scaling) {
		return getScaledColor(value, min, max, min_color, max_color, getFactorFromScale(scaling));
	}

	public static double getFactorFromScale(ScalingType scale) {
		switch (scale) {
			case Quadratic:
				return 2;
			case Logarithm:
				return 0.5;
			default:
				return 1;
		}
	}

	public static Color getScaledColor(double value, double min, double max, Color min_color, Color max_color, double scaling) {

		if (min > max) {
			double tmp = max;
			max = min;
			min = tmp;
		}

		int r1 = min_color.getRed();
		int g1 = min_color.getGreen();
		int b1 = min_color.getBlue();
		int r2 = max_color.getRed();
		int g2 = max_color.getGreen();
		int b2 = max_color.getBlue();

		int color_distance = Math.abs(r1 - r2);
		color_distance += Math.abs(g1 - g2);
		color_distance += Math.abs(b1 - b2);

		// What proportion of the way through the possible values is that.
		double position = value - min;
		double percentPosition = position / Math.abs(max - min);

		// Which colour group does that put us in.
		int colourPosition = (int) Math.round(color_distance * Math.pow(percentPosition, scaling));

		int r = min_color.getRed();
		int g = min_color.getGreen();
		int b = min_color.getBlue();

		// Make n shifts of the colour, where n is the colourPosition.
		for (int i = 0; i < colourPosition; i++) {
			int rDistance = r - max_color.getRed();
			int gDistance = g - max_color.getGreen();
			int bDistance = b - max_color.getBlue();

			if ((Math.abs(rDistance) >= Math.abs(gDistance))
					&& (Math.abs(rDistance) >= Math.abs(bDistance))) {
				// Red must be the largest.
				r = changeColourValue(r, rDistance);
			}
			else if (Math.abs(gDistance) >= Math.abs(bDistance)) {
				// Green must be the largest.
				g = changeColourValue(g, gDistance);
			}
			else {
				// Blue must be the largest.
				b = changeColourValue(b, bDistance);
			}
		}

		return new Color(r, g, b);
	}

	private static int changeColourValue(int colourValue, int colourDistance) {
		if (colourDistance < 0) {
			return colourValue + 1;
		}
		else if (colourDistance > 0) {
			return colourValue - 1;
		}
		else {
			// This shouldn't actually happen here.
			return colourValue;
		}
	}

	public static List<Color> getUniqueColors(int amount) {
		List<Color> result = new ArrayList<>(amount);

		if (amount == 0) {
			// ignore
		}
		else if (amount == 1) {
			// Special case for only one color
			result.add(Color.red);
		}
		else {
			float hueMax = (float) 0.85;
			float sat = (float) 0.8;

			for (int i = 0; i < amount; i++) {
				float hue = hueMax * i / (amount - 1);

				// Here we interleave light colors and dark colors
				// to get a wider distribution of colors.
				if (i % 2 == 0) {
					result.add(Color.getHSBColor(hue, sat, (float) 0.9));
				}
				else {
					result.add(Color.getHSBColor(hue, sat, (float) 0.7));
				}
			}
		}

		return result;
	}

	public static String getHTMLColor(Color c) {
		int red = c.getRed();
		int green = c.getGreen();
		int blue = c.getBlue();
		int alpha = c.getAlpha();

		String code = toHex(red) + toHex(green) + toHex(blue);
		if (alpha != 255) {
			code += toHex(alpha);
		}
		return "#" + code;
	}
	
	private static String toHex(int i){
		String hex = Integer.toHexString(i);
		if( hex.length() < 2){
			return "0"+hex;
		}
		return hex;
	}
}
