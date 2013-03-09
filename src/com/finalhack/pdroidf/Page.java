package com.finalhack.pdroidf;

import static com.finalhack.pdroidf.Pdf.NEW_LINE;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents an internal data structure.
 * Someone using this code to output a PDF doesn't need to worry about this class.
 */
public class Page
{
	//Most classes in this PDF library need an objectId
	//The objectId must be unique across all elements in the PDF
	private int objectId;  
	
	//References to parent/child PDF objects
	private int parentObjectId;
	private Stream stream;
	private Font font;
	
	/**
	 * Construct self and child objects based on unique objectIds
	 * @param currentObjectId
	 * @param pages
	 */
	public Page(AtomicInteger currentObjectId, Pages pages)
	{
		this.objectId = currentObjectId.getAndAdd(1);
		stream = new Stream(currentObjectId, pages);
		font = new Font(currentObjectId.getAndAdd(1));
		this.parentObjectId = pages.getObjectId();
	}
	
	/**
	 * Simple getter for child Stream
	 * @return
	 */
	protected Stream getStream() { return this.stream; }
	
	/**
	 * Simple getter for child Font
	 * @return
	 */
	protected Font getFont() { return this.font; }
	
	/**
	 * Simple getter for unique objectId
	 * @return
	 */
	protected int getObjectId() { return this.objectId; }
	
	
	/**
	 * Output the actual object in PDF format
	 */
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		ret.append("" + objectId + " 0 obj").append(NEW_LINE);
		ret.append("<<").append(NEW_LINE);
		ret.append("  /Type /Page").append(NEW_LINE);
		ret.append("  /Parent " + parentObjectId + " 0 R").append(NEW_LINE);
		ret.append("  /MediaBox [ 0 0 " + com.finalhack.pdroidf.Pages.PAGE_WIDTH + " " + com.finalhack.pdroidf.Pages.PAGE_HEIGHT + " ]").append(NEW_LINE);
		ret.append("  /Contents " + stream.getObjectId() + " 0 R").append(NEW_LINE);
		ret.append("  /Resources <<").append(NEW_LINE);
		ret.append("    /Font <<").append(NEW_LINE);
		ret.append("      /F1 " + (font.getObjectId()) + " 0 R").append(NEW_LINE);
		ret.append("    >>").append(NEW_LINE);
		ret.append("  >>").append(NEW_LINE);
		ret.append(">>").append(NEW_LINE);
		ret.append("endobj").append(NEW_LINE);
		ret.append(NEW_LINE);
		
		return ret.toString();
	}
	
}