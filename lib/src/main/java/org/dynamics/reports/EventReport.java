package org.dynamics.reports;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.dynamics.model.Event;
import org.dynamics.model.Fixture;
import org.dynamics.model.Match;
import org.dynamics.model.Person;
import org.dynamics.util.Utility;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class EventReport implements Report{

    private PdfWriter pdfWriter;
    private Document doc;
    public EventReport(String fileName) throws IOException, DocumentException {
        this.doc = new Document(PageSize.A1,1f,1f,1f,1f);
        this.pdfWriter = PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get(fileName)));
        this.pdfWriter.setPageEvent(new PdfPageEventHelper(){
            @Override
            public void onEndPage(PdfWriter writer, Document doc) {
                ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase("Page " + writer.getPageNumber()),
                        (doc.left() + doc.right()) / 2, doc.bottom() + 10, 0);
            }
        });
        System.out.println("PDF Document created successfully...!");
    }
    @Override
    public void generateReport( Event event) throws DocumentException {
        doc.open();
        List<Event>  events = Utility.findListOfEvents(event);
        List<List<String>> data = new LinkedList<>();
        for(Event ev: events){
            List<String> row = new LinkedList<>();
            List<Match> matches = ev.getMatcher().getMatches();
            matches.forEach(match -> {
                    for(int i=0;i<events.indexOf(ev)+2;i++){
                        row.add("");
                    }
                row.add(match.getFrom().getName());
                if(ev.getParentEvent()!=null){
                    for(int i=0;i<events.indexOf(ev)*2+1;i++){
                        row.add("");
                    }
                }else{
                    row.add("");
                }
                row.add(match.getTo().getName());
            });
            data.add(row);
        }

        List<List<String>> newData = transpose(data);
        PdfPTable table = new PdfPTable(events.size());
        newData.forEach(row->{
            List<String> selRow = newData.get(newData.indexOf(row));
            selRow.forEach(sel->{
                addStringCell(selRow.get(selRow.indexOf(sel)),table);
            });
        });
        doc.add(table);
        doc.close();
        this.pdfWriter.close();
        JOptionPane.showMessageDialog(null, "PDF Generated successfully.");

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
