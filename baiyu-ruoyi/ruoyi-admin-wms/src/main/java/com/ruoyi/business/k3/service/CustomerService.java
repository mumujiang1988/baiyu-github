package com.ruoyi.business.k3.service;

import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.k3.domain.bo.CustomerBo;
import com.ruoyi.business.k3.domain.vo.CustomerVo;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;

/**
 * 客户服务接口
 */
public interface CustomerService  {

    /**
     * 多线程同步金蝶客户数据到本地数据库
     * @return 处理的记录总数
     */
    int syncCustomerMultiThread();


    //同步金蝶客户银行卡信息到本地数据库
    int syncCustomerBankInfo();

    //同步金蝶客户转让人信息到本地数据库
    int syncCustomerTransferInfo();
    /**
     * 根据主键ID查询客户
     */
    Customer getById(Long id);

    /**
     * 根据ID删除客户
     *
     * @param id 客户ID
     * @return 是否删除成功
     */
    Boolean removeById(String id);

    /**
     * 新增客户
     */
    Result create(Customer customer);

    /**
     * 根据客户ID更新客户
     */
    boolean update(Customer customer);

    /**
     * 同步金蝶客户联系人信息
     * @return 处理的记录总数
     */
    int syncCustomerContactList();

    /**
     * 批量推送客户列表到金蝶
     * @param customers 客户列表
     * @return Result结果
     */
    Result pushCustomerListToK3(java.util.List<Customer> customers);

    /**
     * 获取客户列表（支持条件和分页）
     * @param pageQuery 查询条件对象
     * @return 客户列表
     */
    TableDataInfo<Customer> getList(CustomerBo customer, PageQuery pageQuery);


}
