/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat.Field;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 *
 * @author manuel
 */
public class LoggingUtils {

	public static void initLogger() {
		initLogger(Level.INFO);
	}

	public static void initLogger(Level level) {
		initLogger("de.gobics", level);
	}

	public static void initLogger(final String root_package, final Level level) {
		Logger root = Logger.getLogger("");
		root.setLevel(level);
		
		// Set level and formatter for all handlers
		for (Handler handler : root.getHandlers()) {
			handler.setLevel(level);
			handler.setFormatter(new LogFormatter());
			handler.setFilter(new Filter() {

				public boolean isLoggable(LogRecord record) {
					if (record.getLevel().equals(Level.SEVERE)) {
						return true;
					}
					if (record.getLevel().equals(Level.WARNING)) {
						return true;
					}
					//if( record.getLoggerName().startsWith("de.gobics.keggcache."))
					//	return false;
					
					return record.getLoggerName().startsWith(root_package)
							//|| record.getLoggerName().startsWith("javacyc")
							//|| record.getLoggerName().startsWith("keggapi")
							;
						

				}
			});
		}

	}

	public static void initLogger(Level level, File output_file) throws IOException {
		initLogger(level, output_file, true);
	}

	public static void initLogger(Level level, File output_file, boolean append) throws IOException {
		// Add the filehandler
		if (output_file != null) {
			if (!append && output_file.exists()) {
				output_file.delete();
			}

			Logger.getLogger("").addHandler(new FileHandler(output_file.getAbsolutePath()));
		}

		// Set the level and other stuff
		initLogger(level);
	}
//
//	private static void initLog4j() {
//		org.apache.log4j.Logger l = org.apache.log4j.Logger.getRootLogger();
//		l.setLevel(org.apache.log4j.Level.INFO);
//		ConsoleAppender a = new org.apache.log4j.ConsoleAppender(new Layout() {
//
//			private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			private final String lineSeparator = "\n";
//			private final Date dat = new Date();
//
//			@Override
//			public String format(LoggingEvent record) {
//				StringBuffer sb = new StringBuffer(record.getLevel().toString());
//				sb.append(" [");
//
//				dat.setTime(record.getTimeStamp());
//				formatter.format(dat, sb, new FieldPosition(Field.YEAR));
//
//				sb.append("] ");
//				if (record.getLoggerName() != null) {
//					sb.append(record.getLoggerName());
//				} else {
//					sb.append("UNKOWN-SOURCE");
//				}
//				sb.append(".");
//				if (record.getLocationInformation().getMethodName() != null) {
//					sb.append(record.getLocationInformation().getMethodName());
//				} else {
//					sb.append("UNKOWN-METHOD");
//				}
//				sb.append("()");
//
//
//				sb.append(": ");
//				sb.append(record.getMessage());
//				sb.append(lineSeparator);
//				return sb.toString();
//			}
//
//			@Override
//			public boolean ignoresThrowable() {
//				return false;
//			}
//
//			public void activateOptions() {
//			}
//		});
//		l.addAppender(a);
//	}
}

class LogFormatter extends Formatter {

	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String lineSeparator = "\n";
	Date dat = new Date();

	public synchronized String format(LogRecord record) {
		StringBuffer sb = new StringBuffer(record.getLevel().getLocalizedName());
		sb.append(" [");

		dat.setTime(record.getMillis());
		formatter.format(dat, sb, new FieldPosition(Field.YEAR));

		sb.append("] ");
		if (record.getSourceClassName() != null) {
			sb.append(record.getSourceClassName());
		} else {
			sb.append("UNKOWN-SOURCE");
		}
		sb.append(".");
		if (record.getSourceMethodName() != null) {
			sb.append(record.getSourceMethodName());
		} else {
			sb.append("UNKOWN-METHOD");
		}
		sb.append("()");

		String message = formatMessage(record);
		sb.append(": ");
		sb.append(message);
		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
			}
		}
		sb.append(lineSeparator);
		return sb.toString();
	}
}