package de.gobics.marvis.utils.swing.filechooser;

public class FileFilterExcel extends FileFilterAbstract {

	@Override
	public String getDescriptionName() {
		return "Microsoft Excel 97/2000/XP";
	}

	@Override
	public String[] getDefaultExtensions() {
		return new String[]{"xls"};
	}
}
