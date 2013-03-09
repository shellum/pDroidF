package com.finalhack.pdroidf;

import static com.finalhack.pdroidf.Pdf.NEW_LINE;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

/**
 * This class is used to add elements such as text and graphics to a PDF.
 * Each stream is hooked to a PDF page.
 * This class is where much of the PDF work is done.
 */
public class Stream
{
	private static final String CHARSET = "ISO-8859-1";

	private static final String WRAP_MARKER = "\n";

	//For auto-text adds
	private static final int DEFAULT_FONT_SIZE = 12;

	//Can be used to center text in a PDF via addText(String, int, int, int)
	public static final int CENTER = -1;
	
	//This tracks the entire stream as elements are added.
	//This is done in a List for simplicity in appending elements
	private List<Byte> streamList = new ArrayList<Byte>();
	
	//Most classes in this PDF library need an objectId
	//The objectId must be unique across all elements in the PDF
	private int objectId;
	
	//RGB info
	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;
	
	//BMP settings for images
	private static final String BITS_PER_COLOR = "8";
	private static final String COLOR_SPACE = "/RGB";
	private static final String IMAGE_DECODER = "/DCT";
	
	//Track average glyphs (font characters) in order to center text without too much work
	private static final double GLYPH_WIDTH = 5.4;
	private static final double GLYPH_HEIGHT = 13.8;

	//Margins to keep clean
	public static final double MARGIN_VERTICAL = 72.00;
	public static final double MARGIN_HORIZONTAL = 72.00;

	//Used for line wrapping
	private int averageGlyphsPerLine;
	
	//The coordinates for the next auto-placed line
	private double nextLineX = MARGIN_HORIZONTAL;
	private double nextLineY;
	
	//A punctuation set to help with knowing when to split a wrapping line
	private static Set<Integer> punctuation = new HashSet<Integer>();
	
	//Used in case a line wraps to a new page...
	//A new page and new stream will need to be added to that the wrapped text can be written
	private Stream streamToAddTo = this;
	
	//A parent reference
	private Pages pages;
	
	//A reference to the last unique objectId created
	//This is used in case new pages are created on the fly, which need their own unique objectIds
	private AtomicInteger currentObjectId;
	
	//Prepare the punctuation list
	static
	{
		punctuation.add((int)'.');
		punctuation.add((int)'?');
		punctuation.add((int)'!');
		punctuation.add((int)',');
		punctuation.add((int)';');
		punctuation.add((int)':');
	}
	
	/**
	 * Construct the stream, and save off info for future on the fly page construction
	 * @param currentObjectId
	 * @param pages
	 */
	public Stream(AtomicInteger currentObjectId, Pages pages)
	{
		this.currentObjectId = currentObjectId;
		this.objectId = currentObjectId.getAndAdd(1);
		this.pages = pages;
		
		nextLineY = Pages.PAGE_HEIGHT - MARGIN_VERTICAL;
		averageGlyphsPerLine = (int)((Pages.PAGE_WIDTH - (MARGIN_HORIZONTAL * 2)) / GLYPH_WIDTH);
	}
	
	/**
	 * Simple getter for the object's uniqueId
	 * @return
	 */
	public int getObjectId() { return this.objectId; }
	
