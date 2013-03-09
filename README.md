pDroidF - An Android PDF library free to all

This is an example Android project that uses & includes pDroidF.

#Adding it to a project
You can add it in two ways:

    1 - Include the source
    or
    2 - Include the jar

#Using it in a project
    import com.finalhack.pdroidf.*;

    // Instantiate a Pdf class
    Pdf pdf = new Pdf();
    // Add a page to the PDF and save it's Stream
    // A Stream is what you use to add things to a page (PDF Stream)
    Stream stream = pdf.addPage();

    // Add things to your PDF

    // Add some text with a different color
    // Colors are simply hex color (RGB) Strings
    // Font sizes are ints
    stream.addText("Text", "0000ff", 16);

    // You can place text at a location by specifying an x,y coordinate
    // Coordinates are based on a (0,0) origin that is at the bottom left corner of the page
    stream.addText("This is a test pdf.", 16, 100, 50, "ff0000");

    // Add an image at a specific coordinate (top right corner of the page)
    // Images should be JPG files and go nicely in the assets directory
    stream.addBmpImage(Pages.PAGE_WIDTH - (int) Stream.MARGIN_HORIZONTAL - JPG_WIDTH, Pages.PAGE_HEIGHT - (int) Stream.MARGIN_VERTICAL - JPG_HEIGHT, JPG_WIDTH, JPG_HEIGHT, stickerBmp);

#Using PDF output
You can:
    1 - Get the bytes for streaming it somewhere
    or
    2 - Write out a PDF file
    
    // Get the raw PDF bytes like this
    byte[] pdfBytes = pdf.getPDF();

    // Write the bytes out to a file:
    File file = Pdf.writePdf(this, pdfBytes, "my.pdf");

    // Show the PDF in an external viewer
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.fromFile(file);
    intent.setDataAndType(uri, MIME_TYPE_PDF);
    startActivity(intent);

#Happy PDFing!
