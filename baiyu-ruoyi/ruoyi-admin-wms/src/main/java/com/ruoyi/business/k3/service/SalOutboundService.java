package com.ruoyi.business.k3.service;

import java.util.List;

public interface SalOutboundService {

    //同步销售出库单主表数据
    void syncSalOutboundList(List<List<Object>> salOutboundList);

}
