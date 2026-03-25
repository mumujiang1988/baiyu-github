package com.ruoyi.business.servicel.impl;

import com.ruoyi.business.entity.Country;
import com.ruoyi.business.mapper.CountryMapper;
import com.ruoyi.business.servicel.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryServiceImpl implements CountryService {
    @Autowired
    CountryMapper countryMapper;

    @Override
    public List<Country> getnAtion() {
        return countryMapper.selectList();
    }
    
    @Override
    public List<Country> searchNation(String keyword) {
        return countryMapper.searchNation(keyword);
    }
}
