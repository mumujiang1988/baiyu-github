package com.ruoyi.business.k3.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.Supplier;
import com.ruoyi.business.entity.SysDataAuditLog;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;




import java.util.List;

public interface SupplierService {


    /**
     * 同步金蝶供应商数据
    * */
    boolean querySupplierList(List<List<Object>> querySupplierList);
    /**
     * 同步金蝶供应商拜访记录数据
     *
     * @param visitRecordList 从金蝶获取的供应商拜访记录数据
     * @return 是否同步成功
     */
    boolean syncSupplierVisitRecordFromK3(List<List<Object>> visitRecordList);
    /**
     * 添加供应商
     * @param supplier 供应商信息
     * @return 是否添加成功
     */
    Result save(Supplier supplier) throws Exception;

    /**
     * 根据ID更新供应商信息
     * @param supplier 供应商信息
     * @return 是否更新成功
     */
    boolean updateById(Supplier supplier);


    /**
     * 根据ID删除供应商
     * @param number 供应商ID
     * @return 是否删除成功
     */
    boolean removeById(String number);

    /**
     * 根据ID查询供应商
     * @param number 供应商ID
     * @return 供应商信息
     */
    Supplier getById(String number);

    /**
     * 查询所有供应商
     * @return 供应商列表
     */
    List<Supplier> list(Supplier supplier);

    /**
     * 分页查询供应商列表
     * @param supplier 查询条件
     * @param page 页码
     * @param size 每页数量
     * @return 分页数据
     */
    Page<Supplier> listSuppliers(Supplier supplier, int page, int size);


    /**根据编码查询供应商
     * */
    Supplier getMaterialByNumberDirect(String number);

    Supplier selectBySupplierGroup(String supplierGroup);

    /**
     * 根据表名和主键ID查询审计日志（分页）
     ** @return 审计日志分页数据
     */
    Page<SysDataAuditLog> getAuditLogsByTableAndId(String tableName, String rowId, int pageNum, int pageSize);


}
