package com.finalhack.pdroidf;

import static com.finalhack.pdroidf.Pdf.NEW_LINE;

/**
 * This class represents an internal data structure.
 * Someone using this code to output a PDF doesn't need to worry about this class.
 */
public class Catalog
{
	//Most classes in this PDF library need an objectId
	//The objectId must be unique across all elements in the PDF
	private int objectId;
	
	//We need to a reference to our child Pages object
	private int pagesObjectId;
	
	/**
	 * Simple constructor to set a unique objectId
	 * @param objectId
	 */
	public Catalog(int objectId) { this.objectId = objectId; }
	
	/**
	 * Simple setter to set the reference to a child Pages object
	 * @param pagesObjectId
	 */
	protected void setPages(int pagesObjectId) { this.pagesObjectId = pagesObjectId; }
	
	/**
	 * Output this object in PDF format
	 */
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		ret.append(objectId + " 0 obj").append(NEW_LINE);
		ret.append("<<").append(NEW_LINE);
		ret.append("  /Type /Catalog").append(NEW_LINE);
		ret.append("  /Pages ").append(pagesObjectId + " 0 R").append(NEW_LINE);
		ret.append(">>").append(NEW_LINE);
		ret.append("endobj").append(NEW_LINE);
		ret.append(NEW_LINE);
		
		return ret.toString();
	}
}