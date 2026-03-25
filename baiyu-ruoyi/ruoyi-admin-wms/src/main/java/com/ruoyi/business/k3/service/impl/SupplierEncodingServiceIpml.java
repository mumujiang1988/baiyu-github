package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.SupplierEncoding;
import com.ruoyi.business.k3.service.SupplierEncodingService;
import com.ruoyi.business.mapper.SupplierEncodingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupplierEncodingServiceIpml implements SupplierEncodingService {

    @Autowired
    private SupplierEncodingMapper supplierEncodingMapper;

    @Override
    public SupplierEncoding selectBySupplierGroup(String supplierGroup) {
        return supplierEncodingMapper.selectBySupplierGroup(supplierGroup);
    }
}
