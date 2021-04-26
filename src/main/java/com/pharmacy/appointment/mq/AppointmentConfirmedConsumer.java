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

import java.nio.charset.StandardCharsets;

// process message after appointment is confirmed and deduct spot from DB
@Slf4j
@Component
@RocketMQMessageListener(topic = "confirmed", consumerGroup = "confirmed_group")
public class AppointmentConfirmedConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private VenueDao venueDao;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        // 1, parse request message of confirmed appointment
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("received request message of confirm appointment：" + message);
        Appointment appointment = JSON.parseObject(message, Appointment.class);
        // 2, deduct spot
        venueDao.deductSpot(appointment.getVenueId());
    }
}
