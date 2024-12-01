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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EventListReport implements Report{
    private PdfWriter pdfWriter;
    private Document doc;
    private Configuration configuration;
    private Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    private Font H1 = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
    private Font H2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private Font H3 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private Font H4 = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
    private Consumer<PdfPCell> defaultCellOptions = (a)->System.out.println("default Supplier");

    public EventListReport(String fileName,Configuration configuration) throws IOException, DocumentException {
        this.configuration = configuration;
        this.doc = new Document(PageSize.LETTER,1f,1f,1f,1f);
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

    }

    @Override
    public void generateReport(List<Event> event,String reportTitle ) throws DocumentException {
        if(!event.isEmpty()){
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


            Chunk titleName = new Chunk((String)configuration.get("title"),H2);
            titleParagraph.add(titleName);
            Chunk totalBouts = new Chunk("Total No of Bouts "+event.size(),H3);
            titleParagraph.add(titleName);
            titleParagraph.add("\n");
            titleParagraph.add(totalBouts);
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
            titleTable.setSpacingAfter(10);
            doc.add(titleTable);
            doc.add(new LineSeparator());
            doc.add(new Paragraph("\n"));
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell(getPdfCell("Bout"));
            table.addCell(getPdfCell("Category"));
            table.addCell(getPdfCell("Corner"));
            table.addCell(getPdfCell("Team"));
            table.setWidths(new float[]{1f,2f,2f,2f});
            event.forEach(ev->{
                try {
                    ev.getMatcher().getMatches().stream().filter(m->!m.isPrimary()).forEach(a->{
                        addStringCell(a.getMatchId().toString(),table,defaultCellOptions);
                        String eventNameDetailed = ev.getTeamName()+"\n"+ev.getEventName();
                        addStringCell(eventNameDetailed,table,defaultCellOptions);

                        //corner table
                        PdfPTable innerTable = new PdfPTable(2);
                        try {
                            innerTable.setWidths(new float[]{1f,2f});
                        } catch (DocumentException e) {
                            throw new RuntimeException(e);
                        }
                        Consumer<PdfPCell> borderBottomCellOptions = cell->{
                            cell.setBorder( Rectangle.BOTTOM);
                        };
                        Consumer<PdfPCell> noBorder = cell->{
                            cell.setBorder(0);
                        };
                        addStringCell(a.getToCorner().toString(),innerTable,borderBottomCellOptions);
                        addStringCell(a.getTo().getName(),innerTable,borderBottomCellOptions);
                        addStringCell(a.getFromCorner().toString(),innerTable,noBorder);
                        addStringCell(a.getFrom().getName(),innerTable,noBorder);

                        table.addCell(innerTable);

                        //team table
                        PdfPTable teamTable = new PdfPTable(1);
                        addStringCell(a.getFrom().getTeamName(),teamTable,borderBottomCellOptions);
                        addStringCell(a.getTo().getTeamName(),teamTable,noBorder);
                        table.addCell(teamTable);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,e.getMessage());
                }
            });
            doc.add(table);
            doc.close();
            this.pdfWriter.close();
            JOptionPane.showMessageDialog(null, "PDF Generated successfully.");
        }
    }

    public PdfPCell getPdfCell(String msg){
        PdfPCell cell = new PdfPCell(new Phrase(msg,new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,BaseColor.WHITE)));
        cell.setBackgroundColor(BaseColor.BLACK);
        return  cell;
    }

    public PdfPCell addStringCell(String msg, PdfPTable table, Consumer<PdfPCell> cellOptions){
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM); // Only horizontal borders
        Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        cell.addElement(new Phrase(msg, font)); // Add plain text
        cell.setFixedHeight(20f);
        cell.setPadding(0);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellOptions.accept(cell);
        table.addCell(cell);
        return cell;
    }
}
