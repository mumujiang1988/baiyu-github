package com.ruoyi.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    Employee selectById(@Param("fid") Long fid);

    Employee selectByStaffNumber(@Param("fstaffNumber") String fstaffNumber);

    Employee selectByFName(@Param("fname") String fname);


    /**
     * 根据销售员ID查询员工
     */
    Employee selectBySalesmanId(@Param("salesmanId") String salesmanId);

    /**
     * 根据员工编号查询员工（精确匹配）
     */
    Employee selectByExactStaffNumber(@Param("fstaffNumber") String fstaffNumber);

    /**
     * 查询所有员工ID
     */
    List<Long> selectAllFids();

    /**
     * 批量插入员工
     */
    int batchInsert(@Param("list") List<Employee> list);

    /**
     * 批量更新员工
     */
    int batchUpdateByFid(@Param("list") List<Employee> list);
}
