package com.ruoyi.business.servicel.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.business.config.FtApiConfig;
import com.ruoyi.business.entity.*;
import com.ruoyi.business.mapper.FtContactMapper;
import com.ruoyi.business.mapper.FtCustomerMapper;
import com.ruoyi.business.mapper.Ftbank.FtbankMapper;
import com.ruoyi.business.servicel.FtApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
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
    private FtCustomerMapper customerMapper;

    @Autowired
    private FtContactMapper contactMapper;

    @Autowired
    private FtbankMapper ftbankMapper;

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
     * 获取 accessToken
     *
     * @param tgtToken tgtToken（实际上这个接口需要的是 secretKey）
     * @return accessToken
     */
    public FtApiResponse<String> getAccessToken(String tgtToken) {
        try {
            // 根据富通 API 文档，getAccessToken 需要将 secretKey 作为路径参数
            String url = ftApiConfig.getBaseUrl() + "/secret/getAccessToken/" + ftApiConfig.getSecretKey();

            String response = HttpUtil.get(url, (Charset) null);
            log.info("获取 accessToken 请求 URL: {}, 响应：{}", url, response);

            // 解析并返回结果
            FtApiResponse<String> result = JSONUtil.toBean(response, FtApiResponse.class);
            return result;
        } catch (Exception e) {
            log.error("获取 accessToken 失败", e);
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
                    //同步富通数据
                    if (CollectionUtil.isNotEmpty(customers)){
                        //同步
                        customers.forEach(en ->{
                            //客户主表
                            FtCustomer ftCustomer = customerMapper.selectByIds(en.getId());
                            if (ftCustomer != null){
                                customerMapper.updateById(en);
                            }else {
                                customerMapper.insert(en);
                            }

                            // 客户联系人信息
                            if (en.getContactList() != null && !en.getContactList().isEmpty()){
                                en.getContactList().forEach(ens ->{
                                    List<FtContact> contacts = contactMapper.selectByIds(ens.getFtId());
                                    if(contacts != null && !contacts.isEmpty()){
                                        customers.forEach(cu ->{
                                            contactMapper.updateById(ens);
                                        });

                                    }else {
                                        FtContact contact = new FtContact();
                                        if (ens.getId() != null && !ens.getId().toString().isEmpty()){
                                            contact.setFtId(ens.getId().toString());
                                        }
                                        contact.setOperatorName(ens.getOperatorName());
                                        contact.setName(ens.getName());
                                        contact.setSex(ens.getSex());
                                        contact.setJob(ens.getJob());
                                        contact.setEmail(ens.getEmail());
                                        contact.setBirthday(ens.getBirthday());
                                        contact.setMobile(ens.getMobile());
                                        contact.setFax(ens.getFax());
                                        contact.setQq(ens.getQq());
                                        contact.setMsn(ens.getMsn());
                                        contact.setSkype(ens.getSkype());
                                        contact.setAddress(ens.getAddress());
                                        contactMapper.insert(contact);

                                    }
                                });
                            }
                            //客户银行信息
                            if (en.getBankList() != null && !en.getBankList().isEmpty()){
                                en.getBankList().forEach(es ->{
                                    Ftbank bank = ftbankMapper.selectById(es.getId());
                                    if (bank != null){
                                        ftbankMapper.updateById(es);
                                    }else {
                                        ftbankMapper.insert(es);
                                    }
                                });
                            }
                        });
                    }
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
    public void saveOrUpdateLocalCustomer(FtCustomer customer) {
        // 检查客户是否已存在
        FtCustomer existingCustomer = customerMapper.selectById(customer.getId());
        if (existingCustomer != null) {
            // 如果存在则更新
            customerMapper.updateById(existingCustomer);
            log.debug("更新客户数据: {}", customer.getId());
        } else {
            // 如果不存在则插入
            customerMapper.insert(existingCustomer);
            log.debug("插入新客户数据: {}", existingCustomer.getName());
        }
    }
}
