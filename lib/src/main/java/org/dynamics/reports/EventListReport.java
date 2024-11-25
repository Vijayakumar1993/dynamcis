package org.dynamics.reports;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.dynamics.model.Event;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class EventListReport implements Report{
    private PdfWriter pdfWriter;
    private Document doc;

    public EventListReport(String fileName) throws IOException, DocumentException {
        this.doc = new Document(PageSize.LETTER,1f,1f,1f,1f);
        this.pdfWriter = PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get(fileName)));
        System.out.println("PDF Document created successfully...!");
    }
    @Override
    public void generateReport(Event event) throws DocumentException {

    }

    @Override
    public void generateReport(List<Event> event,String reportTitle ) throws DocumentException {
        if(!event.isEmpty()){
            doc.open();
            if(reportTitle!=""){
                reportTitle = reportTitle.replace("\\n","\n");
                Paragraph header = new Paragraph(reportTitle);
                header.setAlignment(Paragraph.ALIGN_CENTER);
                doc.add(header);
            }

            doc.add(new Paragraph("\n"));
            PdfPTable table = new PdfPTable(5);
            table.addCell(getPdfCell("Order"));
            table.addCell(getPdfCell("Bout"));
            table.addCell(getPdfCell("Category"));
            table.addCell(getPdfCell("Corner Name"));
            table.addCell(getPdfCell("Team Name"));
            table.setWidths(new float[]{1f,2f,2f,2f,2f});
            event.forEach(ev->{
                addStringCell((event.indexOf(ev)+1)+"",table);
                addStringCell(ev.getId().toString(),table);
                addStringCell(ev.getEventName().toString(),table);
                PdfPTable innerTable = new PdfPTable(2);

                ev.getMatcher().getMatches().forEach(a->{
                    System.out.println(a.getFrom().getName());
                    addStringCell(a.getFromCorner().toString(),innerTable);
                    addStringCell(a.getFrom().getName(),innerTable);
                    addStringCell(a.getToCorner().toString(),innerTable);
                    addStringCell(a.getTo().getName(),innerTable);
                });
                table.addCell(innerTable);
                addStringCell(ev.getTeamName().toString(),table);
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

    public PdfPCell addStringCell(String msg, PdfPTable table){
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM); // Only horizontal borders
        Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        cell.addElement(new Phrase(msg, font)); // Add plain text
        cell.setFixedHeight(30f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        return cell;
    }
}
