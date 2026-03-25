package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.mybatis.annotation.DataColumn;
import com.ruoyi.common.mybatis.annotation.DataPermission;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import com.ruoyi.system.domain.bo.SysUserBo;
import com.ruoyi.system.domain.entity.SysUser;
import com.ruoyi.system.domain.vo.SysUserExportVo;
import com.ruoyi.system.domain.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户表 数据层
 *
 * @author Lion Li
 *
 */
public interface SysUserMapper extends BaseMapperPlus<SysUser, SysUserVo> {

    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    Page<SysUserVo> selectPageUserList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询用户列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    List<SysUserVo> selectUserList(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询已配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    Page<SysUserVo> selectAllocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    Page<SysUserVo> selectUnallocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);


    Long countUserById(Long userId);

    List<SysUserExportVo> selectUserExportList(Wrapper<SysUser> sysUserWrapper);

    List<Map<String, Object>> selectMapByLoginName(@Param("k3key") List<String> k3key);


    SysUserBo selectLoginName(@Param("username") String username);

    SysUserBo selectNickname(@Param("userId") Long userId);

    /**
     * 根据 staff_id 查询用户
     */
    SysUserBo selectUserByK3key(@Param("k3key") String k3key);

    /**
     * 根据 staff_id 查询用户
     */
    SysUserBo selectUserByStaffId(@Param("staffId") String staffId);

    /**
     * 查询所有用户名称
     */
    List<String> selectAllLoginNames();
    List<SysUserBo> selectsStaffIdNames();
    List<SysUserBo> selectsk3keyNames();

    /** 销售员 */
    List<SysUserBo> selectSales();

}
