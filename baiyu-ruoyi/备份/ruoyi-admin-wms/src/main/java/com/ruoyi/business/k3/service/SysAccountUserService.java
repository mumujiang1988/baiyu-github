package com.ruoyi.business.k3.service;




import com.ruoyi.business.entity.SysAccountUser;
import com.ruoyi.system.domain.bo.SysUserBo;

import java.util.List;

public interface SysAccountUserService {

    List<SysUserBo> getSysAccountUser();

    List<SysUserBo> getSysAccountUserk3key();

    /** 销售员 */
    List<SysUserBo> getSysAccountSales();
}
