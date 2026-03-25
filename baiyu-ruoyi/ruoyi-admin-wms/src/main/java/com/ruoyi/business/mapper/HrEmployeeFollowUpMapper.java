package com.ruoyi.business.mapper;


import com.ruoyi.business.entity.HrEmployeeFollowUp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HrEmployeeFollowUpMapper {

    int insert(HrEmployeeFollowUp record);

    int updateById(HrEmployeeFollowUp record);

    HrEmployeeFollowUp selectById(@Param("id") Long id);

    List<HrEmployeeFollowUp> selectByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 根据员工编号查询跟进记录
     */
    HrEmployeeFollowUp selectByEmployeeNumber(@Param("employeeNumber") String employeeNumber);

    /**
     * 查询所有员工编号
     */
    List<String> selectAllEmployeeNumbers();

    /**
     * 批量插入跟进记录
     */
    int batchInsert(@Param("list") List<HrEmployeeFollowUp> list);

    /**
     * 批量更新跟进记录
     */
    int batchUpdate(@Param("list") List<HrEmployeeFollowUp> list);
}
