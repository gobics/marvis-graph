package de.gobics.marvis.utils.swing.filechooser;

public class FileFilterExcelX extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "Microsoft Excel 2003";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"xlsx"};
	}



}
