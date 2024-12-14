package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.Configuration;
import org.dynamics.model.TablePair;
import org.dynamics.util.Utility;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigureFrame extends CommonFrame{
    private JButton submit = new JButton("Find");
    private TablePair pair;

    public ConfigureFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        super(title);
    }

    public void northPanel(Db db){
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(20,100));
        jp.setBorder(BorderFactory.createTitledBorder("Create Configuration"));
        jp.setLayout(new GridLayout(1,2));
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
        JButton createConfiguration = new JButton(Utility.getImageIcon("/settings.png"));
        JComboBox<String> key = comboBox(Utility.CONFIGURATIONS);
        createConfiguration.addActionListener(e->{

            key.setBorder(BorderFactory.createTitledBorder("Key"));
            JPanel jprs = new JPanel();
            jprs.setLayout(new FlowLayout());
            JTextField value = textField();
            value.setBorder(BorderFactory.createTitledBorder("Value"));
            JPanel jsp = new JPanel();
            jsp.setLayout(new GridLayout(2,1,10,10));
            jsp.add(key);

            jprs.add(value);
            ImageIcon imageIcon = Utility.getImageIcon("/import.png");
            Image scaledImage = imageIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            JButton upload = new JButton(new ImageIcon(scaledImage));
            jprs.add(upload);
            jsp.add(jprs);

            upload.addActionListener(up->{
                fileChooser().ifPresent(value::setText);
            });

            int result = confirmation("Please enter the details.", ()->jsp);
            if(JOptionPane.YES_OPTION == result){
                String keyValue = key.getSelectedItem().toString();
                String valueValue = value.getText();
                try {
                    Configuration configuration = db.findObject("configuration");
                    System.out.println("before insertino "+configuration);
                    if(configuration==null){
                        configuration = new Configuration();
                    }

                    Map<String, Object> existing = configuration.getKeys();

                    if(valueValue.contains("|")){
                        List<String> da = Arrays.stream(valueValue.split(",")).collect(Collectors.toList());
                        existing.put(keyValue, da);
                    }else {
                        existing.put(keyValue, valueValue);
                    }

                    db.insert("configuration",configuration);
                    this.submit.doClick();
                    SwingUtilities.updateComponentTreeUI(this);
                    createConfiguration.doClick();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    alert(ex.getMessage());
                }

            }

        });

        JButton help = new JButton(Utility.getImageIcon("/help.png"));
        help.addActionListener(e->{
            JPanel jsp = new JPanel();
            jsp.setLayout(new FlowLayout());
            jsp.setSize(new Dimension(1000,1000));
            Vector<Vector<Object>> rows = new Vector<>();

            Vector r1 = new Vector();
            r1.add("left-logo");
            r1.add("Select the image path for the log at pdf left side and Dashboard left side");
            rows.add(r1);

            Vector r2 = new Vector();
            r2.add("right-logo");
            r2.add("Select the image path for the log at pdf right side.");
            rows.add(r2);

            Vector r3 = new Vector();
            r3.add("watermark-logo");
            r3.add("Pdf Watermark logo");
            rows.add(r3);

            Vector r4 = new Vector();
            r4.add("title");
            r4.add("Title of the PDF.");
            rows.add(r4);
            Vector r5 = new Vector();
            r5.add("club-title");
            r5.add("Title of the Dashboard.");
            rows.add(r5);


            Vector r6 = new Vector();
            r6.add("address");
            r6.add("Address of the Club.");
            rows.add(r6);

            Vector r7 = new Vector();
            r7.add("website");
            r7.add("Website of the Club.");
            rows.add(r7);

            Vector r8 = new Vector();
            r8.add("phone-number");
            r8.add("Phone Number of the Club.");
            rows.add(r8);


            Vector<String> cols = new Vector<>();
            cols.add("Key");
            cols.add("Description");
            createTable(jsp,rows,cols, LinkedHashMap::new,null);
            JFrame jf = new JFrame("Help");
            jf.setSize(new Dimension(500,500));
            jf.setResizable(false);
            jsp.setBackground(Color.WHITE);
            jf.add(new JScrollPane(jsp));
            jf.setVisible(true);
            jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jf.setBackground(Color.WHITE);
            try {
                jf.setIconImage(ImageIO.read(Objects.requireNonNull(CommonFrame.class.getResource("/logo.png"))));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });


        JPanel rightButtons = new JPanel(new FlowLayout());
        rightButtons.add(help);
        rightButtons.add(createConfiguration);
        add(rightButtons,BorderLayout.SOUTH);
    }
}
