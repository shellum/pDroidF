package com.finalhack.pdroidf;

import static com.finalhack.pdroidf.Pdf.NEW_LINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents an internal data structure.
 * Someone using this code to output a PDF doesn't need to worry about this class.
 */
public class Xref
{
	//References to all refs created
	List<Ref> refs = new ArrayList<Ref>();
	int refOffset;
	
	/**
	 * Add an x-reference for an object that will exist in the PDF
	 * @param objectId
	 * @param byteOffset
	 */
	protected void addXref(int objectId, int byteOffset)
	{
		refs.add(new Ref(objectId, byteOffset));
	}
	
	/**
	 * Simple setter for the offset of all ref data
	 * @param refOffset
	 */
	protected void setRefOffset(int refOffset) { this.refOffset = refOffset; }
	
	/**
	 * Output the actual object in PDF format
	 */
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		
		//Standard xref header info
		ret.append("xref").append(NEW_LINE);
		// +1 to account for the top line (0000000000 65535 f )
		ret.append("0 " + (refs.size()+1)).append(NEW_LINE);
		//must be 20 bytes long including EOL marker
		ret.append("0000000000 65535 f ").append(NEW_LINE);
		
		//Get ready to output all the references in order by sorting them
		Collections.sort(refs);
		for (Ref ref : refs)
		{
			//must be 20 bytes long including EOL marker
			ret.append(ref.getByteOffset()).append(" ").append("00000 n ").append(NEW_LINE);
		}
		
		ret.append("trailer").append(NEW_LINE);
		ret.append("<<").append(NEW_LINE);
		// from the pdf spec:
		/*
		 * The total number of entries in 
		 * the file’s cross-reference table, as defined by the combination of the original
		 * section and all update sections. Equivalently, this value shall be 1 greater
		 * than the highest object number defined in the file.
		 */
		ret.append("  /Size " + (refs.size()+1)).append(NEW_LINE);
		ret.append("  /Root 1 0 R").append(NEW_LINE);
		ret.append(">>").append(NEW_LINE);
		ret.append("startxref").append(NEW_LINE);
		ret.append(refOffset).append(NEW_LINE);
		
		return ret.toString();
	}
	
	/**
	 * A simple inner class to represent a single reference 
	 *
	 */
	private class Ref implements Comparable<Ref>
	{
		private int objectId;
		private int byteOffset;
		
		/**
		 * Construct a x-reference using all the attributes that make it up
		 * @param objectId
		 * @param byteOffset
		 */
		public Ref(int objectId, int byteOffset)
		{
			this.objectId = objectId;
			this.byteOffset = byteOffset;
		}
		
		/**
		 * After an element has been written out and its offset set, its byte offset can be retrieved for use in writing
		 * @return
		 */
		public String getByteOffset()
		{
			StringBuilder ret = new StringBuilder();
			ret.append(byteOffset);
			while (ret.length() < 10) ret.insert(0, "0");
			return ret.toString();
		}
		
		/**
		 * A standard sorter
		 */
		@Override
		public int compareTo(Ref ref)
		{
			if (this.objectId < ref.objectId) return -1;
			if (this.objectId > ref.objectId) return 1;
			return 0;
		}

	}
}