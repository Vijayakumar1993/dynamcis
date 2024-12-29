package org.dynamics.ui;

import javafx.scene.layout.Background;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dynamics.db.Db;
import org.dynamics.model.FileImport;
import org.dynamics.model.Person;
import org.dynamics.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ImportFrame extends CommonFrame{
    private static final Logger logger = LogManager.getLogger(ImportFrame.class);
    private final Map<String, FileImport> fileImports = new LinkedHashMap<>();
    public ImportFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        super(title);
    }

    public void showList(Db db){
        try{
            List<String> keys =db.keyFilterBy("File_");
            if(!keys.isEmpty()){
                Vector<Vector<Object>> rows = keys.stream().map(key->{
                    try {
                        FileImport fileImport = db.findObject(key);
                        fileImports.put(key,fileImport);
                        return fileImport;
                    } catch (Exception e) {
                        e.printStackTrace();
                        alert(e.getMessage());
                    }
                    return  null;
                }).map(fileImport -> {
                    Vector<Object> row = new Vector<>();
                    row.add(fileImport.getId());
                    row.add(fileImport.getFilePath());
                    row.add(fileImport.getImportedBy());
                    row.add(fileImport.getTotalCount());
                    row.add(fileImport.getName());
                    row.add(fileImport.getStatus());
                    return row;
                }).collect(Vector::new,Vector::add,Vector::addAll);
                this.createTable(this,rows,FileImport.keys(), LinkedHashMap::new,null);
            }
        }catch (Exception e){
            logger.error("An error occurred", e);
            e.printStackTrace();
            alert(e.getMessage());
        }
    }

    public void southPanle(Db db){
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.setBackground(Color.WHITE);
        ImageIcon imageIcon = Utility.getImageIcon("/remove.png");
        ImageIcon combine = Utility.getImageIcon("/combine.png");
        JButton deleteIcon = new JButton(new ImageIcon(imageIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        JButton intersection = new JButton(new ImageIcon(combine.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        jp.add(intersection, BorderLayout.WEST);
        jp.add(deleteIcon, BorderLayout.EAST);

        FileImport[] imports = new FileImport[this.fileImports.size()];

        intersection.addActionListener(a->{

            if(this.fileImports.isEmpty()){
                alert("No imports found to delete.");
                return;
            }
            JList<FileImport> multiSelect = new JList<>(this.fileImports.values().toArray(imports));
            multiSelect.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JPanel jsp = new JPanel();
            jsp.setBackground(Color.WHITE);
            jsp.setBorder(BorderFactory.createTitledBorder("Combine Players"));
            jsp.setLayout(new GridLayout(2,1,10,10));
            jsp.add(multiSelect);
            JLabel comp = new JLabel("<html>Players merge won't affect events.<br />Hold Ctrl and click to select multiple items.</html>", Utility.getImageIcon("/alert.png"), JLabel.LEADING);
            comp.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
            comp.setForeground(Color.RED);
            jsp.add(comp);
            int result = confirmation("Please select imports to delete",()->jsp);
            if(result==JOptionPane.YES_OPTION){
                java.util.List<FileImport> selectedValues = multiSelect.getSelectedValuesList();
                FileImport combinedFileImport = new FileImport();
                combinedFileImport.setId(Utility.getRandom());
                combinedFileImport.setImportedBy(System.getProperty("user.name"));
                List<Person> persons = new LinkedList<>();
                AtomicReference<String> newName = new AtomicReference<>("");
                int confi = JOptionPane.showConfirmDialog(this,"Do you want to delete existing list?");
                if(selectedValues.isEmpty()) {
                    alert("Nothing to combine/merge");
                    return;
                }
                selectedValues.forEach(fileImport -> {
                    this.fileImports.entrySet().stream().filter(k->k.getValue().equals(fileImport)).map(Map.Entry::getKey).findFirst().ifPresent(k->{
                            persons.addAll(fileImport.getPerson());
                            newName.set(newName + fileImport.getName()+" ");
                            if(confi == JOptionPane.YES_OPTION) db.delete(k);
                    });
                });
                combinedFileImport.setName(newName.get());
                combinedFileImport.setTotalCount(persons.size());
                combinedFileImport.setPerson(persons);
                try {
                    db.insert("File_"+combinedFileImport.getId(),combinedFileImport);
                } catch (IOException e) {
                    logger.info("An error occurred ",e);
                    alert(e.getMessage());
                }
                this.setVisible(false);
            }

        });
        deleteIcon.addActionListener(a->{
            if(this.fileImports.isEmpty()){
                alert("No imports found to delete.");
                return;
            }
            JList<FileImport> multiSelect = new JList<>(this.fileImports.values().toArray(imports));
            multiSelect.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JPanel jsp = new JPanel();
            jsp.setBackground(Color.WHITE);
            jsp.setBorder(BorderFactory.createTitledBorder("Delete Imported Players"));
            jsp.setLayout(new GridLayout(2,1,10,10));
            jsp.add(multiSelect);
            JLabel comp = new JLabel("<html>Players deletion won't affect events.<br />Hold Ctrl and click to select multiple items.</html>", Utility.getImageIcon("/alert.png"), JLabel.LEADING);
            comp.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
            comp.setForeground(Color.RED);
            jsp.add(comp);
            int result = confirmation("Please select imports to delete",()->jsp);
            if(result==JOptionPane.YES_OPTION){
                java.util.List<FileImport> selectedValues = multiSelect.getSelectedValuesList();
                if(selectedValues.isEmpty()) {
                    alert("Nothing to delete.");
                    return;
                }
                selectedValues.forEach(fileImport -> {
                    this.fileImports.entrySet().stream().filter(k->k.getValue().equals(fileImport)).map(Map.Entry::getKey).findFirst().ifPresent(k->{
                       int reu =  JOptionPane.showConfirmDialog(this,"Are you sure to delete "+fileImport);
                       if(reu == JOptionPane.YES_OPTION){
                           db.delete(k);
                       }
                    });
                });
                this.setVisible(false);
            }
        });

        this.add(jp, BorderLayout.SOUTH);
    }

}
