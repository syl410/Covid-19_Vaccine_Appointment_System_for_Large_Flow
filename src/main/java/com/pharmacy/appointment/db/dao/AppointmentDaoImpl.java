package com.pharmacy.appointment.db.dao;

import org.springframework.stereotype.Repository;
import com.pharmacy.appointment.db.mappers.AppointmentMapper;
import com.pharmacy.appointment.db.po.Appointment;

import javax.annotation.Resource;

@Repository
public class AppointmentDaoImpl implements AppointmentDao {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Override
    public void insertAppointment(Appointment appointment) {
        appointmentMapper.insert(appointment);
    }

    @Override
    public Appointment queryAppointment(String appointmentNo) {
        return appointmentMapper.selectByAppointmentNo(appointmentNo);
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        appointmentMapper.updateByPrimaryKey(appointment);
    }

}
