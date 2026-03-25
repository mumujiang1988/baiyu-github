package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.entity.Customer;
import com.ruoyi.business.k3.domain.vo.CustomerVo;
import com.ruoyi.common.mybatis.annotation.DataColumn;
import com.ruoyi.common.mybatis.annotation.DataPermission;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import com.ruoyi.system.domain.entity.SysUser;
import com.ruoyi.system.domain.vo.SysUserVo;
import io.github.linpeilie.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapperPlus<Customer, CustomerVo> {

    /** 根据主键ID查询 */
    Customer selectById(@Param("id") Long id);

    /** 根据金蝶ID查询（K3同步用） */
    Customer selectByFcustid(@Param("fcustid") Long fcustid);

    /** 根据客户编码查询 */
    Customer selectByFnumber(@Param("fnumber") String fnumber);

    int insert(Customer customer);

    int updateById(Customer customer);

    /**
     * 查询所有客户ID
     */
    List<Long> selectAllFcustids();



    /**
     * 根据金蝶ID更新客户
     */
    int updateByFcustid(Customer customer);

    /**
     * 获取客户列表（支持条件和分页）
     * @param page 查询条件对象
     * @return 客户列表
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "su.user_id")
    })
    Page<Customer> selectList(@Param("page") Page<Customer> page, @Param(Constants.WRAPPER) Wrapper<Customer> queryWrapper);


    /**
     * 根据条件分页查询用户列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "su.user_id")
    })
    List<CustomerVo> selectCustomerList(@Param(Constants.WRAPPER) Wrapper<Customer> queryWrapper);


    /**
     * 根据条件分查询同统计条数
     */
    long countByCondition(@Param("customer") Customer customer);


    /**
     * 根据条件查询客户
     * @param customer 查询条件对象
     */
    List<Customer> selectListByCondition(@Param("customer") Customer customer);

    /**
     * 分页查询客户
     * @param offset 偏移量
     * @param pageSize 页面大小
     */
    List<Customer> selectListByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据客户id删除
     */
    Boolean removeById(@Param("id") String id);

}
