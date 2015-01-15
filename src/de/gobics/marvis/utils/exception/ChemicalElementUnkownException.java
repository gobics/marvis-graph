/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.exception;

/**
 *
 * @author manuel
 */
public class ChemicalElementUnkownException extends Exception {
	final String element;
	
	public ChemicalElementUnkownException(String elem){
		element=elem;
	}
	
	public String getElement(){
		return element;
	}
	
	@Override
	public String getMessage(){
		return "The chemical element '"+element+"' is unkown";
	}
}
