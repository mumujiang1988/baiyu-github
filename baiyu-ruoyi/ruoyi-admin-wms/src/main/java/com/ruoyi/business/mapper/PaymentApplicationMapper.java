package com.ruoyi.business.mapper;

import com.ruoyi.business.k3.domain.entity.PaymentApplicationForm;
import com.ruoyi.business.k3.domain.vo.PaymentApplicationFormVo;
import com.ruoyi.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentApplicationMapper extends BaseMapperPlus<PaymentApplicationForm, PaymentApplicationFormVo> {
}
