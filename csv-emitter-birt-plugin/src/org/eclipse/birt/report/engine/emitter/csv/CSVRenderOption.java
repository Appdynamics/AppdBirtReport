package org.eclipse.birt.report.engine.emitter.csv;

import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

public class CSVRenderOption extends RenderOption implements ICSVRenderOption{
	
	public static final String CSV = "CSV";	
	
	public CSVRenderOption( ) 
	{
		super();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param options
	 */
	public CSVRenderOption( IRenderOption options )
	{
		super( options );
	}

	/**
	 * Set show datatype in second row
	 * 
	 * @param showDatatypeInSecondRow
	 */
	public void setShowDatatypeInSecondRow(boolean showDatatypeInSecondRow)
	{	
		setOption( SHOW_DATATYPE_IN_SECOND_ROW, showDatatypeInSecondRow );		
	}

	/**
	 * Get show datatype in second row
	 * 
	 * @param
	 */
	public boolean getShowDatatypeInSecondRow() 
	{
		return getBooleanOption(SHOW_DATATYPE_IN_SECOND_ROW, true);
	}

	/**
	 * Set Export Table By Name option
	 * 
	 * @param tableName
	 */
	public void setExportTableByName(String tableName)
	{
		setOption(EXPORT_TABLE_BY_NAME,tableName);		
	}

	/**
	 * Get Export Table By Name option
	 * 
	 * @param
	 */
	public String getExportTableByName()
	{
		return getStringOption(EXPORT_TABLE_BY_NAME);
	}
	
	/**
	 * Set Field Delimiter Option
	 * 
	 * @param fieldDelimiter
	 */
	public void setDelimiter(String protectedChars)
	{
		setOption(PROTECTED_CHARS,protectedChars);
	}

	/**
	 * Get Field Delimiter Option
	 * 
	 * @param
	 */
	public String getDelimiter()
	{		
		return getStringOption(PROTECTED_CHARS);
	}

	/**
	 * Get Replace Delimiter Inside Text With Option
	 * 
	 * @param
	 */
	public String getProtectedChars()
	{
		return getStringOption(PROTECTED_CHARS);
	}

	/**
	 * Set Replace Delimiter Inside Text With Option
	 * 
	 * @param replaceDelimiterInsideTextWith
	 */
	public void setProtectedChars(String protectedChars)
	{
		setOption(PROTECTED_CHARS, protectedChars);		
	}

	/**
	 * Set Replace Delimiter Inside Text With Option
	 * 
	 * @param replaceDelimiterInsideTextWith
	 */
	public void setQuoteChar(String quoteChar)
	{
		setOption(QUOTE_CHAR, quoteChar);		
	}

	/**
	 * Get Replace Delimiter Inside Text With Option
	 * 
	 * @param
	 */
	public String getQuoteChar()
	{
		return getStringOption(QUOTE_CHAR);
	}
}
