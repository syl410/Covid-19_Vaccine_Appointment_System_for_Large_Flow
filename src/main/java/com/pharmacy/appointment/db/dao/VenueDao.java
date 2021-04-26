package com.pharmacy.appointment.db.dao;

import com.pharmacy.appointment.db.po.Venue;

import java.util.List;

public interface VenueDao {

    public List<Venue> queryVenuesByStatus(int venueStatus);

    public void inertVenue(Venue venue);

    public Venue queryVenueById(long venueId);

    public void updateVenue(Venue venue);

    boolean lockSpot(Long venueId);

    boolean deductSpot(Long venueId);

    void revertSpot(Long venueId);
}
