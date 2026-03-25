package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.SysAccountUser;
import com.ruoyi.business.k3.service.SysAccountUserService;
import com.ruoyi.system.domain.bo.SysUserBo;
import com.ruoyi.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysAccountUserServiceimpl implements SysAccountUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public List<SysUserBo> getSysAccountUser() {

        return sysUserMapper.selectsStaffIdNames();
    }

    @Override
    public List<SysUserBo> getSysAccountUserk3key() {
        return sysUserMapper.selectsStaffIdNames();
    }

    /** 销售员 */
    @Override
    public List<SysUserBo> getSysAccountSales() {
        return sysUserMapper.selectSales();
    }
}
