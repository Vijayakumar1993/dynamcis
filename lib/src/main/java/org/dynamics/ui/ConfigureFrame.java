package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Configuration;
import org.dynamics.model.TablePair;
import org.jdatepicker.impl.JDatePickerImpl;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigureFrame extends CommonFrame{
    private JButton submit = new JButton("Find");
    private TablePair pair;

    public ConfigureFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
    }

    public void northPanel(Db db){
        JPanel jp = new JPanel();
        jp.setBorder(BorderFactory.createTitledBorder("Create Configuration"));
        JTextField key = textField();
        key.setBorder(BorderFactory.createTitledBorder("Key"));
        jp.add(key);

        jp.add(submit);

        submit.addActionListener(a->{
            String selectedKey = key.getText();

            Configuration configuration = new Configuration();
            try {
                configuration =  db.findObject("configuration");
            } catch (Exception e) {
                e.printStackTrace();
                alert(e.getMessage());
            }
            this.pair.getDefaultTableModel().setRowCount(0);
            System.out.println(configuration.getKeys());
            Map<String, Object> keys =  configuration.getKeys();

            keys.forEach((k,v)->{
                if(!selectedKey.isEmpty()){
                    if(k.contains(selectedKey)){
                        Vector<String> data = new Vector<>();
                        data.add(k);
                        data.add(v.toString());
                        this.pair.getDefaultTableModel().addRow(data);
                    }
                }else{
                    Vector<String> data = new Vector<>();
                    data.add(k);
                    data.add(v.toString());
                    this.pair.getDefaultTableModel().addRow(data);
                }

            });
        });
        add(jp, BorderLayout.NORTH);
    }
    public void centerPanel(Db db) throws IOException, ClassNotFoundException {
        pair = this.createTable(this,new Vector<>(), Configuration.keys(), LinkedHashMap::new,null);
    }
    public void southPanel(Db db){
        JButton createConfiguration = new JButton("Create Configuration");
        createConfiguration.addActionListener(e->{
            JTextField key = textField();
            key.setBorder(BorderFactory.createTitledBorder("Key"));
            JTextField value = textField();
            value.setBorder(BorderFactory.createTitledBorder("Value"));
            JPanel jsp = new JPanel();
            jsp.setLayout(new GridLayout(2,1,10,10));
            jsp.add(key);
            jsp.add(value);

            int result = confirmation("Please enter the details.", ()->jsp);
            if(JOptionPane.YES_OPTION == result){
                String keyValue = key.getText();
                String valueValue = value.getText();
                try {
                    Configuration configuration = db.findObject("configuration");
                    System.out.println("before insertino "+configuration);
                    if(configuration==null){
                        configuration = new Configuration();
                    }

                    Map<String, Object> existing = configuration.getKeys();

                    if(valueValue.contains(",")){
                        List<String> da = Arrays.stream(valueValue.split(",")).collect(Collectors.toList());
                        existing.put(keyValue, da);
                    }else {
                        existing.put(keyValue, valueValue);
                    }

                    db.insert("configuration",configuration);
                    this.submit.doClick();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    alert(ex.getMessage());
                }

            }
        });
        JPanel rightButtons = new JPanel(new FlowLayout());
        rightButtons.add(createConfiguration);
        add(rightButtons,BorderLayout.SOUTH);
    }
}
