package com.pharmacy.appointment.mq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.pharmacy.appointment.db.dao.AppointmentDao;
import com.pharmacy.appointment.db.dao.VenueDao;
import com.pharmacy.appointment.db.po.Appointment;
import com.pharmacy.appointment.util.RedisService;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "vaccine_appointment", consumerGroup = "vaccine_appointment_group")
public class AppointmentConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private VenueDao venueDao;

    @Autowired
    RedisService redisService;

    private static final int VALID = 1, INVALID = 0;

    @Override
    @Transactional
    public void onMessage (MessageExt messageExt) {
        // 1, parse request message of created appointment
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("received request message of created appointment：：" + message);
        Appointment appointment = JSON.parseObject(message, Appointment.class);
        appointment.setCreateTime(new Date());
        // 2, lock spot
        boolean lockSpotResult = venueDao.lockSpot(appointment.getVenueId());
        if (lockSpotResult) {
            // status of appointment: 0: no available spot, 1: appointment has been created, wait for confirmation
            appointment.setAppointmentStatus(VALID);
            // user is added to limited user list
            redisService.addLimitedUser(appointment.getVenueId(), appointment.getUserId());
        } else {
            appointment.setAppointmentStatus(INVALID);
        }
        // 3, insert appointment
        appointmentDao.insertAppointment(appointment);
    }
}
