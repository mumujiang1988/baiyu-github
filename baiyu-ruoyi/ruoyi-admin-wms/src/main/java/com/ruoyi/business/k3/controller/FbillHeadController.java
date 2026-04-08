package com.ruoyi.business.k3.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.business.k3.config.k3config;
import com.ruoyi.business.k3.domain.bo.FbillHeadBo;
import com.ruoyi.business.k3.domain.entity.FbillHead;
import com.ruoyi.business.k3.service.FbillHeadService;
import com.ruoyi.business.util.Result;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/k3/billhead")
@Validated
@Slf4j
public class FbillHeadController {

    @Autowired
    private FbillHeadService billHeadService;

    @Resource
    private k3config k3configks;

    /**
     * 同步金蝶采购调价表信息
     */
    @PostMapping("/login")
    @Transactional(rollbackFor = Exception.class)  // 添加类级别的事务支持
    public Result login() {
        //获取采购价目主表数据
        List<List<Object>> billHeadList =  k3configks.billHeadList() ;
        billHeadService.syncBillHeadList(billHeadList);
//        //获取采购价目明细表
        List<List<Object>> fpurpatentryList =  k3configks.fpurpatentryList() ;
        billHeadService.syncFpurPatentry(fpurpatentryList);
        return Result.success();
    }


    /**
     * 分页查询采购调价表列表（支持条件查询）
     * @param billHead 查询条件
     * @param pageQuery 分页参数
     * @return 采购调价表分页列表
     */
    @SaCheckPermission("k3:billhead:list")
    @GetMapping("/list")
    public TableDataInfo<FbillHead> list(FbillHeadBo billHead, PageQuery pageQuery) {
        return billHeadService.list(billHead, pageQuery);
    }

    /**
     * 根据编码查采购调价表（包含明细）
     *
     * @param ids 采购调价编码
     * @return 采购调价表信息
     */
    @SaCheckPermission("k3:billhead:query")
    @GetMapping("/query")
    public Result getByNumber(@RequestParam("ids") String ids) {
        try {
            FbillHead head = billHeadService.getById(Long.valueOf(ids));
            if (head != null) {
                return Result.success(head);
            } else {
                return Result.error("未找到该采购调价表");
            }
        } catch (Exception e) {
            return Result.error("查询采购调价表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID删除价目表（级联删除明细）
     *
     * @param id 价目表ID
     * @return 操作结果
     */
    @SaCheckPermission("k3:billhead:delete")
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        try {
            boolean result = billHeadService.deleteById(id);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除采购调价失败: " + e.getMessage());
        }
    }


}
