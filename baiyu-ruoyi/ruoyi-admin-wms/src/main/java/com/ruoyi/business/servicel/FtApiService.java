package com.ruoyi.business.servicel;

import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.entity.FtApiResponse;
import com.ruoyi.business.entity.FtCustomer;
import com.ruoyi.business.entity.FtCustomerQueryRequest;

import java.util.List;

public interface FtApiService {

    FtApiResponse<String> getTgtToken();

    FtApiResponse<String> getAccessToken(String tgtToken);

    FtApiResponse<List<FtCustomer>> getCustomers(FtCustomerQueryRequest queryRequest);
    String getAccessTokenWithTgt();

    void saveOrUpdateLocalCustomer(FtCustomer customer);
}
