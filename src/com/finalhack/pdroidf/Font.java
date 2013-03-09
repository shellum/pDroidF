package com.finalhack.pdroidf;

import static com.finalhack.pdroidf.Pdf.NEW_LINE;

/**
 * This class represents an internal data structure.
 * Someone using this code to output a PDF doesn't need to worry about this class.
 *
 */
public class Font
{
	//Most classes in this PDF library need an objectId
	//The objectId must be unique across all elements in the PDF
	private int objectId;  
	private static final String DEFAULT_FONT = "Times-Roman";
	
	/**
	 * Simple constructor
	 * @param objectId
	 */
	public Font(int objectId) { this.objectId = objectId; }
	
	/**
	 * Simple getter for unique objectId
	 * @return
	 */
	protected int getObjectId() { return this.objectId; }
	
	/**
	 * Output this object in PDF format
	 */
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		ret.append("" + objectId + " 0 obj").append(NEW_LINE);
		ret.append("<<").append(NEW_LINE);
		ret.append("  /Type /Font").append(NEW_LINE);
		ret.append("  /Subtype /Type1").append(NEW_LINE);
		ret.append("  /Name /F1").append(NEW_LINE);
		ret.append("  /BaseFont /" + DEFAULT_FONT).append(NEW_LINE);
		ret.append("  /Encoding /WinAnsiEncoding").append(NEW_LINE);
		ret.append(">>").append(NEW_LINE);
		ret.append("endobj").append(NEW_LINE);
		
		return ret.toString();
	}
}