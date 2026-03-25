package com.ruoyi.business.entity;


import com.ruoyi.business.entity.PoOrderBillHead;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;


@Data
@AllArgsConstructor
public class PageResult {
private List<PoOrderBillHead> inserts;
private List<PoOrderBillHead> updates;
}
