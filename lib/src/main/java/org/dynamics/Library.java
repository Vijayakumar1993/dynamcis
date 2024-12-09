/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.dynamics;

import org.dynamics.db.Db;
import org.dynamics.db.LevelDb;
import org.dynamics.model.Configuration;
import org.dynamics.model.FileImport;
import org.dynamics.model.Item;
import org.dynamics.model.Person;
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
import java.nio.file.Paths;
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
                icon = ImageIO.read(Objects.requireNonNull(CommonFrame.class.getResource("/logo.jpeg")));
                final Configuration configuration = db.findObject("configuration");
                JPanel j = new JPanel();
                j.setBackground(Color.WHITE);
                String ttle = (String)configuration.get("title");
                JLabel cont = new JLabel(ttle);
                cont.setFont(new Font("Serif",Font.BOLD,25));
                j.add(cont);
                jf.add(j);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            jf.setIconImage(icon); // Set the icon for the JFrame
            jf.getContentPane().setBackground(Color.WHITE);
            jf.setSize(new Dimension(300,150));
            jf.setResizable(false);
            jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



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
                    Reader<Person> reader = null;
                    try {
                        FileImport fileImport = new FileImport();
                        String reportTitle = JOptionPane.showInputDialog("Please Enter the Player list name");
                        if(reportTitle.toString().isEmpty()){
                            alert("Please enter valid Report title");
                            return;
                        }
                        fileImport.setName(reportTitle);
                        reader = new CsvFileReader(filePath,fileImport);
                        List<Person> persons =  reader.read();
                        db.insert("File_"+fileImport.getId(),fileImport);
                        alert("Total : "+persons.size()+" Uploaded Successfully...!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        alert(ex.getMessage());
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
        setVisible(true);
    }
    public static void main(String args[]) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        Library library = new Library("Dynamcis 101 MMA");
    }
}
