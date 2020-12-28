package com.hotjoe.services.model;
 
import java.io.Serializable;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
 
@Entity
@Table(name="visits_v1")
public class Visit {
    @Id
    @Column(name = "VisitID")
    @GeneratedValue()
    private Long visitId;

    @Column(name = "StartURL")
    private String startUrl; 
    public String getStartUrl() {
        return this.startUrl;
    }
    public void setStartUrl(String s) {
        this.startUrl = s;
    }
    
    @Column(name = "EndURL")
    private String endUrl; 
    public String getEndUrl() {
        return this.endUrl;
    }
    public void setEndUrl(String s) {
        this.endUrl = s;
    }
}