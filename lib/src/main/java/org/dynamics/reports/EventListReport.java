package org.dynamics.reports;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.dynamics.model.Event;

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
    private Consumer<PdfPCell> defaultCellOptions = (a)->System.out.println("default Supplier");

    public EventListReport(String fileName) throws IOException, DocumentException {
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


            event.forEach(ev->{
                try {
                    PdfPTable table = new PdfPTable(5);
                    table.addCell(getPdfCell("Order"));
                    table.addCell(getPdfCell("Bout"));
                    table.addCell(getPdfCell("Weight"));
                    table.addCell(getPdfCell("Corner Name"));
                    table.addCell(getPdfCell("Team"));
                    table.setWidths(new float[]{1f,1f,2f,2f,2f});
                    ev.getMatcher().getMatches().forEach(a->{
                        addStringCell((ev.getMatcher().getMatches().indexOf(a)+1)+"",table,defaultCellOptions);
                        addStringCell(a.getMatchId().toString(),table,defaultCellOptions);
                        addStringCell(ev.getTeamName().toString(),table,defaultCellOptions);

                        //corner table
                        PdfPTable innerTable = new PdfPTable(2);
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
                    doc.add(table);doc.add(new Paragraph("\n"));
                } catch (DocumentException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,e.getMessage());
                }

            });
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
        cell.setFixedHeight(30f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellOptions.accept(cell);
        table.addCell(cell);
        return cell;
    }
}
