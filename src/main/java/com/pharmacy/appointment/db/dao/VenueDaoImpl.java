package com.pharmacy.appointment.db.dao;

import javax.annotation.Resource;
import java.util.List;

import com.pharmacy.appointment.db.mappers.VenueMapper;
import com.pharmacy.appointment.db.po.Venue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class VenueDaoImpl implements VenueDao {

    @Resource
    private VenueMapper venueMapper;

    @Override
    public List<Venue> queryVenuesByStatus(int venueStatus) {
        return venueMapper.queryVenuesByStatus(venueStatus);
    }

    @Override
    public void inertVenue(Venue venue) {
        venueMapper.insert(venue);
    }

    @Override
    public Venue queryVenueById(long venueId) {
        return venueMapper.selectByPrimaryKey(venueId);
    }

    @Override
    public void updateVenue(Venue venue) {
        venueMapper.updateByPrimaryKey(venue);
    }

    @Override
    public boolean lockSpot(Long venueId) {
        int result = venueMapper.lockSpot( venueId );
        if (result < 1) {
            log.error("Failed to log the spot");
            return false;
        }
        return true;
    }

    @Override
    public boolean deductSpot(Long venueId) {
        int result = venueMapper.deductSpot(venueId);
        if (result < 1) {
            log.error("Failed to deduct the spot");
            return false;
        }
        return true;
    }

    @Override
    public void revertSpot(Long venueId) {
        venueMapper.revertSpot(venueId);
    }
}
