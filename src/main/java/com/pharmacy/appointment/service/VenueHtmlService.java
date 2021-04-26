package com.pharmacy.appointment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.pharmacy.appointment.db.dao.VenueDao;
import com.pharmacy.appointment.db.dao.PharmacyDao;
import com.pharmacy.appointment.db.po.Venue;
import com.pharmacy.appointment.db.po.Pharmacy;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VenueHtmlService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private VenueDao venueDao;

    @Autowired
    private PharmacyDao pharmacyDao;

    // create static html page
    public void createVenueHtml(long venueId) {

        PrintWriter writer = null;
        try {
            Venue venue = venueDao.queryVenueById(venueId);
            Pharmacy pharmacy = pharmacyDao.queryPharmacyById(venue.getPharmacyId());
            // get data for the page
            Map<String, Object> resultMap = new HashMap<>();

            resultMap.put("appointmentTime", venue.getAppointmentTime());
            resultMap.put("pharmacyId", venue.getPharmacyId());
            resultMap.put("pharmacyName", pharmacy.getPharmacyName());
            resultMap.put("pharmacyAddress", pharmacy.getPharmacyAddress());
            resultMap.put("venue", venue);
            resultMap.put("pharmacy", pharmacy);

            // create Thymeleaf context object
            Context context = new Context();
            // put data into context object
            context.setVariables(resultMap);

            // create output file for static html
            File file = new File("src/main/resources/templates/" + "venue_detail_" + venueId + ".html");
            writer = new PrintWriter(file);
            // run template engine for static page
            templateEngine.process("venue_detail", context, writer);
        } catch (Exception e) {
            log.error("Error during making static page：" + venueId);
            log.error(e.toString());
        } finally {
            if (writer != null) writer.close();
        }
    }
}

