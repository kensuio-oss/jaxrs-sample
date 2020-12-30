package com.hotjoe.services.model;
 
import java.io.Serializable;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
 
@Entity
@Table(name="hits_v1")
public class Hit {
    @Id
    @Column(name = "HID")
    @GeneratedValue()
    private Long hitId;

    @Column(name = "CounterID")
    private Long counterId;

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }
}