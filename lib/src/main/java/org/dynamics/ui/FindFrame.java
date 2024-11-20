package org.dynamics.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class FindFrame extends CommonFrame{
    public FindFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    public void addDetails(Vector<String> columns, Vector<Vector<String>> rows){
        createTable(this,rows,columns);
    }
}
