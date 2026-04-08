package com.ruoyi.business.k3.service;

import java.util.List;

public interface PaymentApplicationService {

    /**
     * 收款单信息表
     * */
    void syncPaymentApplicationList( List<List<Object>> PaymentApplicationList);

}
