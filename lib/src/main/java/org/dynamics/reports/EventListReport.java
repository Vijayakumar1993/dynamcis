package org.dynamics.reports;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dynamics.model.Configuration;
import org.dynamics.model.Event;
import org.dynamics.reader.Reader;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EventListReport implements Report{
    private static final Logger logger = LogManager.getLogger(Reader.class);
    private PdfWriter pdfWriter;
    private Document doc;
    private Configuration configuration;
    private Font NORMAL_FONT = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
    private Font H1 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private Font H2 = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.BOLD);
    private Font H3 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    private Font H4 = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD);
    private Consumer<PdfPCell> defaultCellOptions = (a)->logger.info("default Supplier");
    private String fileName;
    public EventListReport(String fileName,Configuration configuration) throws IOException, DocumentException {
        this.configuration = configuration;
        this.fileName = fileName;
        this.doc = new Document(PageSize.LETTER,1f,1f,1f,1f);
        this.doc.setMargins(30, 30, 20, 50);
        this.pdfWriter = PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get(fileName)));
        this.pdfWriter.setPageEvent(new PdfPageEventHelper(){
            @Override
            public void onEndPage(PdfWriter writer, Document doc) {
                ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase("Page "+writer.getPageNumber(),H4),
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
    @Override
    public void generateReport(Event event) throws DocumentException {

    }

    @Override
    public void generateReport(List<Event> event,String reportTitle ) throws DocumentException {
        if(!event.isEmpty()){
            doc.open();
            PdfPTable titleTable = new PdfPTable(3);
            titleTable.setWidthPercentage(100);
            titleTable.setWidths(new float[]{2f,5f,2f});
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
                logger.error("An error occurred", e);
                e.printStackTrace();
            }


            Chunk titleName = new Chunk((String)configuration.get("title"),H1);
            titleParagraph.add(titleName);
            titleParagraph.add("\n");
            Chunk clubTitle = new Chunk((String)configuration.get("club-title"),H2);
            titleParagraph.add(clubTitle);
            titleParagraph.add("\n");
            Chunk address = new Chunk((String)configuration.get("address"),H3);
            titleParagraph.add(address);
            titleParagraph.add("\n");
            Chunk website = new Chunk((String)configuration.get("website"),H4);
            titleParagraph.add(website);
            titleParagraph.add("\n");

            Long ttlBts = event.stream().map(ev->ev.getMatcher().getMatches().stream().filter(m->!m.isPrimary()).collect(Collectors.toList()).stream().count()).reduce(Long::sum).orElse(0L);
            Chunk totalBouts = new Chunk("Total No of Bout: "+ttlBts,H3);
            titleParagraph.add(totalBouts);
            titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);


            PdfPCell titleCell = new PdfPCell();
            titleCell.addElement(titleParagraph);
            titleCell.setBorder(0);
            titleTable.addCell(titleCell);
            titleTable.setWidthPercentage(100);
            try {
                Image img1 = Image.getInstance((String)configuration.get("right-logo"));
                img1.scaleToFit(100, 100);
                img1.setAlignment(Image.ALIGN_RIGHT); // Align image to center
                PdfPCell rightCell = new PdfPCell();
                rightCell.addElement(img1);
                rightCell.setBorder(0);
                titleTable.addCell(rightCell);
            } catch (IOException e) {
                logger.error("An error occurred", e);
                e.printStackTrace();
            }
            titleTable.setSpacingAfter(10);
            doc.add(titleTable);
            doc.add(new LineSeparator());
            doc.add(new Paragraph("\n"));
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell(getPdfCell("Order"));
            table.addCell(getPdfCell("Bout"));
            table.addCell(getPdfCell("Category"));
            table.addCell(getPdfCell("Corner"));
            table.addCell(getPdfCell("Team"));
            table.setWidths(new float[]{1f,1f,2f,2f,2f});
            final Integer[] boutIndex = {1};
            try{
                boutIndex[0] = Integer.parseInt(JOptionPane.showInputDialog("Please enter bout starting index"));
            }catch (Exception e){
                logger.error("An error occurred", e);
                JOptionPane.showMessageDialog(null, "Invalid "+e.getMessage());
                throw new IllegalArgumentException("Invalid, Please put number only");
            }

            final int[] index = {0};
            event.forEach(ev->{
                try {
                    ev.getMatcher().getMatches().stream().filter(m->!m.isPrimary()).forEach(a->{
                        index[0] = index[0] +1;
                        boutIndex[0] = boutIndex[0] +1;
                        addStringCell((index[0])+"",table,defaultCellOptions);
                        addStringCell((boutIndex[0])+"",table,defaultCellOptions);
                        String eventNameDetailed = ev.getTeamName()+"\n"+ev.getEventName();
                        addStringCell(eventNameDetailed,table,defaultCellOptions);

                        //corner table
                        PdfPTable innerTable = new PdfPTable(2);
                        try {
                            innerTable.setWidths(new float[]{1f,2f});
                        } catch (DocumentException e) {
                            logger.error("An error occurred", e);
                            e.printStackTrace();
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
                        addStringCell(a.getTo().getTeamName(),teamTable,borderBottomCellOptions);
                        addStringCell(a.getFrom().getTeamName(),teamTable,noBorder);
                        table.addCell(teamTable);
                    });
                } catch (Exception e) {
                    logger.error("An error occurred", e);
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,e.getMessage());
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
        }
    }

    public PdfPCell getPdfCell(String msg){
        PdfPCell cell = new PdfPCell(new Phrase(msg,new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD,BaseColor.WHITE)));
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
