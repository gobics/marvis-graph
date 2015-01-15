/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.matrix;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public abstract class AbstractAnnotatedMatrix {
	private final int dimensions;
	private final TreeMap<Integer, LinkedList<Object>> labels = new TreeMap<Integer, LinkedList<Object>>();
	
	public AbstractAnnotatedMatrix(int dimensions){
		this.dimensions = dimensions;
		for(int i = 0 ; i < dimensions ; i++){
			labels.put( new Integer(i), new LinkedList<Object>());
		}
	}
	
	public void setLabel(int dimension, int index, Object label){
		if( dimension >= dimensions ){
			throw new IndexOutOfBoundsException("Given dimension is to big: "+dimension);
		}
		labels.get(dimension).set(index, label);
	}
	
	public Object getLabel(int dimension, int index){
		if( dimension >= dimensions ){
			throw new IndexOutOfBoundsException("Given dimension is to big: "+dimension);
		}
		return labels.get(dimension).get(index);
	}

}
