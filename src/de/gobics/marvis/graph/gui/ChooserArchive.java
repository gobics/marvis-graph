package de.gobics.marvis.graph.gui;

import de.gobics.marvis.utils.swing.filechooser.ChooserAbstract;
import de.gobics.marvis.utils.swing.filechooser.FileFilterXml;
import de.gobics.marvis.utils.swing.filechooser.FileFilterXmlGzip;
import java.io.File;

public class ChooserArchive extends ChooserAbstract {

	private static final ChooserArchive global_instance = new ChooserArchive();
	static final long serialVersionUID = 1;

	public ChooserArchive() {
		this.setAcceptAllFileFilterUsed(false);
		this.setFileFilter(new FileFilterXml());
		this.setFileFilter(new FileFilterXmlGzip());
	}

	public static ChooserArchive getInstance() {
		return global_instance;
	}
}