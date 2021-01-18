package io.kensu.example.jboss.model.entities;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "customers")
public class Customer {
    @Column(name = "Date_record")
    private Date Date_record;
    @Id
    @Column(name = "Loan_ID")
    @GeneratedValue()
    private String Loan_ID;
    @Column(name = "LoanAmount")
    private Integer LoanAmount;
    @Column(name = "Loan_Amount_Term")
    private Integer Loan_Amount_Term;
    @Column(name = "Credit_History")
    private Integer Credit_History;
    @Column(name = "ApplicantIncome")
    private Integer ApplicantIncome;
    @Column(name = "CoapplicantIncome")
    private Integer CoapplicantIncome;
    @Column(name = "Married_Yes")
    private Integer Married_Yes;
    @Column(name = "Dependents_1")
    private Integer Dependents_1;
    @Column(name = "Dependents_2")
    private Integer Dependents_2;
    @Column(name = "Dependents_3")
    private Integer Dependents_3;
    @Column(name = "Education_Not_Graduate")
    private Integer Education_Not_Graduate;
    @Column(name = "Self_Employed_Yes")
    private Integer Self_Employed_Yes;
    @Column(name = "Property_Area_Semiurban")
    private Integer Property_Area_Semiurban;
    @Column(name = "Property_Area_Urban")
    private Integer Property_Area_Urban;
    @Column(name = "predict")
    private Integer predict;
    @Column(name = "p0")
    private Double p0;
    @Column(name = "p1")
    private Double p1;

    public Date getDate_record() {
        return Date_record;
    }

    public void setDate_record(Date date_record) {
        Date_record = date_record;
    }

    public String getLoan_ID() {
        return Loan_ID;
    }

    public void setLoan_ID(String loan_ID) {
        Loan_ID = loan_ID;
    }

    public Integer getLoanAmount() {
        return LoanAmount;
    }

    public void setLoanAmount(Integer loanAmount) {
        LoanAmount = loanAmount;
    }

    public Integer getLoan_Amount_Term() {
        return Loan_Amount_Term;
    }

    public void setLoan_Amount_Term(Integer loan_Amount_Term) {
        Loan_Amount_Term = loan_Amount_Term;
    }

    public Integer getCredit_History() {
        return Credit_History;
    }

    public void setCredit_History(Integer credit_History) {
        Credit_History = credit_History;
    }

    public Integer getApplicantIncome() {
        return ApplicantIncome;
    }

    public void setApplicantIncome(Integer applicantIncome) {
        ApplicantIncome = applicantIncome;
    }

    public Integer getCoapplicantIncome() {
        return CoapplicantIncome;
    }

    public void setCoapplicantIncome(Integer coapplicantIncome) {
        CoapplicantIncome = coapplicantIncome;
    }

    public Integer getMarried_Yes() {
        return Married_Yes;
    }

    public void setMarried_Yes(Integer married_Yes) {
        Married_Yes = married_Yes;
    }

    public Integer getDependents_1() {
        return Dependents_1;
    }

    public void setDependents_1(Integer dependents_1) {
        Dependents_1 = dependents_1;
    }

    public Integer getDependents_2() {
        return Dependents_2;
    }

    public void setDependents_2(Integer dependents_2) {
        Dependents_2 = dependents_2;
    }

    public Integer getDependents_3() {
        return Dependents_3;
    }

    public void setDependents_3(Integer dependents_3) {
        Dependents_3 = dependents_3;
    }

    public Integer getEducation_Not_Graduate() {
        return Education_Not_Graduate;
    }

    public void setEducation_Not_Graduate(Integer education_Not_Graduate) {
        Education_Not_Graduate = education_Not_Graduate;
    }

    public Integer getSelf_Employed_Yes() {
        return Self_Employed_Yes;
    }

    public void setSelf_Employed_Yes(Integer self_Employed_Yes) {
        Self_Employed_Yes = self_Employed_Yes;
    }

    public Integer getProperty_Area_Semiurban() {
        return Property_Area_Semiurban;
    }

    public void setProperty_Area_Semiurban(Integer property_Area_Semiurban) {
        Property_Area_Semiurban = property_Area_Semiurban;
    }

    public Integer getProperty_Area_Urban() {
        return Property_Area_Urban;
    }

    public void setProperty_Area_Urban(Integer property_Area_Urban) {
        Property_Area_Urban = property_Area_Urban;
    }

    public Integer getPredict() {
        return predict;
    }

    public void setPredict(Integer predict) {
        this.predict = predict;
    }

    public Double getP0() {
        return p0;
    }

    public void setP0(Double p0) {
        this.p0 = p0;
    }

    public Double getP1() {
        return p1;
    }

    public void setP1(Double p1) {
        this.p1 = p1;
    }
}
