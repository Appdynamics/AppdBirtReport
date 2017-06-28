package org.eclipse.birt.report.engine.emitter.csv;

import org.eclipse.birt.report.engine.api.IRenderOption;

public interface ICSVRenderOption extends IRenderOption{
	
	/**
	 * CSV Emitter Id
	 */
	public static final String OUTPUT_EMITTERID_CSV = "org.eclipse.birt.report.engine.emitter.csv";	
	
	/**
	 * CSV Output Format
	 */
	public static final String OUTPUT_FORMAT_CSV = "csv";
	
	/**
	 * The option to decide to show data type in second row of output CSV
	 */
	public static final String SHOW_DATATYPE_IN_SECOND_ROW = "csvRenderOption.showDatatypeInSecondRow";
	
	/**
	 * The option to export a specific table by name in the CSV Output
	 */
	public static final String EXPORT_TABLE_BY_NAME = "csvRenderOption.exportTableByName";
	
	/**
	 * The option to specify the protected chars, default:,\n. If protected chars are seen in any field they are enclosed by quotes
	 */
	public static final String DELIMITER = "csvRenderOption.delimiter";
	
	/**
	 * The option to specify the protected chars, default:,\n. If protected chars are seen in any field they are enclosed by quotes
	 */
	public static final String PROTECTED_CHARS = "csvRenderOption.protectedChars";
	
	/**
	 * The option to specify the character to do the quoting with, default is double quote
	 */
	public static final String QUOTE_CHAR = "csvRenderOption.quoteChar";
	
	public void setShowDatatypeInSecondRow(boolean showDatatypeInSecondRow);
	
	public boolean getShowDatatypeInSecondRow();
	
	public void setExportTableByName(String tableName);
	
	public String getExportTableByName();
	
	public void setDelimiter (String fieldDelimiter);
	
	public String getDelimiter ();
	
	public void setProtectedChars (String protectedChars);
	
	public String getProtectedChars ();
	
	public void setQuoteChar (String quoteChar);
	
	public String getQuoteChar ();
}
