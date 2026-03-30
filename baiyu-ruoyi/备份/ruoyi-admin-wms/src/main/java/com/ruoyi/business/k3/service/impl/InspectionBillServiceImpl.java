package com.ruoyi.business.k3.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.business.entity.ReceiptNoticeFull;
import com.ruoyi.business.k3.config.Dictionaryconfig;
import com.ruoyi.business.k3.service.InspectionBillService;
import com.ruoyi.business.k3.util.K3DataUtils;
import com.ruoyi.business.mapper.ReceiptNoticeFullMapper;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 检验单服务实现类
 */
@Slf4j
@Service
public class InspectionBillServiceImpl implements InspectionBillService {

    @Autowired
    private Dictionaryconfig dictionaryconfig;

    @Autowired
    private ReceiptNoticeFullMapper receiptNoticeFullMapper;

    @Override
    @Transactional
    public void syncInspectionBill() {
        try {
            log.info("开始同步检验单数据...");


            int pageSize = 100;
            int currentPage = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                // 从金蝶获取检验单完整数据
                List<List<Object>> k3Data = dictionaryconfig.queryInspectionBillFullData(currentPage * pageSize, pageSize);

                if (k3Data == null || k3Data.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                // 转换为完整实体对象
                List<ReceiptNoticeFull> fullDataList = convertToReceiptNoticeFull(k3Data);

                for (ReceiptNoticeFull fullData : fullDataList) {
                    ReceiptNoticeFull existingData = receiptNoticeFullMapper.selectById(fullData.getFid());
                    if (existingData != null) {
                        // 如果存在，更新记录
                        receiptNoticeFullMapper.updateById(fullData);
                        log.debug("更新检验单完整数据: {}", fullData.getFBillNo());
                    } else {
                        // 如果不存在，插入新记录
                        receiptNoticeFullMapper.insert(fullData);
                        log.debug("插入检验单完整数据: {}", fullData.getFBillNo());
                    }
                }

                // 如果返回的数据少于页面大小，说明没有更多数据
                if (k3Data.size() < pageSize) {
                    hasMoreData = false;
                }

                currentPage++;
            }

            log.info("检验单完整数据同步完成");
            log.info("检验单数据同步完成");
        } catch (Exception e) {
            log.error("同步检验单数据失败", e);
            throw new RuntimeException("同步检验单数据失败", e);
        }
    }

    @Override
    @Transactional
    public boolean addInspectionBill(ReceiptNoticeFull receiptNoticeFull) {
        try {
            log.info("开始新增检验单数据: {}", receiptNoticeFull.getFBillNo());

            //检查是否已存在相同单据编号的数据
            ReceiptNoticeFull existingData = receiptNoticeFullMapper.selectReceiptNoticeFullByBillNo(receiptNoticeFull.getFBillNo());
            if (existingData != null) {
                log.warn("检验单数据已存在，单据编号: {}", receiptNoticeFull.getFBillNo());
                return false;
            }

            //插入新数据
            int result = receiptNoticeFullMapper.insertReceiptNotice(receiptNoticeFull);
            if (result > 0) {
                log.info("新增检验单数据成功: {}", receiptNoticeFull.getFBillNo());
                return true;
            } else {
                log.error("新增检验单数据失败: {}", receiptNoticeFull.getFBillNo());
                return false;
            }
        } catch (Exception e) {
            log.error("新增检验单数据异常", e);
            throw new RuntimeException("新增检验单数据失败", e);
        }
    }

    @Override
    public List<ReceiptNoticeFull> pageInspectionBills(long current, long size, ReceiptNoticeFull receiptNoticeFull) {
        try {
            log.info("开始分页查询检验单数据，当前页：{},大小：{}", current, size);

            // 计算偏移量
            int offset = (int) ((current - 1) * size);

            // 直接调用 Mapper 的分页查询方法
            List<ReceiptNoticeFull> list = receiptNoticeFullMapper.selectReceiptNoticeFullByPage(receiptNoticeFull, offset, (int) size);

            return list;
        } catch (Exception e) {
            log.error("分页查询检验单数据失败", e);
            throw new RuntimeException("分页查询检验单数据失败", e);
        }
    }

