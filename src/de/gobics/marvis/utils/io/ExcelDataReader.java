package de.gobics.marvis.utils.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author manuel
 */
public class ExcelDataReader extends TabularDataReader {

	private Workbook workbook;
	private int sheet_index = 0;
	private boolean evaluate_formula = true;

	public ExcelDataReader(File input) throws IOException {
		super(input);
	}

	public int getSheet() {
		return sheet_index;
	}

	public void setSheet(int sheet) {
		this.sheet_index = sheet;
	}

	public int countSheets() throws IOException {
		return getWorkbook().getNumberOfSheets();
	}

	private Workbook getWorkbook() throws IOException {
		if (workbook == null) {
			try {
				workbook = WorkbookFactory.create(getInputFile());
			}
			catch (InvalidFormatException ex) {
				throw new IOException("Can not open Excel file", ex);
			}
		}
		return workbook;
	}

	@Override
	public Iterator<Object[]> getRowIterator() throws IOException {
		return new RowIterator(getWorkbook().getSheetAt(sheet_index).rowIterator(), evaluate_formula ? getWorkbook().getCreationHelper().createFormulaEvaluator() : null);
	}

	@Override
	public int getRowCount() throws IOException {
		return getWorkbook().getSheetAt(sheet_index).getLastRowNum();
	}

	private static class RowIterator implements Iterator<Object[]> {

		private final Iterator<Row> row_iter;
		private final FormulaEvaluator evaluate;

		public RowIterator(Iterator<Row> iter, FormulaEvaluator evaluate) {
			this.row_iter = iter;
			this.evaluate = evaluate;

		}

		@Override
		public boolean hasNext() {
			return row_iter.hasNext();
		}

		@Override
		public Object[] next() {
			Row row = row_iter.next();
			if (row == null) {
				return null;
			}

			Object[] row_data = new Object[row.getLastCellNum()];
			for (int i = 0; i < row_data.length; i++) {
				Cell cell = row.getCell(i, Row.RETURN_NULL_AND_BLANK);
				if (cell != null) {
					row_data[i] = toData(cell);
				}
			}

			return row_data;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported.");
		}

		private Object toData(Cell c) {
			switch (c.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					return "";
				case Cell.CELL_TYPE_BOOLEAN:
					return c.getBooleanCellValue();
				case Cell.CELL_TYPE_NUMERIC:
					double d = c.getNumericCellValue();
					if (d == Math.floor(d)) {
						return new Integer(new Double(d).intValue());
					}
					return new Double(d);
				case Cell.CELL_TYPE_FORMULA:
					if (evaluate != null) {
						CellValue cv = evaluate.evaluate(c);
						if (cv == null) {
							return c.getCellFormula();
						}
						if (cv.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							double d2 = cv.getNumberValue();
							if (d2 == Math.floor(d2)) {
								return new Integer(new Double(d2).intValue());
							}
							return new Double(d2);
						}
						else if (cv.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
							return cv.getBooleanValue();
						}
						return cv.getStringValue();
					}
					return c.getCellFormula();
			}
			return c.getStringCellValue();
		}
	}
}
