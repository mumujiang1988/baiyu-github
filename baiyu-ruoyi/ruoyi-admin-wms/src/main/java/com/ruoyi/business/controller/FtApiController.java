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
 * 富通系统API控制器
 */
@Slf4j
@RestController
@RequestMapping("/ft/api")
public class FtApiController {

    @Autowired
    private FtApiService ftApiService;

    /**
     * 获取免登token
     */
    @GetMapping("/getTgtToken")
    public R<String> getTgtToken() {
        try {
            var result = ftApiService.getTgtToken();
            if (result.isSuccess()) {
                return R.ok(result.getData(), "获取tgtToken成功");
            } else {
                return R.fail(result.getMessage() != null ? result.getMessage() : "获取tgtToken失败");
            }
        } catch (Exception e) {
            log.error("获取tgtToken异常", e);
            return R.fail("获取tgtToken异常: " + e.getMessage());
        }
    }

    /**
     * 查询客户列表
     */
    @PostMapping("/getCustomers")
    public R<List<FtCustomer>> getCustomers(@RequestBody FtCustomerQueryRequest request) {
        try {
            // 如果请求中没有提供accessToken，尝试获取
            if (request.getAccessToken() == null || request.getAccessToken().isEmpty()) {
                String accessToken = ftApiService.getAccessTokenWithTgt();
                if (accessToken != null) {
                    request.setAccessToken(accessToken);
                } else {
                    return R.fail("无法获取访问令牌，请检查富通系统配置");
                }
            }

            var result = ftApiService.getCustomers(request);
            if (result.isSuccess()) {
                return R.ok( "查询客户列表成功",result.getData());
            } else {
                return R.fail(result.getMessage() != null ? result.getMessage() : "查询客户列表失败");
            }
        } catch (Exception e) {
            log.error("查询客户列表异常", e);
            return R.fail("查询客户列表异常: " + e.getMessage());
        }
    }

    /**
     * 便捷接口：获取accessToken
     */
    @GetMapping("/getAccessToken")
    public R<String> getAccessToken() {
        try {
            String accessToken = ftApiService.getAccessTokenWithTgt();
            if (accessToken != null) {
                return R.ok(accessToken, "获取accessToken成功");
            } else {
                return R.fail("获取accessToken失败");
            }
        } catch (Exception e) {
            log.error("获取accessToken异常", e);
            return R.fail("获取accessToken异常: " + e.getMessage());
        }
    }
}
