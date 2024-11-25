package org.dynamics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class FileImport<T> implements Serializable {
    private Long id;
    private String name;
    private  String importedBy;
    private LocalDateTime importedTime;
    private Integer totalCount;
    private List<T> person = new LinkedList<>();

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
}
