package com.pharmacy.appointment.web;

import com.pharmacy.appointment.db.dao.AppointmentDao;
import com.pharmacy.appointment.db.dao.VenueDao;
import com.pharmacy.appointment.db.dao.PharmacyDao;
import com.pharmacy.appointment.db.po.Appointment;
import com.pharmacy.appointment.db.po.Venue;
import com.pharmacy.appointment.db.po.Pharmacy;
import com.pharmacy.appointment.service.VenueService;
import com.pharmacy.appointment.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class VenueController {

    @Autowired
    private VenueDao venueDao;

    @Autowired
    private PharmacyDao pharmacyDao;

    @Autowired
    VenueService venueService;

    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    RedisService redisService;


    @RequestMapping("/addVenue")
    public String addVenue() {
        return "add_venue";
    }

    // main page, list of all appointment venues
    @RequestMapping("/appointments")
    public String venueList(Map<String, Object> resultMap) {
        try (Entry entry = SphU.entry("appointments")) {
            String idListStr = redisService.getValue("venue_ID_list");
            String[] idStrList = idListStr.split("#");
            List<Venue> venues = new ArrayList<>();
            List<String> spots = new ArrayList<>(); // String is easy for using redis
            List<Pharmacy> pharmacies = new ArrayList<>();

            for (String idStr : idStrList) {
                // add venue
                String venueStr = redisService.getValue("venue_detail:" + idStr);
                Venue venue = JSON.parseObject(venueStr, Venue.class);
                venues.add(venue);
                // add spots
                spots.add(redisService.getValue("venue:" + idStr));
                // add pharmacies
                String pharmacyStr = redisService.getValue("pharmacy:" + venue.getPharmacyId());
                Pharmacy pharmacy = JSON.parseObject(pharmacyStr, Pharmacy.class);
                pharmacies.add(pharmacy);
            }

            resultMap.put("venues", venues);
            resultMap.put("spots", spots);
            resultMap.put("pharmacies", pharmacies);
            return "appointment_venue";
        } catch (BlockException ex) {
            log.error("Visited main page too fast. The visti has been limited" + ex.toString());
            return "wait";
        }
    }

    @RequestMapping("/addVenueAction")
    public String addVenueAction(
            @RequestParam("vaccine") String vaccine,
            @RequestParam("pharmacyId") long pharmacyId,
            @RequestParam("appointmentTime") int appointmentTime,
            @RequestParam("spotNumber") int spotNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException {
        startTime = startTime.substring(0, 10) +  startTime.substring(11);
        endTime = endTime.substring(0, 10) +  endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        Venue venue = new Venue();
        venue.setVaccine(vaccine);
        venue.setPharmacyId(pharmacyId);
        venue.setAppointmentTime(appointmentTime);
        venue.setTotalSpot(spotNumber);
        venue.setAvailableSpot(spotNumber);
        venue.setLockSpot(0);
        venue.setVenueStatus(1);
        venue.setStartTime(format.parse(startTime));
        venue.setEndTime(format.parse(endTime));
        venueDao.inertVenue(venue);
        resultMap.put("venue", venue);
        return "add_success";
    }

    @RequestMapping("/details/{venueId}")
    public String detailsPage(Map<String, Object> resultMap, @PathVariable long venueId) {
        Venue venue;
        Pharmacy pharmacy;

        String venueInfo = redisService.getValue("venue_detail:" + venueId);
        System.out.println("Visiting:" + venueInfo);
        if (StringUtils.isNotEmpty(venueInfo)) {
            log.info("cache data in redis:" + venueInfo);
            venue = JSON.parseObject(venueInfo, Venue.class);
        } else {
            venue = venueDao.queryVenueById(venueId);
        }

        String pharmacyInfo = redisService.getValue("pharmacy:" + venue.getPharmacyId());
        if (StringUtils.isNotEmpty(pharmacyInfo)) {
            log.info("cache data in redis:" + pharmacyInfo);
            pharmacy = JSON.parseObject(pharmacyInfo, Pharmacy.class);
        } else {
            pharmacy = pharmacyDao.queryPharmacyById(venue.getPharmacyId());
        }


        /*
        // push to Redis
        if (venue != null) venueService.pushVenueInfoToRedis(venueId);
         */

        resultMap.put("venue", venue);
        resultMap.put("pharmacy", pharmacy);
        resultMap.put("appointmentTime", venue.getAppointmentTime());
        resultMap.put("pharmacyId", venue.getPharmacyId());
        resultMap.put("pharmacyName", pharmacy.getPharmacyName());
        resultMap.put("pharmacyAddress", pharmacy.getPharmacyAddress());
        return "venue_detail";
    }

    // process schduling request
    @RequestMapping("/appointment/schedule/{userId}/{venueId}")
    public ModelAndView pharmacy(@PathVariable long userId, @PathVariable long venueId) {
        boolean spotValidateResult = false;

        ModelAndView modelAndView = new ModelAndView();
        try (Entry entry = SphU.entry("scheduling")) {
            /*
             * check if user is in the list of limited user
             */
            /*
            if (redisService.isInLimitedUser(venueId, userId)) {
                modelAndView.addObject("resultInfo", "sorry, you are in the list of limited user");
                modelAndView.setViewName("scheduling_result");
                return modelAndView;
            }
             */
            // check if user can schedule the appointment
            spotValidateResult = venueService.appointmentSpotValidator(venueId);
            if (spotValidateResult) {
                Appointment appointment = venueService.createAppointment(venueId, userId);
                modelAndView.addObject("resultInfo","Congratulaiton!" +
                        "Your appointment is creating. Please continue and confirm the appointment. " +
                        "The appointment No. is：" + appointment.getAppointmentNo());
                modelAndView.addObject("appointmentNo",appointment.getAppointmentNo());
            } else {
                modelAndView.addObject("resultInfo","Sorry, there are no appointments available");
            }
        // if there are too many visits which server cannot process
        } catch (BlockException ex) {
            log.error("Total scheduling times exceed limit");
            modelAndView.addObject("resultInfo","Sorry, we have unexpected amount of visits. Please come back later");
        // other exceptions
        } catch (Exception e) {
            log.error("Scheduling exception " + e.toString());
            modelAndView.addObject("resultInfo","Sorry, please try again");
        }
        modelAndView.setViewName("scheduling_result");
        return modelAndView;
    }

    // query appointment
    @RequestMapping("/appointment/appointmentQuery/{appointmentNo}")
    public ModelAndView appointmentQuery(@PathVariable String appointmentNo) {
        log.info("query appintment with No.：" + appointmentNo);
        Appointment appointment = appointmentDao.queryAppointment(appointmentNo);
        ModelAndView modelAndView = new ModelAndView();

        if (appointment != null) {
            modelAndView.setViewName("appointment"); // html name
            modelAndView.addObject("appointment", appointment);
            Venue venue = venueDao.queryVenueById(appointment.getVenueId());
            Pharmacy pharmacy = pharmacyDao.queryPharmacyById(venue.getPharmacyId());
            modelAndView.addObject("venue", venue);
            modelAndView.addObject("pharmacy", pharmacy);
            int appointmentTime = venue.getAppointmentTime();
            String appointmentTimeStr = ((appointmentTime < 10) ? "0" : "") + appointmentTime + ":00:00";
            modelAndView.addObject("appointmentTime", appointmentTimeStr);
        } else {
            modelAndView.setViewName("appointment_wait"); // html name
        }
        return modelAndView;
    }

    // confirm appointment
    @RequestMapping("/appointment/confirmAppointment/{appointmentNo}")
    public String confirmAppointment(@PathVariable String appointmentNo) throws Exception {
        venueService.confirmAppointmentProcess(appointmentNo);
        return "redirect:/appointment/appointmentQuery/" + appointmentNo;
    }

    // get system time of the server
    @ResponseBody
    @RequestMapping("/appointment/getSystemTime")
    public String getSystemTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());// new Date() is the system time of the server
        return date;
    }
}