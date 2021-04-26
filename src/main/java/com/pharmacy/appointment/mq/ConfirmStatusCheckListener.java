
package com.pharmacy.appointment.mq;


import com.alibaba.fastjson.JSON;
import com.pharmacy.appointment.db.dao.AppointmentDao;
import com.pharmacy.appointment.db.dao.VenueDao;
import com.pharmacy.appointment.db.po.Appointment;
import com.pharmacy.appointment.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "confirm_check", consumerGroup = "confirm_check_group")
public class ConfirmStatusCheckListener implements RocketMQListener<MessageExt> {
    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private VenueDao venueDao;

    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("received message about checking confirmation status of appointment:" + message);
        Appointment appointment = JSON.parseObject(message, Appointment.class);
        // 1, query appointment
        Appointment appointmentInfo = appointmentDao.queryAppointment(appointment.getAppointmentNo());
        // 2, check if appointment hasn't been confirm
        if (appointmentInfo.getAppointmentStatus() != 2) {
            // 3, the appointment wasn't confirmed
            log.info("the appointment wasn't confirmed, close the appointment No.: " + appointmentInfo.getAppointmentNo());
            appointmentInfo.setAppointmentStatus(99);
            appointmentDao.updateAppointment(appointmentInfo);
            // 4, revert spot in DB and Redis
            venueDao.revertSpot(appointment.getVenueId());
            redisService.revertSpot("spot:" + appointment.getVenueId());
            // 5, remove user from list of limited user
            redisService.removeLimitedUser(appointment.getVenueId(), appointment.getUserId());
        }
    }
}

