/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.dynamics;

import org.dynamics.db.Db;
import org.dynamics.db.LevelDb;
import org.dynamics.model.*;
import org.dynamics.reader.CsvFileReader;
import org.dynamics.reader.Reader;
import org.dynamics.ui.*;
import org.dynamics.util.Utility;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Library  extends CommonFrame {
    private Map<String, Map<String, ActionListener>> men = new LinkedHashMap<>();
    private Map<String, ActionListener> fileMenuItems = new LinkedHashMap<>();
    private Map<String, ActionListener> bouteMenuItems = new LinkedHashMap<>();
    private Map<String, ActionListener> contactUs = new LinkedHashMap<>();
    private Map<String, ActionListener> configurationItems = new LinkedHashMap<>();
    private Db db = new LevelDb();
    public Library(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        super(title);
        JWindow loadingWindow = Utility.createLoadingWindow();
        loadingWindow.setVisible(true);
        Configuration initialConfiguration = new Configuration();
        try {
            initialConfiguration =  db.findObject("configuration");
            if(initialConfiguration==null){
              alert("Configuration is missing, kindly add");
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert(e.getMessage());
        }
        this.commonNorthPanel(db);
        this.commonWestPanel(db);
        this.commonCenterPanel(db);
        this.commonSouthPanal(db);
        contactUs.put("Contact",(ActionEvent e)->{
            JFrame jf = new JFrame("Contact Us");
            jf.setTitle("Contact Us");
            jf.setVisible(true);
            Image icon = null;
            try {
                icon = ImageIO.read(Objects.requireNonNull(CommonFrame.class.getResource("/logo.png")));
                final Configuration configuration = db.findObject("configuration");
                JPanel j = new JPanel();
                j.setBorder(BorderFactory.createEmptyBorder(50,50,50,50));
                j.setBackground(new Color(54, 69, 79));
                j.setLayout(new BoxLayout(j,BoxLayout.Y_AXIS));
                j.add(Utility.getBasicLable(configuration,"club-title",Utility.CONSUMER_DEFAULT));
                j.add(Utility.getBasicLable(configuration,"address",Utility.CONSUMER_DEFAULT));
                j.add(Utility.getBasicLable(configuration,"phone-number",Utility.CONSUMER_DEFAULT));
                j.add(Utility.getBasicLable(configuration,"website",Utility.CONSUMER_DEFAULT));
                jf.add(j);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            jf.setIconImage(icon); // Set the icon for the JFrame
            jf.pack();
            jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        });
        fileMenuItems.put("Imports",(ActionEvent e)->{
            try {
                ImportFrame importFrame = new ImportFrame("Imports");
                importFrame.showList(db);
            } catch (Exception ex) {
                alert(ex.getMessage());
                ex.printStackTrace();
            }
        });
        fileMenuItems.put("New Players", (ActionEvent e)->{
            String reportTitle = JOptionPane.showInputDialog("Please Enter the Player list name");
            if(reportTitle.toString().isEmpty()){
                alert("Please enter valid Report title");
                return;
            }
            FileImport fileImport = new FileImport();
            fileImport.setName(reportTitle);
            fileImport.setImportedBy(System.getProperty("user.name"));
            fileImport.setId(Utility.getRandom());
            fileImport.setImportedTime(LocalDateTime.now());
            try{
                db.insert("File_"+fileImport.getId(),fileImport);
                FindFrame findFrame = new FindFrame("Find",fileImport.getPerson(),fileImport);
                findFrame.northpanel();
                findFrame.addDetails(db);
                findFrame.southPanel(db);
            }catch (Exception es){
                alert(es.getMessage());
                es.printStackTrace();
            }
        });
        fileMenuItems.put("Search Players",(ActionEvent e)->{
            try {
                List<String> fileImports = db.keyFilterBy("File_");
                JComboBox<Item> comboBox = new JComboBox<>();

                 fileImports.stream().map(a->{
                    try {
                        FileImport fileImport  = db.findObject(a);
                        comboBox.addItem(new Item(fileImport.getId(),fileImport.getName()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        alert(ex.getMessage());
                    }
                    return null;
                }).collect(Collectors.toList());

                Integer result = confirmation("Please select imported file.",()->comboBox);
                if(result==JOptionPane.YES_OPTION){
                    Item keySelected = (Item)comboBox.getSelectedItem();
                    if(keySelected.getId()!=0){
                        String fileKey = "File_"+keySelected.getId().toString();
                        FileImport fileImport = db.findObject(fileKey);
                        FindFrame findFrame = new FindFrame("Find",fileImport.getPerson(),fileImport);
                        findFrame.northpanel();
                        findFrame.addDetails(db);
                        findFrame.southPanel(db);
                    }else {
                        alert("Please select valid file Id");
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }
        });
        bouteMenuItems.put("Search Events",(ActionEvent e)->{
            try {
                BouteFrame bouteFrame = new BouteFrame("Find Events",db);
                bouteFrame.northPanel();
                bouteFrame.centerPanel();
                bouteFrame.southPanel();
            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }

        });
        bouteMenuItems.put("List Events", (ActionEvent e)->{
            try {
                EventListFrame eventListFrame = new EventListFrame("List Events");
                eventListFrame.northPanel(db);
                eventListFrame.listEvents(db);
                eventListFrame.southPanel(db);
            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }
        });


        configurationItems.put("Configure",(ActionEvent e)->{
            try {
                ConfigureFrame frame = new ConfigureFrame("Configuration");
                frame.northPanel(db);
                frame.centerPanel(db);
                frame.southPanel(db);
            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }

        });
        fileMenuItems.put("Import Players",(ActionEvent e)->{
            try {
                fileChooser().ifPresent(filePath->{

                    String fileType = Utility.getFileType(filePath);
                    if(!fileType.contains(Utility.CSV)){
                        alert("Invalid file format, Expected "+Utility.CSV+", Actual "+fileType);
                        return;
                    }
                    Reader<Person> reader = null;
                    try {
                        FileImport fileImport = new FileImport();
                        String reportTitle = JOptionPane.showInputDialog("Please Enter the Player list name");
                        if(reportTitle.toString().isEmpty()){
                            alert("Please enter valid Report title");
                            return;
                        }
                        fileImport.setName(reportTitle);
                        fileImport.setStatus(Status.FINISHED);
                        reader = new CsvFileReader(filePath,fileImport);
                        List<Person> persons =  reader.read();
                        db.insert("File_"+fileImport.getId(),fileImport);
                        alert("Total : "+persons.size()+" Uploaded Successfully...!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        alert("File import failed, Please verify the file.");
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                alert(ex.getMessage());
            }
        });

        men.put("Player List", fileMenuItems);
        men.put("Event", bouteMenuItems);
        men.put("Configuration", configurationItems);
        men.put("Contact Us",contactUs);
        super.menuBar(men);
        loadingWindow.dispose();
        if(initialConfiguration!=null){
            String titl = (String)initialConfiguration.get("club-title");
            setTitle(titl);
        }
        setVisible(true);

    }
    public static void main(String args[]) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        Library library = new Library("Dynamcis 101 MMA");
    }
}
