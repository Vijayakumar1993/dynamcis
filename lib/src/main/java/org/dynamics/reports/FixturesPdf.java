package org.dynamics.reports;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.dynamics.model.Configuration;
import org.dynamics.model.Event;
import org.dynamics.model.Person;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FixturesPdf implements Report{
    private PdfWriter pdfWriter;
    private Document doc;
    private Configuration configuration;
    private Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    private Font H1 = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
    private Font H2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private Font H3 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private Font H4 = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
    public FixturesPdf(String fileName, Configuration configuration) throws IOException, DocumentException {
        this.configuration = configuration;
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
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                watermarkImage.setAbsolutePosition(100, 200); // Position of the watermark image
                watermarkImage.scaleToFit(400, 400); // Resize the image to fit
//                watermarkImage.setRotationDegrees(45); // Rotate image if needed
                try {
                    canvas.addImage(watermarkImage);
                } catch (DocumentException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        System.out.println("PDF Document created successfully...!");
    }

    @Override
    public void generateReport(Event event) throws DocumentException {
        doc.open();
        PdfPTable titleTable = new PdfPTable(3);
        titleTable.setWidthPercentage(100);
        Paragraph titleParagraph = new Paragraph();
        try {
            Image img = Image.getInstance((String)configuration.get("left-logo"));
            img.scaleToFit(100, 100);
            img.setAlignment(Image.ALIGN_LEFT); // Align image to center

            PdfPCell imageCell = new PdfPCell();
            imageCell.addElement(img);
            imageCell.setBorder(0);
            titleTable.addCell(imageCell);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Chunk categoryName = new Chunk(event.getEventName(),H1);
        titleParagraph.add(categoryName);
        titleParagraph.add("\n");
        Chunk teamName = new Chunk(event.getTeamName(),H2);
        titleParagraph.add(teamName);
        titleParagraph.add("\n");
        Chunk headerTitle =new Chunk("Players List",H3);
        titleParagraph.add(headerTitle);
        titleParagraph.add("\n");
        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);


        PdfPCell titleCell = new PdfPCell();
        titleCell.addElement(titleParagraph);
        titleCell.setBorder(0);
        titleTable.addCell(titleCell);
        try {
            Image img1 = Image.getInstance((String)configuration.get("right-logo"));
            img1.scaleToFit(100, 100);
            img1.setAlignment(Image.ALIGN_RIGHT); // Align image to center
            PdfPCell rightCell = new PdfPCell();
            rightCell.addElement(img1);
            rightCell.setBorder(0);
            titleTable.addCell(rightCell);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Person> persons = event.getFixture().getPersons();
        titleTable.setSpacingAfter(10);
        doc.add(titleTable);
        LineSeparator lineSeparator =  new LineSeparator();
        doc.add(lineSeparator);

        Paragraph totalFixtures =new Paragraph("Total No of Players: "+persons.size(),H3);
        totalFixtures.setAlignment(Paragraph.ALIGN_CENTER);
        totalFixtures.setSpacingAfter(10);
        totalFixtures.setSpacingAfter(10);
        doc.add(totalFixtures);
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        Person.keys().forEach(key->{
            table.addCell(getPdfCell(key));
        });

        persons.forEach(person->{
            addStringCell(person.getId()+"",table);
            addStringCell(person.getName(),table);
            addStringCell(person.getGender().toString(),table);
            addStringCell(person.getCategories().toString(),table);
            addStringCell(person.getWeight().toString(),table);
            addStringCell(person.getTeamName(),table);
        });

        doc.add(table);
        doc.add(new LineSeparator());
        doc.close();
        JOptionPane.showMessageDialog(null, "PDF Generated successfully.");
    }
    public PdfPCell getPdfCell(String msg) {
        PdfPCell cell = new PdfPCell(new Phrase(msg, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
        cell.setBackgroundColor(BaseColor.BLACK);
        return cell;
    }

    public void addStringCell(String msg, PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        cell.addElement(new Phrase(msg, font)); // Add plain text
        cell.setFixedHeight(20f);
        cell.setPadding(0);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    @Override
    public void generateReport(List<Event> event, String title) throws DocumentException {

    }
}
