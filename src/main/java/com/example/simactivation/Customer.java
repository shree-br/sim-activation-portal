package com.example.simactivation;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Table(name = "customer")
public class Customer {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(name = "application_reference", unique = true)
private String applicationReference;

@Column(name = "first_name")
private String firstName;

@Column(name = "last_name")
private String lastName;

@Column(name = "email")
private String email;

@Column(name = "date_of_birth")
private String dateOfBirth;

@Column(name = "address")
private String address;

@Column(name = "id_proof")
private String idProof;

@Column(name = "mobile_number")
private String mobile;

@Column(name = "aadhaar")
private String aadhaar;

@Column(name = "sim_number")
private String simNumber;

// New field: SIM Activation Status
@Column(name = "activation_status")
private String activationStatus = "PENDING";

public Customer() {
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public String getFirstName() {
    return firstName;
}

public void setFirstName(String firstName) {
    this.firstName = firstName;
}

public String getLastName() {
    return lastName;
}

public void setLastName(String lastName) {
    this.lastName = lastName;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}

public String getDateOfBirth() {
    return dateOfBirth;
}

public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
}

public String getAddress() {
    return address;
}

public void setAddress(String address) {
    this.address = address;
}

public String getIdProof() {
    return idProof;
}

public void setIdProof(String idProof) {
    this.idProof = idProof;
}

public String getMobile() {
    return mobile;
}

public void setMobile(String mobile) {
    this.mobile = mobile;
}

public String getAadhaar() {
    return aadhaar;
}

public void setAadhaar(String aadhaar) {
    this.aadhaar = aadhaar;
}

public String getSimNumber() {
    return simNumber;
}

public void setSimNumber(String simNumber) {
    this.simNumber = simNumber;
}

// Getter and Setter for Activation Status
public String getActivationStatus() {
    return activationStatus;
}

public void setActivationStatus(String activationStatus) {
    this.activationStatus = activationStatus;
}

public String getApplicationReference() {
    return applicationReference;
}

public void setApplicationReference(String applicationReference) {
    this.applicationReference = applicationReference;
}
}