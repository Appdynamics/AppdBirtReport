package org.eclipse.birt.report.engine.emitter.csv;


import java.io.OutputStream;
import java.util.logging.Logger;

import org.eclipse.birt.report.engine.emitter.XMLWriter;

public class CSVWriter extends XMLWriter{
	
	public CSVWriter(){
		bImplicitCloseTag = false;
		bPairedFlag = false;
	}
	
	
	protected static Logger logger = Logger.getLogger( CSVWriter.class.getName( ) );

	public void open(OutputStream out, String string) {		
		super.open(out,string);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.engine.emitter.XMLWriter#startWriter()
	 * overridden in CSVWriter to avoid writing XML tagging in beginning of file
	 */
	public void startWriter() {
		// do nothing here. 		
	}

	public void endWriter() {
		// do nothing flushing will happen in close
	}

	public void close() {		
		super.close();
	}

	public void text(String textValue, String protectedChars, String quoteChar) {
		if ( textValue == null || textValue.length( ) == 0 )
		{
			return;
		}		
		
		// Replacing delimiter in Cell Data with user defined character for CSVWriter
		textValue = textValue.replace(quoteChar, quoteChar+quoteChar);
		
		boolean containsProtected = false;
		for (int i = 0; i < protectedChars.length(); i++)
		{
			containsProtected = containsProtected || textValue.indexOf(protectedChars.charAt(i)) > -1;
		}
		
		if(containsProtected)
		{
			textValue = quoteChar + textValue +quoteChar;
		}
		
		print( textValue );		
	}

	public void closeTag( String tagName )
	{
		print(tagName);			
	}
}
