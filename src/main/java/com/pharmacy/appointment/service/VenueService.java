package com.pharmacy.appointment.service;

import com.pharmacy.appointment.db.dao.AppointmentDao;
import com.pharmacy.appointment.db.dao.VenueDao;
import com.pharmacy.appointment.db.dao.PharmacyDao;
import com.pharmacy.appointment.db.po.Appointment;
import com.pharmacy.appointment.db.po.Venue;
import com.pharmacy.appointment.db.po.Pharmacy;
import com.pharmacy.appointment.mq.RocketMQService;
import com.pharmacy.appointment.util.RedisService;
import com.pharmacy.appointment.util.SnowFlake;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class VenueService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private VenueDao venueDao;

    @Autowired
    private RocketMQService rocketMQService;

    @Autowired
    PharmacyDao pharmacyDao;

    @Autowired
    AppointmentDao appointmentDao;

    private static final int APP_COUNT = 1; // Default count of appointments

    // for this is run on local machine (not in distributed system), both DATACNTER_ID and MACHINE_ID are hard coded to 1.
    private static final int DATACNTER_ID = 1;
    private static final int MACHINE_ID = 1;
    private final SnowFlake snowFlake = new SnowFlake(DATACNTER_ID, MACHINE_ID);

    /**
     * Create appointment
     * @param id Venue ID
     * @param userId User ID
     * @return Appointment detail
     * @throws Exception MQ exception
     */
    public Appointment createAppointment(long id, long userId) throws Exception {

        // 1. query & get venue
        Venue venue = venueDao.queryVenueById(id);

        // 2. create & set new appointment information
        Appointment appointment = new Appointment();

        // use snowflake algorithm to generate appointment ID
        appointment.setAppointmentNo(String.valueOf(snowFlake.nextId()));
        appointment.setVenueId(venue.getId());
        appointment.setUserId(userId);
        appointment.setAppointmentCount(APP_COUNT);

        // 3. send "create appointment" message to Rocket MQ
        rocketMQService.sendMessage("vaccine_appointment", JSON.toJSONString(appointment));

        // 4. send "validate confirm status" message to Rocket MQ
        // 18 levels are supported: messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        rocketMQService.sendDelayMessage("confirm_check", JSON.toJSONString(appointment), 5);

        return appointment;
    }

    /**
     * check if there are appointment spots for a venue ID
     * @param venueId venueId
     * @return true/false
     */
    public boolean appointmentSpotValidator(long venueId) {
        String key = "venue:" + venueId; // venue is for a key, it is not related to which MySQL
        return redisService.spotDeductValidator(key);
    }


    /**
     * add venue details into redis
     * @param venueId
     */
    public void pushVenueInfoToRedis(long venueId) {
        Venue venue = venueDao.queryVenueById(venueId);
        redisService.setValue("venue_detail:" + venueId, JSON.toJSONString(venue));

        Pharmacy pharmacy = pharmacyDao.queryPharmacyById(venue.getPharmacyId());
        redisService.setValue("pharmacy:" + venue.getPharmacyId(), JSON.toJSONString(pharmacy));
    }

    /**
     * process confirmed appointment
     * @param appointmentNo
     */
    public void confirmAppointmentProcess(String appointmentNo) throws Exception {
        Appointment appointment = appointmentDao.queryAppointment(appointmentNo);

        // check if appointment exists or not
        if (appointment == null) {
            log.error("the appointment with the No. doesn't exist：" + appointmentNo);
            return;
        // check if appointment is unconfirmed    
        } else if(appointment.getAppointmentStatus() != 1 ) {
            log.error("invalid appointment status：" + appointmentNo);
            return;
        }
        log.info("confirmed appointment No.：" + appointmentNo);

        // appintment has been confirmed
        appointment.setConfirmTime(new Date());
        // status 2 means appointment has been confirmed 
        appointment.setAppointmentStatus(2);
        appointmentDao.updateAppointment(appointment);
        // send confirmation message of the appointment
        rocketMQService.sendMessage("confirmed", JSON.toJSONString(appointment));
    }
}
