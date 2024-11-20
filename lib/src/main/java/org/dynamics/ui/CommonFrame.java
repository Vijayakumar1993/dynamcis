package org.dynamics.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Supplier;

public abstract class CommonFrame extends JFrame {
    public CommonFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        setTitle(title);
        setVisible(true);
        setSize(new Dimension(300,300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    }

    public void menuBar(Map<String, Map<String, ActionListener>> menuItems){
        JMenuBar menuBar = new JMenuBar();
        menuItems.forEach((key,value)->{
            JMenu menu = new JMenu(key);
            value.forEach((subMenu, listener)->{
                JMenuItem menuItem = new JMenuItem(subMenu);
                menuItem.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                menuItem.addActionListener(listener);
                menu.add(menuItem);
            });
            menuBar.add(menu);
        });
        setJMenuBar(menuBar);
    }

    protected void alert(String message){
        JOptionPane.showMessageDialog(this,message);
    }

    protected Optional<String> fileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            return Optional.of(fileChooser.getSelectedFile().getAbsolutePath());
        }
        return Optional.empty();
    }

    public DefaultTableModel createTable(JFrame frame, Vector<Vector<String>> rows, Vector<String> columns ){
        DefaultTableModel model = new DefaultTableModel(rows, columns);
        JTable table = new JTable(model);
        table.getTableHeader().setFont(new Font("Serif",Font.BOLD,12));
        table.setShowVerticalLines(false);

        JScrollPane jsp = new JScrollPane(table);
        frame.add(jsp, BorderLayout.CENTER);
        return model;
    }

    public void confirmation(String msg, Supplier<JComponent> supplier){
        JOptionPane.showConfirmDialog(this,supplier.get(),"Choose ",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
    }
}
