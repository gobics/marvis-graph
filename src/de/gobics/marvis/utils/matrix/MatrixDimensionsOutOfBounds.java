/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.utils.matrix;

import cern.colt.matrix.impl.AbstractMatrix;
import cern.colt.matrix.impl.AbstractMatrix2D;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class MatrixDimensionsOutOfBounds extends RuntimeException {
	private final String message;
	
	
	public MatrixDimensionsOutOfBounds(AbstractMatrix2D m1, AbstractMatrix2D m2){
		message = "Matrix dimensions do not aggree: "+m1.rows()+"x"+m1.columns() +" vs. "+m2.rows() +"x"+m2.columns();
	}
	
	@Override
	public String getMessage(){
		return message;
	}

}
