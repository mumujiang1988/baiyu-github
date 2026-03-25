package com.ruoyi.business.servicel;

import com.ruoyi.business.entity.Country;

import java.util.List;

public interface CountryService {

    public List<Country> getnAtion();
    
    /**
     * 模糊搜索国家
     * @param keyword 搜索关键词（支持中文、英文）
     * @return 匹配的国家列表
     */
    public List<Country> searchNation(String keyword);

}
