package org.dynamics.reports;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.dynamics.model.Configuration;
import org.dynamics.model.Event;
import org.dynamics.model.Match;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EventReport implements Report{
    private Configuration configuration;
    private PdfWriter pdfWriter;
    private Document doc;
    private Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    private Font H1 = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
    private Font H2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private Font H3 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private Font H4 = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
    public EventReport(String fileName, Configuration configuration) throws IOException, DocumentException {
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
    public void generateReport( Event event) throws DocumentException {
        doc.open();
        List<Match> matches = event.getMatcher().getMatches();

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

        String title = (String)configuration.get("title");
        Chunk titleName = new Chunk(title,H1);
        titleParagraph.add(titleName);
        titleParagraph.add("\n");
        Chunk categoryName = new Chunk(event.getEventName(),H2);
        titleParagraph.add(categoryName);
        titleParagraph.add("\n");
        Chunk teamName = new Chunk(event.getTeamName(),H2);
        titleParagraph.add(teamName);
        titleParagraph.add("\n");
        Chunk drawSheet = new Chunk("Draw Sheet",H3);
        titleParagraph.add(drawSheet);
        titleParagraph.add("\n");
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
        doc.add(titleTable);
        doc.add(new LineSeparator());

        Paragraph asOfDate = new Paragraph("As of "+ DateTimeFormatter.ofPattern("EEE dd MMM yyyy").format(event.getEventDate()),H3);
        asOfDate.setAlignment(Paragraph.ALIGN_CENTER);
        doc.add(asOfDate);


        Paragraph noOfBoxers = new Paragraph("No of Boxers :"+noOFBoxers(event),H3);
        noOfBoxers.setAlignment(Paragraph.ALIGN_CENTER);
        doc.add(noOfBoxers);
        doc.add(new Paragraph("\n"));
        PdfPTable table = new PdfPTable(3);

        class Content{
            private String message;
            private Integer border;
            public Content(String message, Integer border){
                this.message = message;
                this.border = border;
            }
        }
        table.setWidthPercentage(100);
        table.addCell(getPdfCell("Team"));
        table.addCell(getPdfCell("Name"));
        table.addCell(getPdfCell(" "));
        List<List<Content>> options = new LinkedList<>();
        matches.forEach(match->{
            List<Content> innerList = new LinkedList<>();
            innerList.add(new Content(" ",Rectangle.NO_BORDER));
            innerList.add(new Content(" ",Rectangle.NO_BORDER));
            innerList.add(new Content(" ",Rectangle.NO_BORDER));
            innerList.add(new Content(match.getFrom().getTeamName(),Rectangle.BOTTOM));
            innerList.add(new Content(match.getFrom().getName(),Rectangle.BOTTOM));
            if(!match.isPrimary()){
                innerList.add(new Content(" ",Rectangle.NO_BORDER));
                innerList.add(new Content(" ",Rectangle.NO_BORDER));
                innerList.add(new Content(" ",Rectangle.RIGHT));
                innerList.add(new Content(" ",Rectangle.BOTTOM));
                innerList.add(new Content(match.getTo().getTeamName(),Rectangle.BOTTOM ));
                innerList.add(new Content(match.getTo().getName(),Rectangle.BOTTOM | Rectangle.RIGHT));
                innerList.add(new Content(" ",Rectangle.NO_BORDER));
            }else{
                innerList.add(new Content(" Bye ",Rectangle.BOTTOM));
            }
            options.add(innerList);
        });

        options.forEach(a->{
            a.stream().filter(Objects::nonNull).forEach(c->{
                PdfPCell cell = new PdfPCell();
                cell.setBorder(c.border);
                cell.addElement(new Phrase(c.message,NORMAL_FONT));
                table.addCell(cell);
            });
        });
        doc.add(table);


        doc.close();
        this.pdfWriter.close();
        JOptionPane.showMessageDialog(null, "PDF Generated successfully.");

    }
    public long noOFBoxers(Event e){
        List<Match> matches = e.getMatcher().getMatches();
        long fixturesCount  = matches.stream().filter(Match::isPrimary).count();
        long matcherCount = matches.stream().filter(m->!m.isPrimary()).count()*2;
        return fixturesCount+matcherCount;
    }
    public static List<List<String>> transpose(List<List<String>> original) {
        // Find the maximum number of columns in any row
        int maxCols = 0;
        for (List<String> row : original) {
            maxCols = Math.max(maxCols, row.size());
        }

        List<List<String>> result = new LinkedList<>();

        // For each column index, create a new row for the transposed result
        for (int col = 0; col < maxCols; col++) {
            List<String> newRow = new LinkedList<>();
            for (List<String> row : original) {
                // If the current row has enough columns, add the element, otherwise add null
                if (col < row.size()) {
                    newRow.add(row.get(col));
                } else {
                    newRow.add(null); // Or use an empty string, or any placeholder value
                }
            }
            result.add(newRow);
        }

        return result;
    }
    @Override
    public void generateReport(List<Event> event,String title) throws DocumentException {

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
        cell.setFixedHeight(30f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

}
