package org.dynamics.model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TablePair {
    private DefaultTableModel defaultTableModel;
    private JTable jTable;

    public TablePair(DefaultTableModel defaultTableModel, JTable jTable) {
        this.defaultTableModel = defaultTableModel;
        this.jTable = jTable;
    }

    public DefaultTableModel getDefaultTableModel() {
        return defaultTableModel;
    }

    public JTable getjTable() {
        return jTable;
    }
}
