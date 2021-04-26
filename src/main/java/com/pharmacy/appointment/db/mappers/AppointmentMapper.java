package com.pharmacy.appointment.db.mappers;

import com.pharmacy.appointment.db.po.Appointment;

public interface AppointmentMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Appointment record);

    int insertSelective(Appointment record);

    Appointment selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Appointment record);

    int updateByPrimaryKey(Appointment record);

    Appointment selectByAppointmentNo(String appointmentNo);
}