package org.dynamics.reports;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.dynamics.model.Event;
import org.dynamics.model.Fixture;
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

        String eventName = event.getEventName();
        String eventDescription = event.getDescription();
        Paragraph header = new Paragraph(eventName+"("+event.getId()+")\n"+eventDescription);
        header.setAlignment(Paragraph.ALIGN_CENTER);
        if(eventName!=null ) doc.add(header);

        doc.add(new Paragraph("\n"));

        List<Match> matches = event.getMatcher().getMatches();
        List<Person> fixtures = event.getFixture().getPersons();

        if(matches!=null && !matches.isEmpty()){
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
        }


        if(fixtures!=null && !fixtures.isEmpty()){

            PdfPTable fixtrueTable = new PdfPTable(6);
            PdfPCell sn = getPdfCell("S.No");
            fixtrueTable.addCell(sn);
            fixtrueTable.addCell(getPdfCell("Player Id"));
            fixtrueTable.addCell(getPdfCell("Name"));
            fixtrueTable.addCell(getPdfCell("Gender"));
            fixtrueTable.addCell(getPdfCell("Category"));
            fixtrueTable.addCell(getPdfCell("Weight"));
            fixtrueTable.setWidths(new float[]{1f,3f,2f,1f,1f,1f});

            fixtures.forEach(person->{
                addStringCell((fixtures.indexOf(person)+1)+"",fixtrueTable);
                addStringCell(person.getId()+"",fixtrueTable);
                addStringCell(person.getName().concat("\n(").concat(person.getId()+"").concat(")"),fixtrueTable);
                addStringCell(person.getGender().toString(),fixtrueTable);
                addStringCell(person.getCategories().toString(),fixtrueTable);
                addStringCell(person.getWeight().toString(),fixtrueTable);
            });


            Paragraph fixtureHeader = new Paragraph("\nFixtures");
            fixtureHeader.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(fixtureHeader);
            doc.add(new Paragraph("\n"));
            doc.add(fixtrueTable);
        }
        doc.close();
        this.pdfWriter.close();
        JOptionPane.showMessageDialog(null, "PDF Generated successfully.");

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
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM); // Only horizontal borders
        Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        cell.addElement(new Phrase(msg, font)); // Add plain text
        cell.setFixedHeight(30f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

}
