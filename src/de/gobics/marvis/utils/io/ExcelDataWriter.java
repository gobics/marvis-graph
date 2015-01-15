package de.gobics.marvis.utils.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Abstract writer to write data in a CSV (comma-separated-values) style to a
 * file.
 *
 * @author manuel
 */
public class ExcelDataWriter extends TabularDataWriter {

	private Workbook workbook;
	private Sheet current_sheet;

	public ExcelDataWriter(File destination) throws IOException {
		super(destination);
		if (destination.getName().toLowerCase().endsWith(".xls")) {
			workbook = new HSSFWorkbook();
		}
		else {
			workbook = new XSSFWorkbook();
		}
		current_sheet = workbook.createSheet();
	}

	private Workbook getWorkbook() {
		return workbook;
	}

	public void appendSheet() {
		current_sheet = workbook.createSheet();
	}

	private Sheet getSheet() {
		return current_sheet;
	}

	public void appendRow(String[] contents) {
		Sheet sheet = getSheet();
		Row row = sheet.createRow(sheet.getLastRowNum() + 1);

		for (int col = 0; col < contents.length; col++) {
			Cell cell = row.createCell(col);
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(contents[col]);
		}
	}

	public void appendRow(Object[] contents) {
		Sheet sheet = getSheet();
		Row row = sheet.createRow(sheet.getLastRowNum() + 1);

		for (int col = 0; col < contents.length; col++) {
			Cell cell = row.createCell(col);
			Object cur = contents[col];
			if (cur instanceof Number) {
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(((Number) cur).doubleValue());
			}
			else {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellValue(cur.toString());
			}
		}
	}

	@Override
	public void close() throws IOException {
		getWorkbook().write(new FileOutputStream(getOutputFile()));
	}
}