    @Override
    public ReceiptNoticeFull getInspectionBillById(Long id) {
        try {
            log.info("开始根据 ID 查询检验单详情，ID: {}", id);

            // 调用 Mapper 查询检验单详情
            ReceiptNoticeFull receiptNoticeFull = receiptNoticeFullMapper.selectReceiptNoticeFullById(id);

            log.info("根据 ID 查询检验单详情成功，单据编号：{}", receiptNoticeFull.getFBillNo());
            return receiptNoticeFull;
        } catch (Exception e) {
            log.error("根据 ID 查询检验单详情失败", e);
            throw new RuntimeException("根据 ID 查询检验单详情失败", e);
        }
    }


    /**
     * 将金蝶返回的完整数据转换为ReceiptNoticeFull实体
     */
    private List<ReceiptNoticeFull> convertToReceiptNoticeFull(List<List<Object>> k3Data) {
        List<ReceiptNoticeFull> fullDataList = new ArrayList<>();

        for (List<Object> row : k3Data) {
            ReceiptNoticeFull fullData = new ReceiptNoticeFull();

            // 按照金蝶返回的字段顺序设置值
           if (row.size() >= 50) { // 14个主表字段 + 36个明细表字段
                int index = 0;
                // 主表字段
                fullData.setFid(K3DataUtils.getLong(row, index++));
                fullData.setFBillNo(K3DataUtils.getString(row, index++));
                fullData.setFDocumentStatus(K3DataUtils.getString(row, index++));
                fullData.setFApproverId(K3DataUtils.getLong(row, index++));
                fullData.setFApproveDate(K3DataUtils.getDate(row, index++));
                fullData.setFModifierId(K3DataUtils.getLong(row, index++));
                fullData.setFCreateDate(K3DataUtils.getDate(row, index++));
                fullData.setFCreatorId(K3DataUtils.getLong(row, index++));
                fullData.setFModifyDate(K3DataUtils.getDate(row, index++));
                fullData.setFCancelDate(K3DataUtils.getDate(row, index++));
                fullData.setFCanceler(K3DataUtils.getString(row, index++));
                fullData.setFBillTypeId(K3DataUtils.getString(row, index++));
                fullData.setFBusinessType(K3DataUtils.getString(row, index++));
                fullData.setFDate(K3DataUtils.getDateOnly(row, index++));
                fullData.setFMaterialId(K3DataUtils.getString(row, index++));
                fullData.setFMaterialName(K3DataUtils.getString(row, index++));
                fullData.setFMaterialModel(K3DataUtils.getString(row, index++));
                fullData.setFQcSchemeId(K3DataUtils.getString(row, index++));
                fullData.setFUnitId(K3DataUtils.getString(row, index++));
                fullData.setFBaseUnitId(K3DataUtils.getString(row, index++));
                fullData.setFInspectQty(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFQualifiedQty(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFUnqualifiedQty(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFInspectResult(K3DataUtils.getString(row, index++));
                fullData.setFQcStatus(K3DataUtils.getString(row, index++));
                fullData.setFSupplierId(K3DataUtils.getString(row, index++));
                fullData.setFStockId(K3DataUtils.getString(row, index++));
                fullData.setFLot(K3DataUtils.getString(row, index++));
                fullData.setFzxs(K3DataUtils.getInteger(row, index++));
                fullData.setFmz(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFjz(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFChang(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFKuan(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFGao(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFZxshu(K3DataUtils.getInteger(row, index++));
                fullData.setFTj(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFDiscountQty(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFMzh(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFbzyq(K3DataUtils.getString(row, index++));
                fullData.setFTsyq(K3DataUtils.getString(row, index++));
                fullData.setFcptp(K3DataUtils.getString(row, index++));
                fullData.setFXsddh(K3DataUtils.getString(row, index++));
                fullData.setFCgddh(K3DataUtils.getString(row, index++));
                fullData.setFKhjc(K3DataUtils.getString(row, index++));
                fullData.setFCtyPicture(K3DataUtils.getString(row, index++));
                fullData.setFCtyPicture1(K3DataUtils.getString(row, index++));
                fullData.setFPeuuAttachmentCa9(K3DataUtils.getString(row, index++));
                fullData.setFnhc(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFnhk(K3DataUtils.getBigDecimal(row, index++));
                fullData.setFnhg(K3DataUtils.getBigDecimal(row, index++));
            }

            fullDataList.add(fullData);
        }

        return fullDataList;
    }
}

