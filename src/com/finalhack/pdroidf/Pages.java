package com.finalhack.pdroidf;

import static com.finalhack.pdroidf.Pdf.NEW_LINE;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an internal data structure.
 * Someone using this code to output a PDF doesn't need to worry about this class.
 */
public class Pages
{
	protected static final int LAYOUT_PORTRAIT = 0;
	protected static final int LAYOUT_LANDSCAPE = 1;
	
	//Page dimensions based on a 72dpi layout
	public static int PAGE_WIDTH = 612;
	public static int PAGE_HEIGHT = 792;
	
	//Most classes in this PDF library need an objectId
	//The objectId must be unique across all elements in the PDF
	private int objectId;
	private List<Page>kidPages = new ArrayList<Page>();
	
	//Allow information to be written about child objects
	//It is easier for some child objects to be managed fully within a parent object
	private Xref xref;
	private List<Byte> parentDocument;
	
	/**
	 * Construct self, and store internal references to sibling/parent objects
	 * @param objectId
	 * @param xref
	 * @param document
	 */
	public Pages(int objectId, Xref xref, List<Byte> document, int pageLayout)
	{
		this.objectId = objectId;
		this.xref = xref;
		this.parentDocument = document;
		
		//If we need landscape mode, switch the current layout params
		if (pageLayout == LAYOUT_LANDSCAPE)
		{
			int swapVar = PAGE_HEIGHT;
			PAGE_HEIGHT = PAGE_WIDTH;
			PAGE_WIDTH = swapVar;
		}
	}
	
	/**
	 * Simple getter for unique objectId
	 * @return
	 */
	protected int getObjectId() { return objectId; }
	
	/**
	 * Simple getter for all child Page objects
	 * @return
	 */
	protected List<Page> getPages() { return kidPages; }
	
	/**
	 * Add a new child page
	 * @param page
	 */
	protected void addPage(Page page)
	{
		kidPages.add(page);
	}
	
	/**
	 * Add x-reference data for each child Page object, and its associated Font object
	 */
	protected void addPageDataToXref()
	{
		for (Page page : kidPages)
		{
			xref.addXref(page.getObjectId(), parentDocument.size());
			Pdf.addByteArray(parentDocument, page.toString().getBytes());		
		}
	}
	
	/**
	 * Add the font object to keep the numbering correct in the pdf
	 * Added to get the correct ordering according to pdf spec. I
	 * don't know how important correct ordering is because I think
	 * they just link up, but this was to eliminate that possibility
	 */
	protected void addFontDataToXref()
	{
		for (Page page : kidPages)
		{
			xref.addXref(page.getFont().getObjectId(), parentDocument.size());
			Pdf.addByteArray(parentDocument, page.getFont().toString().getBytes());			
		}
	}
	
	/**
	 * Output the actual object in PDF format
	 */
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		ret.append("" + objectId + " 0 obj").append(NEW_LINE);
		ret.append("<<").append(NEW_LINE);
		ret.append("  /Type /Pages").append(NEW_LINE);
		ret.append("  /Kids [ ");
		
		for (Page page : kidPages) ret.append(page.getObjectId() + " 0 R ");
		
		ret.append("]").append(NEW_LINE);
		ret.append("  /Count " + kidPages.size()).append(NEW_LINE);
		ret.append(">>").append(NEW_LINE);
		ret.append("endobj").append(NEW_LINE);
		ret.append(NEW_LINE);
		
		return ret.toString();
	}
}