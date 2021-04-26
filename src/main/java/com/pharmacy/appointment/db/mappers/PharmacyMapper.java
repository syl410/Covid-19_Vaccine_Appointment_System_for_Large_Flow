package com.pharmacy.appointment.db.mappers;

import com.pharmacy.appointment.db.po.Pharmacy;

public interface PharmacyMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Pharmacy record);

    int insertSelective(Pharmacy record);

    Pharmacy selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Pharmacy record);

    int updateByPrimaryKey(Pharmacy record);
}