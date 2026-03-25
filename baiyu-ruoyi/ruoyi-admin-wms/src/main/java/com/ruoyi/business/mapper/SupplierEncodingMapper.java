package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.SupplierEncoding;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SupplierEncodingMapper  extends BaseMapper<SupplierEncoding> {

    SupplierEncoding selectBySupplierGroup(String supplierGroup);

}
