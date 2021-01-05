package io.kensu.example.jboss.model;
 
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

    @Column(name = "CounterID")
    private Long counterId;
    public Long getCounterId() {
        return counterId;
    }
    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }

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

    @Column(name = "Sign")
    private Integer sign;
    public Integer getSign() {
        return sign;
    }
    public void setSign(Integer sign) {
        this.sign = sign;
    }
}