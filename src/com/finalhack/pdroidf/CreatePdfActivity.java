package com.finalhack.pdroidf;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.finalhack.pdroidf.source.Pages;
import com.finalhack.pdroidf.source.Pdf;
import com.finalhack.pdroidf.source.Stream;

public class CreatePdfActivity extends Activity {

	private static final int PDF_HEADER_FONT_SIZE = 16;
	private static final String RED = "ff0000";
	private static final String BLUE = "0000ff";
	private static final String GREEN = "00ff00";
	private static final int LINE_WIDTH = 10;
	private static final int JPG_WIDTH = 50;
	private static final int JPG_HEIGHT = 50;
	private static final String JPG_SRC = "someFile.jpg";
	private static final String PDF_PDFNAME = "my.pdf";
	private static final String MIME_TYPE_PDF = "application/pdf";

	private EditText userText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_pdf);

		userText = (EditText) findViewById(R.id.textView1);
	}

	// This is where the PDF is created.
	// It still needs to be written out to a file or streamed somewhere.
	public void onCreatePdf(View view) {

		// Images used in the PDF should be JPG images, stored as Assets
		byte[] stickerBmp = Pdf.getAssetJpgBytes(this, JPG_SRC);

		// Create a new PDF
		Pdf pdf = new Pdf();
		Stream stream = pdf.addPage();

		// Add some text at a specific position
		// The coordinate system is setup where the origin (0,0) is at the
		// bottom left corner
		stream.addText("This is a test pdf.", PDF_HEADER_FONT_SIZE, 100, 50, RED);

		// Add some text with a different color
		stream.addText(userText.getText().toString(), BLUE, PDF_HEADER_FONT_SIZE);

		// Add a line with a color and a width
		stream.addHorizontalLine(GREEN, LINE_WIDTH);

		// Add an image at a specific coordinate (top right corner of the page)
		stream.addBmpImage(Pages.PAGE_WIDTH - (int) Stream.MARGIN_HORIZONTAL - JPG_WIDTH, Pages.PAGE_HEIGHT - (int) Stream.MARGIN_VERTICAL - JPG_HEIGHT,
				JPG_WIDTH, JPG_HEIGHT, stickerBmp);

		// Get the raw PDF bytes like this:
		byte[] pdfBytes = pdf.getPDF();

		// Optionally, write the bytes out to a file:
		File file = Pdf.writePdf(this, pdfBytes, PDF_PDFNAME);

		// Show the PDF in an external viewer
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, MIME_TYPE_PDF);
		startActivity(intent);

	}

}
