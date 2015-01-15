package de.gobics.marvis.utils.swing.filechooser;

class FileFilterZip extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "ZIP archives";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"zip"};
	}
}
