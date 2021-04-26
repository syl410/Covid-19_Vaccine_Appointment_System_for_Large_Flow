package com.pharmacy.appointment.db.dao;

import com.pharmacy.appointment.db.mappers.PharmacyMapper;
import com.pharmacy.appointment.db.po.Pharmacy;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class PharmacyDaoImpl implements PharmacyDao {

    @Resource
    private PharmacyMapper pharmacyMapper;

    @Override
    public Pharmacy queryPharmacyById(long pharmacyId) {
        return pharmacyMapper.selectByPrimaryKey(pharmacyId);
    }
}
