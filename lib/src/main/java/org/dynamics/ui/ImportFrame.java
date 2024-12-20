package org.dynamics.ui;

import org.dynamics.db.Db;
import org.dynamics.model.FileImport;

import javax.swing.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class ImportFrame extends CommonFrame{
    public ImportFrame(String title) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        super(title);
    }

    public void showList(Db db){
        try{
            List<String> keys =db.keyFilterBy("File_");
            if(!keys.isEmpty()){
                Vector<Vector<Object>> rows = keys.stream().map(key->{
                    try {
                        return (FileImport)db.findObject(key);
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
            e.printStackTrace();
            alert(e.getMessage());
        }
    }
}
