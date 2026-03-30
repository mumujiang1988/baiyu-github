package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.PoOrderBillHeadEntry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PurchaseOrderEntryMapper {

    int insert(PoOrderBillHeadEntry entity);


    int deleteByBillNo(@Param("fid") String fid);

    int updateByBillNo(@Param("entity") PoOrderBillHeadEntry entity);

    int updateByFidAndFgyswlbm(@Param("entity") PoOrderBillHeadEntry entity);

    List<PoOrderBillHeadEntry> selectByBillNo(@Param("fbillNo") String fbillNo);

    List<PoOrderBillHeadEntry> selectByFidAndFgyswlbm(@Param("fid") String fid, @Param("fGyswlbm") String fGyswlbm);

}
