package com.pharmacy.appointment.db.mappers;

import com.pharmacy.appointment.db.po.Venue;

import java.util.List;

public interface VenueMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Venue record);

    int insertSelective(Venue record);

    Venue selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Venue record);

    int updateByPrimaryKey(Venue record);

    List<Venue> queryVenuesByStatus(int venueStatus);

    int lockSpot(Long id);

    int deductSpot(Long id);

    void revertSpot(Long venueId);
}