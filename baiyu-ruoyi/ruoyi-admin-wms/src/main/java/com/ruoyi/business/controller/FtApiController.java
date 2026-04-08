package com.ruoyi.business.controller;

import com.ruoyi.business.entity.FtCustomer;
import com.ruoyi.business.entity.FtCustomerQueryRequest;
import com.ruoyi.business.servicel.FtApiService;
import com.ruoyi.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 富通系统 API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/ft/api")
public class FtApiController {

    @Autowired
    private FtApiService ftApiService;

    /**
     * 获取免登 token
     */
    @GetMapping("/getTgtToken")
    public R<String> getTgtToken() {
        try {
            var result = ftApiService.getTgtToken();
            if (result.isSuccess()) {
                return R.ok(result.getData(), "获取 tgtToken 成功");
            } else {
                return R.fail(result.getMessage() != null ? result.getMessage() : "获取 tgtToken 失败");
            }
        } catch (Exception e) {
            log.error("获取 tgtToken 异常", e);
            return R.fail("获取 tgtToken 异常：" + e.getMessage());
        }
    }

    /**
     * 查询客户列表（分页查询全部数据）
     * @param num 页码，默认 1
     * @param size 每页条数，默认 20，最大 100
     */
    @PostMapping("/getCustomers")
    public R<List<FtCustomer>> getCustomers(
            @RequestParam(value = "num") Integer num,
            @RequestParam(value = "size") Integer size) {
        try {
            // 创建查询请求对象，只设置分页参数
            FtCustomerQueryRequest request = new FtCustomerQueryRequest();
            request.setNum(num);
            request.setSize(size);
            // 自动获取 accessToken
            String accessToken = ftApiService.getAccessTokenWithTgt();
            if (accessToken != null) {
                request.setAccessToken(accessToken);
            } else {
                return R.fail("无法获取访问令牌，请检查富通系统配置");
            }
            // 调用服务层查询客户列表
            var result = ftApiService.getCustomers(request);
            if (result.isSuccess() == true) {
            // 自动获取 accessToken
            String accessToken = ftApiService.getAccessTokenWithTgt();
            if (accessToken != null) {
                request.setAccessToken(accessToken);
            } else {
                return R.fail("无法获取访问令牌，请检查富通系统配置");
            }

            // 调用服务层查询客户列表
            var result = ftApiService.getCustomers(request);
            if (result.isSuccess()) {
                return R.ok("查询客户列表成功", result.getData());
            } else {
                return R.fail(result.getMessage() != null ? result.getMessage() : "查询客户列表失败");
            }
        } catch (Exception e) {
            log.error("查询客户列表异常", e);
            return R.fail("查询客户列表异常：" + e.getMessage());
        }
    }

    /**
     * 便捷接口：获取 accessToken
     */
    @GetMapping("/getAccessToken")
    public R<String> getAccessToken() {
        try {
            String accessToken = ftApiService.getAccessTokenWithTgt();
            if (accessToken != null) {
                return R.ok(accessToken, "获取 accessToken 成功");
            } else {
                return R.fail("获取 accessToken 失败");
            }
        } catch (Exception e) {
            log.error("获取 accessToken 异常", e);
            return R.fail("获取 accessToken 异常：" + e.getMessage());
        }
    }
}