	/**
	 * Draw an actual line using hex color codes on the PDF at the next auto-text position.
	 * This will happen either at the top of the file if addText() has not yet been called, or below the last line added via addText().
	 * An example hex color code would be 2A58CB.
	 * @param hexColorCode
	 * @param lineHeight
	 */
	public void addHorizontalLine(String hexColorCode, int lineHeight)
	{
		//Convert hex codes to RGB doubles
		double[] rgb = convertHexColorCodeToDoubleRgbComponents(hexColorCode);
		//Call the real horizontal line method
		
		StringBuilder streamData = new StringBuilder();

		streamData.append(NEW_LINE).append(rgb[RED] + " " + rgb[GREEN] + " " + rgb[BLUE] + " rg").append(NEW_LINE);
		streamData.append((int)MARGIN_HORIZONTAL + " " + (int)nextLineY + " m").append(NEW_LINE);
		streamData.append((int)(Pages.PAGE_WIDTH - MARGIN_HORIZONTAL) + " " + (int)nextLineY + " l").append(NEW_LINE);
		streamData.append((int)(Pages.PAGE_WIDTH - MARGIN_HORIZONTAL) + " " + (int)(nextLineY-lineHeight) + " l").append(NEW_LINE);
		streamData.append((int)(MARGIN_HORIZONTAL) + " " + (int)(nextLineY-lineHeight) + " l").append(NEW_LINE);
		streamData.append("f").append(NEW_LINE);

		nextLineY -= GLYPH_HEIGHT;

		//Try to encode pdf data via a specific charset
		try { Pdf.addByteArray(this.streamList, streamData.toString().getBytes(CHARSET)); } catch (UnsupportedEncodingException e) { if (Pdf.DEBUG_ON) Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e); }

	}
	
	//Convert an HTML hex code such as 2A58CB to separate R, G, B components ranging from 0 to 1
	private double[] convertHexColorCodeToDoubleRgbComponents(String hexColorCode)
	{
		double[] ret = new double[3];
		
		//If the hex color code is not the correct length, return black (0,0,0)
		if (hexColorCode.length() != 6) return ret;
		
		//Break apart the colors
		String rStr = hexColorCode.substring(0,2);
		String gStr = hexColorCode.substring(2,4);
		String bStr = hexColorCode.substring(4,6);
		
		//Convert them from hex
		int rInt = Integer.parseInt(rStr, 16);
		int gInt = Integer.parseInt(gStr, 16);
		int bInt = Integer.parseInt(bStr, 16);
		
		//Scale them from 0 to 1
		ret[RED] = rInt / 255.0;
		ret[GREEN] = gInt / 255.0;
		ret[BLUE] = bInt / 255.0;
		
		return ret;
	}
		
	/**
	 * Add a line of text to this Stream's Page at x,y using fontSize.
	 * Sorry, no font choice.
	 * Adding a line using this method does not affect the x,y auto line tracking that addText(<the one with no parameters>) uses
	 * Text may be centered when using this method by using this class's static CENTER property as x and or y coordinate values
	 * x,y is based of an origin (0,0) that is in the bottom left corner of the page
	 * @param text
	 * @param fontSize
	 * @param x
	 * @param y
	 */
	public void addText(String text, int fontSize, int x, int y, String hexColorCode)
	{
		//PDFs don't like un-escaped parentheses because those are delimiters for text
		text = text.replaceAll("\\(", "\\\\(");
		text = text.replaceAll("\\)", "\\\\)");
		
		//Centering mechanics
		if (x == CENTER) x = (Pages.PAGE_WIDTH / 2) - ((int)(text.codePointCount(0, text.length()) * GLYPH_WIDTH)/2);
		if (y == CENTER) y = (Pages.PAGE_HEIGHT / 2) - (int)(GLYPH_HEIGHT/2);
		
		double[] rgb = convertHexColorCodeToDoubleRgbComponents(hexColorCode);
		
		StringBuilder streamData = new StringBuilder();
		streamData.append("BT").append(NEW_LINE);
		streamData.append("/F1 " + fontSize + " Tf").append(NEW_LINE);
		streamData.append("1 0 0 1" + " " + x + " " + y + " Tm").append(NEW_LINE);
		streamData.append(rgb[RED] + " " + rgb[GREEN] + " " + rgb[BLUE] + " rg").append(NEW_LINE);
		streamData.append("(" + text + ") Tj").append(NEW_LINE);
		streamData.append("ET").append(NEW_LINE);
		
		//Try to encode pdf data via a specific charset
		try { Pdf.addByteArray(this.streamList, streamData.toString().getBytes(CHARSET)); } catch (UnsupportedEncodingException e) { if (Pdf.DEBUG_ON) Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e); }
	}
	
	/**
	 * A helper method for using default font size
	 */
	public void addText(String text, String hexColorCode)
	{
		addText(text, hexColorCode, DEFAULT_FONT_SIZE);
	}
	
	/**
	 * Add an auto-line (don't worry about specifying an x,y coordinate pair.
	 * This is useful if you don't know how many lines will be added.
	 * Each new line added with this method starts either at the top left (if it is the first line) or below and left aligned with the previous line.
	 * Each line will auto wrap if it is too long. If a line wraps off the page, a new page is created, and the remaining text will flow onto that new page.
	 * This method does not take into account any text that was added via the addText(String, fontSize, x, y). It only takes into account text that was added with addText().
	 */
	public void addText(String text, String hexColorCode, int fontSize)
	{
		//Check to see if line wrapping is needed
		StringBuffer textBuffer = new StringBuffer(text);
		int limit = text.codePointCount(0, text.length());
		for (int i=averageGlyphsPerLine;i<limit-1;i+=averageGlyphsPerLine)
		{
			String stringToInsert = WRAP_MARKER;
			//Find an appropriate spot to line wrap
			//Verify that punctuation is not next
			//If the spot is in the middle of a word, add a '-'
			int nextCodePoint = textBuffer.codePointAt(i);
			if (punctuation.contains(nextCodePoint)) i++;
			if (textBuffer.codePointAt(i)>=65) stringToInsert = "-" + WRAP_MARKER;
			
			//Insert the wrap marker with a possible preceding dash where we want to wrap
			textBuffer.insert(i, stringToInsert);
		}
		
		text = textBuffer.toString();
		
		//Use the previously added WRAP_MARKER to determine where to actually split lines
		String[] lines = text.split(WRAP_MARKER);
		
		//For each line, calculate the appropriate coordinates to place each line at
		for (String line : lines)
		{
			if ((streamToAddTo.nextLineY < MARGIN_VERTICAL))
			{
				Page page = new Page(currentObjectId, this.pages);
				streamToAddTo.pages.addPage(page);
				streamToAddTo = page.getStream();
			}
			
			//Remove spaces that precede a line
			while (line.length() > 0 && line.codePointAt(0)==32) line = line.substring(1);
			streamToAddTo.addText(line, fontSize, (int)streamToAddTo.nextLineX, (int)streamToAddTo.nextLineY, hexColorCode);
			streamToAddTo.nextLineY -= GLYPH_HEIGHT;
		}
	}

	/**
	 * Add a BMP image who's top corner is at x,y based on the standard PDF coordinate system.
	 * The standard PDF coordinate system places the origin (0,0) at the bottom left corner of the page.
	 * Increase x to move right, and increase y to move up.
	 * @param width
	 * @param height
	 * @param bytes
	 */
	public void addBmpImage(int x, int y, int width, int height, byte[] bytes)
	{
		//Image header info
		StringBuilder streamData = new StringBuilder();
		streamData.append("q").append(NEW_LINE);
		streamData.append("" + width + " 0 0 " + height + " " + x + " " + y + " cm").append(NEW_LINE);
		streamData.append("BI").append(NEW_LINE);
		streamData.append("  /W " + width).append(NEW_LINE);
		streamData.append("  /H " + height).append(NEW_LINE);
		streamData.append("  /BPC " + BITS_PER_COLOR).append(NEW_LINE);
		streamData.append("  /CS " + COLOR_SPACE).append(NEW_LINE);
		streamData.append("  /F [" + IMAGE_DECODER + "]").append(NEW_LINE);
		//one white space after ID
		streamData.append("ID ");
		//Try to encode pdf data via a specific charset
		try { Pdf.addByteArray(this.streamList, streamData.toString().getBytes(CHARSET)); } catch (UnsupportedEncodingException e) { if (Pdf.DEBUG_ON) Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e); }
		
		//Actual image data
		for (byte b : bytes) this.streamList.add(b);
		
		//Image footer data
		streamData = new StringBuilder();
		//need newline before EI to indicate end of line for the image stream
		streamData.append(NEW_LINE).append("EI").append(NEW_LINE);
		streamData.append("Q").append(NEW_LINE);
		 
		//Try to encode pdf data via a specific charset
		try { Pdf.addByteArray(this.streamList, streamData.toString().getBytes(CHARSET)); } catch (UnsupportedEncodingException e) { if (Pdf.DEBUG_ON) Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e); }
	}
	
	/**
	 * Output the actual object in PDF format.
	 * toString is not being used here because we need a return type of byte[].
	 * @return
	 */
	protected byte[] toBytes()
	{
		List<Byte> byteList = new ArrayList<Byte>();
		
		//Stream header
		StringBuilder stringData = new StringBuilder();
		stringData.append("" + objectId + " 0 obj").append(NEW_LINE);
		stringData.append("<<").append(NEW_LINE);
		stringData.append("  /Length " + streamList.size()).append(NEW_LINE);
		stringData.append(">>").append(NEW_LINE);
		stringData.append("stream").append(NEW_LINE);		
		//Try to encode pdf data via a specific charset
		try { Pdf.addByteArray(byteList, stringData.toString().getBytes(CHARSET)); } catch (UnsupportedEncodingException e) { if (Pdf.DEBUG_ON) Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e); }
		
		//Actual data stream
		for (Byte b : streamList) byteList.add(b);
		
		//Stream footer
		stringData = new StringBuilder();
		stringData.append(NEW_LINE).append("endstream").append(NEW_LINE);
		stringData.append("endobj").append(NEW_LINE);
		//Try to encode pdf data via a specific charset
		try { Pdf.addByteArray(byteList, stringData.toString().getBytes(CHARSET)); } catch (UnsupportedEncodingException e) { if (Pdf.DEBUG_ON) Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e); }

		//Return an actual byte[], so convert to this from a List<Byte>
		byte[] actualBytes = new byte[byteList.size()];
		for (int i=0;i<byteList.size();i++) actualBytes[i] = byteList.get(i);
		
		return actualBytes;
	}
}