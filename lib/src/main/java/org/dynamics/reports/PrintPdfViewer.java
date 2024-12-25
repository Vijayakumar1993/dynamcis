package org.dynamics.reports;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.dynamics.ui.CommonFrame;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class PrintPdfViewer {
    private static final Logger logger = LogManager.getLogger(PrintPdfViewer.class);
    private String fileName ;
    public PrintPdfViewer(String fileName){
        this.fileName = fileName;
    }
    public void view(){
        SwingUtilities.invokeLater(() -> {
            try (PDDocument document = PDDocument.load(Files.newInputStream(Paths.get(this.fileName)))) {
                PDFRenderer renderer = new PDFRenderer(document);

                // Create a JPanel to hold all the pages
                JPanel pdfPanel = new JPanel();
                pdfPanel.setLayout(new BoxLayout(pdfPanel, BoxLayout.Y_AXIS));

                // Render each page and add to the panel
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 150); // Render page at 150 DPI
                    JLabel pageLabel = new JLabel(new ImageIcon(image));
                    pdfPanel.add(pageLabel);
                    pdfPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between pages
                }

                // Create a scroll pane to make the panel scrollable
                JScrollPane scrollPane = new JScrollPane(pdfPanel);
                scrollPane.getVerticalScrollBar().setUnitIncrement(50); // Smooth scrolling

                // Create the main JFrame
                JFrame printFrame = new JFrame("PDF Viewer");
                Image icon = ImageIO.read(Objects.requireNonNull(CommonFrame.class.getResource("/logo.png")));
                printFrame.setIconImage(icon); // Set the icon for the JFrame
                printFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                printFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                // Add the scroll pane to the center of the BorderLayout
                printFrame.setLayout(new BorderLayout());
                printFrame.add(scrollPane, BorderLayout.CENTER);
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
                JButton deletePdf = new JButton("Delete PDF");
                buttonPanel.add(deletePdf);
                JButton printPdf = new JButton("Print PDF");
                buttonPanel.add(printPdf);
                printFrame.add(buttonPanel, BorderLayout.EAST);


                printPdf.addActionListener(e->{
                    PrinterJob printerJob = PrinterJob.getPrinterJob();
                    try (PDDocument dec = PDDocument.load(new File(this.fileName))){
                        // Set the PDF document as the printable object
                        printerJob.setPageable(new PDFPageable(dec));

                        // Optionally: Choose a specific print service
                        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
                        if (defaultService != null) {
                            try {
                                printerJob.setPrintService(defaultService);
                            } catch (PrinterException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            logger.info("No default print service found. Printing to the system's default.");
                        }

                        // Display a print dialog and print the document
                        if (printerJob.printDialog()) {
                            try {
                                printerJob.print();
                            } catch (PrinterException ex) {
                                ex.printStackTrace();
                            }
                            logger.info("Printing initiated.");
                        } else {
                            logger.info("Print job cancelled.");
                        }
                    } catch (Exception e11) {
                        logger.error("An error occurred", e11);
                        System.err.println("Failed to load the PDF file: " + e11.getMessage());
                    }

                });

                deletePdf.addActionListener(e->{
                    try {
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure?");
                        if(result == JOptionPane.YES_OPTION){
                            Files.delete(Paths.get(this.fileName));
                            JOptionPane.showMessageDialog(null,this.fileName+" is deleted successfully...!");
                            printFrame.setVisible(false);
                        }
                    } catch (IOException ex) {
                        logger.error("An error occurred", ex);
                        ex.printStackTrace();
                    }
                });

                // Make the frame visible
                printFrame.setVisible(true);

            } catch (IOException e) {
                logger.error("An error occurred", e);
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error loading PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
