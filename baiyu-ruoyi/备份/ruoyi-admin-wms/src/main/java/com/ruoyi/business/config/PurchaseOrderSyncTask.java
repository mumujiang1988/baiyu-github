package com.ruoyi.business.config;


import com.ruoyi.business.entity.PageResult;
import com.ruoyi.business.entity.PoOrderBillHead;
import com.ruoyi.business.k3.service.PurchaseOrderService;
import com.ruoyi.business.k3.util.PurchaseOrderDataConverter;
import lombok.extern.slf4j.Slf4j;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;


@Slf4j
public class PurchaseOrderSyncTask implements Callable<PageResult> {


    private final PurchaseOrderService purchaseOrderService;
    private final List<List<Object>> pageList;
    private final Set<String> existBillNos;


    public PurchaseOrderSyncTask(PurchaseOrderService purchaseOrderService,
                                 List<List<Object>> pageList,
                                 Set<String> existBillNos) {
        this.purchaseOrderService = purchaseOrderService;
        this.pageList = pageList;
        this.existBillNos = existBillNos;
    }


    @Override
    public PageResult call() {
        List<PoOrderBillHead> inserts = new ArrayList<>();
        List<PoOrderBillHead> updates = new ArrayList<>();


        for (List<Object> row : pageList) {
            try {
                PoOrderBillHead h = PurchaseOrderDataConverter.parsePurchaseOrderFromRow(row);
            if (h.getFid() == null) continue;
                if (existBillNos.contains(h.getFid())) {
                    updates.add(h);
                } else {
                    inserts.add(h);
                // 将新单号同时加入 existBillNos，防止同批次重复插入
                    existBillNos.add(h.getFid());
                }
            } catch (Exception e) {
                log.error("解析行失败: {}", row, e);
            }
        }


        return new PageResult(inserts, updates);
    }
}
