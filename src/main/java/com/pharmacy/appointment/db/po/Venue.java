package com.pharmacy.appointment.db.po;

import java.math.BigDecimal;
import java.util.Date;

public class Venue {
    private Long id;

    private String vaccine;

    private Long pharmacyId;

    private Integer appointmentTime;

    private Integer venueStatus;

    private Date startTime;

    private Date endTime;

    private Integer totalSpot;

    private Integer availableSpot;

    private Integer lockSpot;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVaccine() {
        return vaccine;
    }

    public void setVaccine(String vaccine) {
        this.vaccine = vaccine == null ? null : vaccine.trim();
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public Integer getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Integer appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getVenueStatus() {
        return venueStatus;
    }

    public void setVenueStatus(Integer venueStatus) {
        this.venueStatus = venueStatus;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalSpot() {
        return totalSpot;
    }

    public void setTotalSpot(Integer totalSpot) {
        this.totalSpot = totalSpot;
    }

    public Integer getAvailableSpot() {
        return availableSpot;
    }

    public void setAvailableSpot(Integer availableSpot) {
        this.availableSpot = availableSpot;
    }

    public Integer getLockSpot() {
        return lockSpot;
    }

    public void setLockSpot(Integer lockSpot) {
        this.lockSpot = lockSpot;
    }
}