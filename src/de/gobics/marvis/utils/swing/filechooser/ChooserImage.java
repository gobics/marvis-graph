package de.gobics.marvis.utils.swing.filechooser;

public class ChooserImage extends ChooserAbstract {

	private static final ChooserImage global_instance = new ChooserImage();

	public ChooserImage() {
		setAcceptAllFileFilterUsed(false);
		setFileFilter(new FileFilterImageJpg());
		setFileFilter(new FileFilterImagePng());
		setFileFilter(new FileFilterSVG());
	}

	public static ChooserImage getInstance() {
		return global_instance;
	}
}
