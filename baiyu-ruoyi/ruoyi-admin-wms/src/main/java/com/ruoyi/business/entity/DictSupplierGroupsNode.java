package com.ruoyi.business.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class DictSupplierGroupsNode{
    private String id;
    private Long supplierGroup;
    private String groupName;
    private Integer groupPing;  // 修正字段名
    private Integer parentId;
    private List<DictSupplierGroupsNode> children = new ArrayList<>();
}
