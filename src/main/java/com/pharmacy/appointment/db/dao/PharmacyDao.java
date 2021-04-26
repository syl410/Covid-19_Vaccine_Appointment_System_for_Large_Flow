package com.pharmacy.appointment.db.dao;

import com.pharmacy.appointment.db.po.Pharmacy;

public interface PharmacyDao {

    public Pharmacy queryPharmacyById(long pharmacyId);
}
