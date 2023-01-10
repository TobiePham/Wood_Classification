package com.example.woody.entity;

public class Glossary {
    private String definition;
    private String english;
    private int id;
    private String note;
    private String vietnamese;
    private String image;

    public Glossary() {
    }

    public Glossary(String definition, String english, int id, String note, String vietnamese, String image) {
        this.definition = definition;
        this.english = english;
        this.id = id;
        this.note = note;
        this.vietnamese = vietnamese;
        this.image = image;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getVietnamese() {
        return vietnamese;
    }

    public void setVietnamese(String vietnamese) {
        this.vietnamese = vietnamese;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String toString(){
        return this.getVietnamese();
    }
}