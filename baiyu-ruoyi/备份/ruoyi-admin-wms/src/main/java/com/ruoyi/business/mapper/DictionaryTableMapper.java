package com.ruoyi.business.mapper;

import com.ruoyi.business.entity.DictionaryTable;
import com.ruoyi.business.entity.SupplierGroups;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictionaryTableMapper {

    /**
     * 新增字典项
     */
    int insert(DictionaryTable dictionaryTable);

    /**
     * 修改字典项
     */
    int update(DictionaryTable dictionaryTable);

    /**
     * 根据ID删除字典项
     */
    int deleteById(Long id);

    /**
     * 批量删除字典项
     */
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * 根据ID查询字典项
     */
    DictionaryTable selectById(Long id);
    /**
     * 条件查询字段
     * */
    DictionaryTable selectByCondition(@Param("condition")DictionaryTable dictionaryTable);
    /**
     * 查询所有字典项
     */
    List<DictionaryTable> selectAll();

    /**
     *
     * 供应商分组
     * */
    List<SupplierGroups> selectSupplierGroupsAll();

    /**
     * 根据k3id查询供应商分组字典
     *
     * @param supplierGroup
     * @return 供应商分组字典信息
     */
    SupplierGroups SupplierGroupsK3id(String supplierGroup);

    /**
     *根据名称查询供应商分组字典
     *
     * @param groupName
     * @return 供应商分组字典信息
     */
    SupplierGroups selectByGroupName(String groupName);

    /**
     *通k3_id查询供应商分组
     */
    SupplierGroups selectBySupplierGroup(String supplierGroup);

    /**
     * 根据父级编码查询子级字典项
     */
    List<DictionaryTable> selectByParentCode(String parentCode);

    /**
     * 根据字典编码查询字典项
     */
    DictionaryTable selectByDictCode(String dictCode);

    DictionaryTable selectByDictBilhead(String bilhead);

    /**
     * 根据字典名称查询
     * */
    @Select("SELECT COUNT(*) FROM dictionary_table WHERE dict_name = #{dictName}")
    int selectByDictName(@Param("dictName") String dictName);
}
