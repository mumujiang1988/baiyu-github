package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.PoOrderBillHead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购订单主表Mapper
 */
@Mapper
public interface PoOrderBillHeadMapper {

    /**
     * 插入采购订单
     * @param poOrderBillHead 采购订单信息
     * @return 影响行数
     */
    int insert(PoOrderBillHead poOrderBillHead);
    /**
     * 查询所有采购订单编号
     * @return 采购订单编号列表
     */
    List<String> selectAllBillNos();

    /**
     * 查询所有金蝶主键ID
     * @return 金蝶主键ID列表
     */
    List<String> selectAllK3Ids();

    /**
     * 根据金蝶主键ID更新采购订单
     * @param poOrderBillHead 采购订单信息
     * @return 影响行数
     */
    int updateByK3Id(PoOrderBillHead poOrderBillHead);

    /**
     * 根据金蝶主键ID查询采购订单
     * @param fid 金蝶主键ID
     * @return 采购订单信息
     */
    PoOrderBillHead selectByK3Id(@Param("fid") String fid);



    /**
     * 根据单据编号更新采购订单
     * @param poOrderBillHead 采购订单信息
     * @return 影响行数
     */
    int updateByBillNo(PoOrderBillHead poOrderBillHead);

    /**
     * 批量更新采购订单（按单据编号）
     * @param list 采购订单列表
     * @return 影响行数
     */
    int batchUpdateByBillNo(@Param("list") List<PoOrderBillHead> list);

    /**
     * 根据单据编号查询采购订单
     * @param fbillNo 单据编号
     * @return 采购订单信息
     */
    PoOrderBillHead selectByBillNo(@Param("fbillNo") String fbillNo);

    /**
     * 根据主键查询采购订单
      * @param id 主键
     * @return 采购订单信息
     */
    PoOrderBillHead selectById(@Param("id") Long id);

    /**
     * 查询所有采购订单
     * @return 采购订单列表
     */
    List<PoOrderBillHead> selectAll();


    /**
     * 删除所有采购订单数据
     * @return 影响行数
     */
    int deleteAll();

    /**
     * 查询已存在的K3 ID列表
     * @param fids K3 ID列表
     * @return 数据库中存在的K3 ID列表
     */
    List<String> selectExistingK3Ids(@Param("list") List<String> fids);

    /**
     * 分页查询采购订单
     * @param page 分页参数
     * @param poOrderBillHead 查询条件
     * @return 分页结果
     */
    Page<PoOrderBillHead> selectPage(Page<PoOrderBillHead> page, PoOrderBillHead poOrderBillHead);
}
