package com.pharmacy.appointment.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.pharmacy.appointment.db.dao.VenueDao;
import com.pharmacy.appointment.db.dao.PharmacyDao;
import com.pharmacy.appointment.db.po.Venue;
import com.pharmacy.appointment.db.po.Pharmacy;
import com.pharmacy.appointment.util.RedisService;
import com.alibaba.fastjson.JSON;

import java.util.List;

@Component
public class RedisWarmupRunner implements ApplicationRunner {

    @Autowired
    VenueDao venueDao;

    @Autowired
    PharmacyDao pharmacyDao;

    @Autowired
    RedisService redisService;

    // sync up spots and details of venues, venue ID list and pharmacy information from DB to Redis
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Venue> venues = venueDao.queryVenuesByStatus(1);
        StringBuilder venueIdSb = new StringBuilder(); // "id1#id2#id3..."
        System.out.println("*** Start cache warming");
        for (Venue venue : venues) {
            // warm up for spots of a venue
            redisService.setValue("venue:" + venue.getId(), venue.getAvailableSpot());

            // warm up for details of a venue
            redisService.setValue("venue_detail:" + venue.getId(), JSON.toJSONString(venue));

            // get venue ID list string
            venueIdSb.append(venue.getId() + "#");

            // warm up for pharmacy information
            Pharmacy pharmacy = pharmacyDao.queryPharmacyById(venue.getPharmacyId());
            redisService.setValue("pharmacy:" + venue.getPharmacyId(), JSON.toJSONString(pharmacy));
        }

        // warm up for venue ID list
        redisService.setValue("venue_ID_list", venueIdSb.toString());
    }

}
