package com.ruoyi.business.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DictNode {
    private String id;
    private String name;
    private String materialgroup;
    private String dictCode;
    private String parentCode;
    private List<DictNode> children = new ArrayList<>();
}
