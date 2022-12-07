package com.example.woody.entity;

import java.io.Serializable;

public class Wood implements Serializable {
    private int id;
    private String commercialName;
    private String distribution;
    private String displayName;
    private String generalName;
    private String properties;
    private int preservation;
    private int type;
    private String usage;
    private String image;
    private String scienceName;
    private String family;
    private String reference;

    public Wood() {

    }

    public Wood(int id, String commercialName, String distribution, String displayName, String generalName, String properties, int preservation, int type, String usage, String image, String scienceName, String family, String reference) {
        this.id = id;
        this.commercialName = commercialName;
        this.distribution = distribution;
        this.displayName = displayName;
        this.generalName = generalName;
        this.properties = properties;
        this.preservation = preservation;
        this.type = type;
        this.usage = usage;
        this.image = image;
        this.scienceName = scienceName;
        this.family = family;
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGeneralName() {
        return generalName;
    }

    public void setGeneralName(String generalName) {
        this.generalName = generalName;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public int getPreservation() {
        return preservation;
    }

    public void setPreservation(int preservation) {
        this.preservation = preservation;
    }

    public String getType() {
        switch (type){
            case 1:
                return "Gỗ cứng";
            case 2:
                return "Gỗ mềm";
            default:
                return "--";

        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getScienceName() {
        return scienceName;
    }

    public void setScienceName(String scienceName) {
        this.scienceName = scienceName;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }
}
