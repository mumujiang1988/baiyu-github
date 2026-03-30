package com.ruoyi.business.servicel.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.business.config.FtApiConfig;
import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.entity.FtApiResponse;
import com.ruoyi.business.entity.FtCustomer;
import com.ruoyi.business.entity.FtCustomerQueryRequest;
import com.ruoyi.business.mapper.CustomerMapper;
import com.ruoyi.business.servicel.FtApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 富通系统API服务类
 */
@Slf4j
@Service
public class FtApiServiceimpl implements FtApiService {

    @Autowired
    private FtApiConfig ftApiConfig;
    @Autowired
    private CustomerMapper customerMapper;
    /**
     * 获取免登token
     *
     * @return tgtToken
     */
    public FtApiResponse<String> getTgtToken() {
        try {
            String url = ftApiConfig.getBaseUrl() + "/secret/getTgtToken";

            Map<String, Object> params = new HashMap<>();
            params.put("loginName", ftApiConfig.getLoginName());
            params.put("secretKey", ftApiConfig.getSecretKey());

            String response = HttpUtil.get(url, params);
            log.info("获取tgtToken请求URL: {}, 参数: {}, 响应: {}", url, params, response);

            FtApiResponse<String> result = JSONUtil.toBean(response, FtApiResponse.class);
            return result;
        } catch (Exception e) {
            log.error("获取tgtToken失败", e);
            FtApiResponse<String> errorResult = new FtApiResponse<>();
            errorResult.setSuccess(false);
            errorResult.setCode("-1");
            errorResult.setMessage(e.getMessage());
            return errorResult;
        }
    }

    /**
     * 获取accessToken
     *
     * @param tgtToken tgtToken
     * @return accessToken
     */
    public FtApiResponse<String> getAccessToken(String tgtToken) {
        try {
            String url = ftApiConfig.getBaseUrl() + "/secret/getAccessToken";

            Map<String, Object> params = new HashMap<>();
            params.put("tgtToken", tgtToken);

            String response = HttpUtil.get(url, params);
            log.info("获取accessToken请求URL: {}, 参数: {}, 响应: {}", url, params, response);

            FtApiResponse<String> result = JSONUtil.toBean(response, FtApiResponse.class);
            return result;
        } catch (Exception e) {
            log.error("获取accessToken失败", e);
            FtApiResponse<String> errorResult = new FtApiResponse<>();
            errorResult.setSuccess(false);
            errorResult.setCode("-1");
            errorResult.setMessage(e.getMessage());
            return errorResult;
        }
    }

    /**
     * 查询客户列表
     *
     * @param queryRequest 查询条件
     * @return 客户列表
     */
    public FtApiResponse<List<FtCustomer>> getCustomers(FtCustomerQueryRequest queryRequest) {
        try {
            String url = ftApiConfig.getBaseUrl() + "/secret/getCustomers";

            // 将请求对象转换为JSON字符串
            String requestBody = JSONUtil.toJsonStr(queryRequest);

            String response = HttpUtil.post(url, requestBody);
            log.info("查询客户列表请求URL: {}, 请求体: {}, 响应: {}", url, requestBody, response);

            // 解析响应，注意这里的data可能是一个包含客户列表的复杂对象
            FtApiResponse<List<FtCustomer>> result = new FtApiResponse<>();
            FtApiResponse rawResponse = JSONUtil.toBean(response, FtApiResponse.class);

            result.setCode(rawResponse.getCode());
            result.setSuccess(rawResponse.getSuccess());
            result.setMessage(rawResponse.getMessage());

            // 如果请求成功，尝试解析客户数据
            if (rawResponse.isSuccess()) {
                // 由于返回的数据结构可能比较复杂，需要根据实际情况解析
                // 这里假设data字段直接包含客户列表，实际情况可能需要调整
                try {
                    // 尝试解析客户列表
                    List<FtCustomer> customers = JSONUtil.toList(JSONUtil.toJsonStr(rawResponse.getData()), FtCustomer.class);
                    result.setData(customers);
                } catch (Exception e) {
                    log.warn("解析客户数据失败，尝试其他方式解析", e);
                    // 如果直接解析失败，可能需要根据实际API返回的数据结构调整解析方式
                    result.setData(null);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("查询客户列表失败", e);
            FtApiResponse<List<FtCustomer>> errorResult = new FtApiResponse<>();
            errorResult.setSuccess(false);
            errorResult.setCode("-1");
            errorResult.setMessage(e.getMessage());
            return errorResult;
        }
    }

    /**
     * 获取tgtToken并获取accessToken的便捷方法
     *
     * @return accessToken
     */
    public String getAccessTokenWithTgt() {
        FtApiResponse<String> tgtResult = getTgtToken();
        if (tgtResult.isSuccess()) {
            String tgtToken = tgtResult.getData();
            FtApiResponse<String> accessTokenResult = getAccessToken(tgtToken);
            if (accessTokenResult.isSuccess()) {
                return accessTokenResult.getData();
            } else {
                log.error("获取accessToken失败: {}", accessTokenResult.getMessage());
            }
        } else {
            log.error("获取tgtToken失败: {}", tgtResult.getMessage());
        }
        return null;
    }

    /**
     * 保存或更新本地客户数据
     */
    public void saveOrUpdateLocalCustomer(Customer customer) {
        // 检查客户是否已存在
        Customer existingCustomer = customerMapper.selectById(customer.getId());
        if (existingCustomer != null) {
            // 如果存在则更新
            customerMapper.updateById(customer);
            log.debug("更新客户数据: {}", customer.getFname());
        } else {
            // 如果不存在则插入
            customerMapper.insert(customer);
            log.debug("插入新客户数据: {}", customer.getFname());
        }
    }
}
