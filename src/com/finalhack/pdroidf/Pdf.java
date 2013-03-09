package com.finalhack.pdroidf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

/**
 * This class represents a PDF file. By calling its methods, elements can be added to the PDF.
 * The final output produced by this class is a byte[].
 */
public class Pdf {

	public static final boolean DEBUG_ON = true;
	public static final String LOG_TAG_DEBUG = "PDroidF";
	public static final String LOG_STACKTRACE = "PDroidF Stacktrace";
	
	//General PDF syntax
	protected static final String DEFAULT_VERSION = "1.7";
	//use new line in ShareUtil also
	public static final String NEW_LINE = "\n";
	private static final String PDF_HEADER = "%PDF-" + DEFAULT_VERSION + NEW_LINE;
	private static final String PDF_FOOTER = "%%EOF";

	//For convenience, a list of bytes will be kept as opposed to constantly copying byte[]s to larger byte[]s when adding data
	private List<Byte> document = new ArrayList<Byte>();
	
	//An AtomicInteger will help keep objectIds unique through the PDF
	private AtomicInteger currentObjectId = new AtomicInteger(1);
	private int catalogId = currentObjectId.getAndAdd(1);
	private int pagesId = currentObjectId.getAndAdd(1);
	
	//Main components required for a PDF to exist
	private Catalog catalog = new Catalog(catalogId);
	private Xref xref = new Xref();
	private Pages pages = new Pages(pagesId, xref, document, Pages.LAYOUT_PORTRAIT);
	
	/**
	 * Adds a new blank page to the PDF.
	 * This method must be called before adding any elements to the first page, and again for each additional page.
	 * The returned Stream object can be used to add elements like text and graphics to the page 
	 * @return Stream
	 */
	public Stream addPage()
	{
		//Create a new page object and set its parent information
		Page page = new Page(currentObjectId, pages);
		pages.addPage(page);
		
		//Return the page's Stream so objects can be added to it
		return page.getStream();
	}

	/**
	 * A convenience method for adding a list to an existing byte array
	 * @param list
	 * @param array
	 */
	protected static void addByteArray(List<Byte> list, byte[] array)
	{
		for (int i=0;i<array.length;i++) list.add(array[i]);
	}
	
	/**
	 * Call this when you are ready to write out the PDF.
	 * This method gets the final byte[] representing the PDF.
	 * The returned byte[] could be written to a file, and that file could then be opened up as a PDF
	 * @return
	 */
	public byte[] getPDF()
	{
		//Write out the header
		addByteArray(document, PDF_HEADER.getBytes());
		
		//Write out the catalog
		xref.addXref(catalogId, document.size());
		catalog.setPages(pagesId);
		addByteArray(document, catalog.toString().getBytes());

		//Write out the 'Pages' element (not to be confused with each page)
		xref.addXref(pagesId, document.size());
		addByteArray(document, pages.toString().getBytes());
		
		//Write out each page
		pages.addPageDataToXref();
		List<Page> individualPages = pages.getPages();
		for (Page page : individualPages)
		{
			Stream stream = page.getStream();
			xref.addXref(stream.getObjectId(), document.size());
			addByteArray(document, stream.toBytes());

		}
		//write out font data
		pages.addFontDataToXref();
		//Write out the x-references
		xref.setRefOffset(document.size());
		addByteArray(document, xref.toString().getBytes());
		
		//Write out the footer
		addByteArray(document, PDF_FOOTER.getBytes());
		
		//Convert what we've been writing out to an actual byte[]
		byte[] documentBytes = new byte[document.size()];
		for (int i=0;i<document.size();i++) documentBytes[i] = document.get(i);
		
		//Return the PDF byte data
		return documentBytes;
	}
	
	//Get a byte array representing a BMP file
	public static byte[] getAssetJpgBytes(Context context, String fileName)
	{
		//Use the Asset Manager to get a stream of data
		AssetManager assetManager = context.getAssets();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream inputStream;
		try
		{
			inputStream = assetManager.open(fileName);
			byte[] byteChunk = new byte[4096];
			int n;

			//As long as we need to, read in BMP data
			while ( (n = inputStream.read(byteChunk)) > 0 ) baos.write(byteChunk, 0, n);
			  
			inputStream.close();
			
			//If successful, return the BMP data
			return baos.toByteArray();
		}
		catch (IOException e)
		{
			if (Pdf.DEBUG_ON) Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e);
		}
		
		//Return null on error and let us know we have a real problem (because there are no known reasons for this failing)
		return null;
	}
	
	// Write out the PDF so it can be viewed by a PDF viewer, sent somewhere,
	// etc.
	public static File writePdf(Context context, byte[] pdfBytes, String filename) {
		File file = null;
		
		try {
			// Make sure everything is written to a valid location
			file = createFileInTheRightSpot(context, filename);
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			//Write out the actual PDF
			fileOutputStream.write(pdfBytes);

			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			if (Pdf.DEBUG_ON)
				Log.d(Pdf.LOG_TAG_DEBUG, Pdf.LOG_STACKTRACE, e);
		}
		
		return file;
	}

	// This method takes care of making sure that storage is available, and
	// creating a file in the right spot
	private static File createFileInTheRightSpot(Context context, String fileName) {
		// Is external storage available?
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			// If we can't get storage, let the user know
			if (Pdf.DEBUG_ON)
				Log.d(Pdf.LOG_TAG_DEBUG, "No external storage available?");
			return null;
		}

		// Create the actual file
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

		// Make all needed directories if they do not exist
		dir.mkdirs();

		// Create the file
		File file = new File(dir, fileName);
		return file;
	}
	
}
