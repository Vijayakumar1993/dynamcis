package org.dynamics.reports;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dynamics.model.Configuration;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MedalReport {
    private static final Logger logger = LogManager.getLogger(MedalReport.class);
    private String fileName;
    private String categoryName;
    private Configuration configuration;
    private PdfWriter pdfWriter;
    private Document doc;
    private Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    private Font H1 = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
    private Font H2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private Font H3 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private Font H4 = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
    public MedalReport(String fileName, String categoryName, Configuration configuration) throws IOException, DocumentException {
        this.categoryName = categoryName;
        this.configuration = configuration;
        this.fileName = fileName;
        this.doc = new Document(PageSize.A4
                ,3f,3f,3f,3f);
        this.doc.setMargins(30, 30, 20, 50);
        this.pdfWriter = PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get(fileName)));
        this.pdfWriter.setPageEvent(new PdfPageEventHelper(){
            @Override
            public void onEndPage(PdfWriter writer, Document doc) {
                ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase("Page " + writer.getPageNumber()),
                        (doc.left() + doc.right()) / 2, doc.bottom()-20 , 0);

                //watermark
                PdfContentByte canvas = writer.getDirectContentUnder();
                Image watermarkImage = null; // Path to your image
                try {
                    watermarkImage = Image.getInstance((String)configuration.get("watermark-logo"));
                } catch (BadElementException e) {
                    logger.error("An error occurred", e);
                    e.printStackTrace();
                } catch (IOException e) {
                    logger.error("An error occurred", e);
                    e.printStackTrace();
                }
                watermarkImage.setAbsolutePosition(100, 200); // Position of the watermark image
                watermarkImage.scaleToFit(400, 400); // Resize the image to fit
//                watermarkImage.setRotationDegrees(45); // Rotate image if needed
                try {
                    canvas.addImage(watermarkImage);
                } catch (DocumentException e) {
                    logger.error("An error occurred", e);
                    e.printStackTrace();
                }
            }
        });
        logger.info("PDF Document created successfully...!");
    }

    public void generateReport(List<String> columns, List<List<String>> rows)  throws DocumentException {
        doc.open();

        PdfPTable titleTable = new PdfPTable(3);
        titleTable.setWidthPercentage(100);
        Paragraph titleParagraph = new Paragraph();
        try {
            Image img = Image.getInstance((String)configuration.get("left-logo"));
            img.scaleToFit(70, 70);
            img.setAlignment(Image.ALIGN_LEFT); // Align image to center

            PdfPCell imageCell = new PdfPCell();
            imageCell.addElement(img);
            imageCell.setBorder(0);
            titleTable.addCell(imageCell);
        } catch (IOException e) {
            logger.error("An error occurred", e);
            e.printStackTrace();
        }

        String title = (String)configuration.get("title");
        Chunk titleName = new Chunk(title,H1);
        titleParagraph.add(titleName);
        titleParagraph.add("\n");
        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        PdfPCell titleCell = new PdfPCell();
        titleCell.addElement(titleParagraph);
        titleCell.setBorder(0);
        titleTable.addCell(titleCell);
        try {
            Image img1 = Image.getInstance((String)configuration.get("right-logo"));
            img1.scaleToFit(70, 70);
            img1.setAlignment(Image.ALIGN_RIGHT); // Align image to center
            PdfPCell rightCell = new PdfPCell();
            rightCell.addElement(img1);
            rightCell.setBorder(0);
            titleTable.addCell(rightCell);
        } catch (IOException | BadElementException e) {
            logger.error("An error occurred", e);
            e.printStackTrace();
        }
        doc.add(titleTable);
        doc.add(new LineSeparator());

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        columns.forEach(key->{
            table.addCell(getPdfCell(key));
        });
        rows.forEach(row->{
            for (String s : row) {
                addStringCell(s, table);
            }
        });

        doc.add(table);
        doc.close();
        this.pdfWriter.close();
        int output = JOptionPane.showConfirmDialog(null, "Do you want to show the PDF?");
        if(output == JOptionPane.YES_OPTION){
            PrintPdfViewer pdfViewer = new PrintPdfViewer(this.fileName);
            pdfViewer.view();
        }
        JOptionPane.showMessageDialog(null, "PDF Generated successfully.");
    }

    public PdfPCell getPdfCell(String msg) {
        PdfPCell cell = new PdfPCell(new Phrase(msg, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
        cell.setBackgroundColor(BaseColor.BLACK);
        cell.setNoWrap(false);
        return cell;
    }

    public void addStringCell(String msg, PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        cell.setNoWrap(false);
        cell.setPadding(10);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.addElement(new Phrase(msg, font)); // Add plain text
        table.addCell(cell);
    }

}
