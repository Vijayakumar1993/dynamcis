package org.dynamics.reports;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.dynamics.model.Event;
import org.dynamics.model.Match;
import org.dynamics.model.Person;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class EventReport implements Report{

    private PdfWriter pdfWriter;
    private Document doc;
    public EventReport(String fileName) throws IOException, DocumentException {
        this.doc = new Document(PageSize.LETTER,1f,1f,1f,1f);
        this.pdfWriter = PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get(fileName)));
        System.out.println("PDF Document created successfully...!");
    }
    @Override
    public void generateReport( Event event) throws DocumentException {
        doc.open();

        String eventName = event.getEventName();
        String eventDescription = event.getDescription();
        Paragraph header = new Paragraph(eventName+"\n"+eventDescription);
        header.setAlignment(Paragraph.ALIGN_CENTER);
        doc.add(header);

        doc.add(new Paragraph("\n\n"));

        List<Match> matches = event.getMatcher().getMatches();
        PdfPTable table = new PdfPTable(5);
        PdfPCell serionNumber = getPdfCell("S.No");
        table.addCell(serionNumber);

        table.addCell(getPdfCell("Match Id"));
        table.addCell(getPdfCell("Blue Corner"));
        table.addCell(getPdfCell("Red Corner"));
        table.addCell(getPdfCell("Winner"));
        table.setWidths(new float[]{0.5f,2f,2f,2f,2f});

        matches.forEach(a->{
            Person from = a.getFrom();
            Person to = a.getTo();
            Person successor = a.getSuccessor();
            String fromValue = from.getName().concat("\n(").concat(from.getId()+"").concat(")");
            String toValue = to.getName().concat("\n(").concat(to.getId()+"").concat(")");

            addStringCell((matches.indexOf(a)+1)+"",table);
            addStringCell(a.getMatchId().toString(),table);
            addStringCell(fromValue,table);
            addStringCell(toValue,table);

            if(successor.getId()!=0){
                String successorValue = successor.getName().concat("\n(").concat(successor.getId()+"").concat(")");
                addStringCell(successorValue,table);
            }else{
                addStringCell("",table);
            }
        });
        doc.add(table);
        doc.close();
        this.pdfWriter.close();
        JOptionPane.showMessageDialog(null, "PDF Generated successfully.");

    }

    public PdfPCell getPdfCell(String msg){
        PdfPCell cell = new PdfPCell(new Phrase(msg,new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,BaseColor.WHITE)));
        cell.setBackgroundColor(BaseColor.BLACK);
        return  cell;
    }

    public void addStringCell(String msg,PdfPTable table){
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM); // Only horizontal borders
        Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        cell.addElement(new Phrase(msg, font)); // Add plain text
        cell.setFixedHeight(30f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

}
