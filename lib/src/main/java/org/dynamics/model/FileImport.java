package org.dynamics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class FileImport<T> implements Serializable {
    private Long id;
    private String name;

    private String filePath;
    private String importedBy;
    private LocalDateTime importedTime;
    private int totalCount;
    private List<T> person = new LinkedList<>();
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<T> getPerson() {
        return person;
    }

    public void setPerson(List<T> person) {
        this.person = person;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImportedBy() {
        return importedBy;
    }

    public void setImportedBy(String importedBy) {
        this.importedBy = importedBy;
    }

    public LocalDateTime getImportedTime() {
        return importedTime;
    }

    public void setImportedTime(LocalDateTime importedTime) {
        this.importedTime = importedTime;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public static Vector<String> keys(){
        Vector<String> keys = new Vector<>();
        keys.add("Id");
        keys.add("File Path");
        keys.add("Imported By");
        keys.add("Total Count");
        keys.add("Name");
        keys.add("Status");
        return keys;
    }

    @Override
    public String toString() {
        return   this.name;
    }
}
