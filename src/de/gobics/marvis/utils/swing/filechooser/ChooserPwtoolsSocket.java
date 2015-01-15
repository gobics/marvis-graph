/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.swing.filechooser;

import java.io.File;

/**
 *
 * @author manuel
 */
public class ChooserPwtoolsSocket extends ChooserAbstract {
	public ChooserPwtoolsSocket(){
		setAcceptAllFileFilterUsed(true);
		setSelectedFile(new File("/tmp/ptools-socket"));
	}
}
