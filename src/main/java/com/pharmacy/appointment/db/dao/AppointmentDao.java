package com.pharmacy.appointment.db.dao;

import com.pharmacy.appointment.db.po.Appointment;

public interface AppointmentDao {

    void insertAppointment(Appointment appointment);

    Appointment queryAppointment(String appointmentNo);

    void updateAppointment(Appointment appointment);
}
