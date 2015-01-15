/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils;

/**
 *
 * @author manuel
 */
public class DoubleUtils {

	public static double crop(double value, int decimal_places){
		double scale = Math.pow(10, decimal_places);
		return Math.round(value*scale)/scale;
	}

}
