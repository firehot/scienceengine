package com.mazalearn.gwt.server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

public class PdfCertificateMaker {

  public static void makeCertificate(ServletContext servletContext, String userName, String dateStr, OutputStream outputStream) {
    PDDocument document = null;
    try {
      InputStream inp = servletContext.getResourceAsStream("/assets/data/Electromagnetism/EMReview.pdf");
        document = PDDocument.load( inp );
        // Get the first page
        PDPage page = (PDPage)document.getDocumentCatalog().getAllPages().get( 0 );
        @SuppressWarnings("unchecked")
        List<PDAnnotation> annotations = page.getAnnotations();

        // Setup some basic reusable objects/constants
        // Annotations themselves can only be used once!

        float inch = 72;
        PDGamma colourRed = new PDGamma();
        colourRed.setR(1);
        PDGamma colourBlue = new PDGamma();
        colourBlue.setB(1);
        PDGamma colourWhite = new PDGamma();
        colourWhite.setB(1);
        colourWhite.setG(1);
        colourWhite.setR(1);

        PDBorderStyleDictionary borderThick = new PDBorderStyleDictionary();
        borderThick.setWidth(inch/12);  // 12th inch
        PDBorderStyleDictionary borderThin = new PDBorderStyleDictionary();
        borderThin.setWidth(inch/72); // 1 point
        PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
        borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
        borderULine.setWidth(inch/72); // 1 point


        float pw = page.getMediaBox().getUpperRightX();
        float ph = page.getMediaBox().getUpperRightY();


        // Position the name 
        PDRectangle namePosition = new PDRectangle();
        namePosition.setLowerLeftX(pw - (6 * inch));  // 1" in from right, 1" wide
        namePosition.setLowerLeftY(ph - (float)(4.1 * inch)); // 1" height, 3.5" down
        namePosition.setUpperRightX(pw - 4 * inch); // 1" in from right
        namePosition.setUpperRightY(ph- (float)(4.5*inch)); // 3.5" down

        // Now add the markup annotation for name
        PDAnnotationTextMarkup name = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_FREETEXT);
        int n = 20 - userName.length();
        String spaces = new String(new char[n]).replace('\0', ' ');
        name.setContents("\n" + spaces + userName.toUpperCase() + spaces);
        name.setRectangle(namePosition);        
        annotations.add(name);
        
        // Add rectangular red border for name
        PDAnnotationSquareCircle aSquare =
            new PDAnnotationSquareCircle( PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
        aSquare.setColour(colourRed);
        aSquare.setBorderStyle(borderThin);
        aSquare.setRectangle(namePosition);
        annotations.add(aSquare);

         
        // Position the date 
        PDRectangle datePosition = new PDRectangle();
        datePosition.setLowerLeftX(pw - 3.4f * inch);  // 1" in from right, 1" wide
        datePosition.setLowerLeftY(ph - 5.25f * inch); // 1" height, 3.5" down
        datePosition.setUpperRightX(pw - 1.9f * inch); // 1" in from right
        datePosition.setUpperRightY(ph - 5.75f * inch); // 3.5" down

        // Now add the markup annotation for name
        PDAnnotationTextMarkup date = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_FREETEXT);
        n = 16 - dateStr.length();
        spaces = new String(new char[n]).replace('\0', ' ');
        date.setContents("\n" + spaces + dateStr + spaces);
        date.setRectangle(datePosition);        
        annotations.add(date);
        
        // save document to a new file
        document.save(outputStream);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (COSVisitorException e) {
      e.printStackTrace();
    } finally {
        if( document != null ) {
            try {
              document.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
        }
    }
  }
}
