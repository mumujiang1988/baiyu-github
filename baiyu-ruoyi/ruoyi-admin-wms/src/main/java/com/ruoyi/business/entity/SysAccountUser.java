package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigInteger;
import java.util.Date;


@Data
public class SysAccountUser  {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private BigInteger id;

    /**
     * 部门ID
     */

    private BigInteger deptId;

    /**
     * 租户ID
     */
    private BigInteger tenantId;

    /**
     * 登录账号
     */
    private String loginName;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 账户类型
     */
    @TableField(value = "account_type")
    private Integer accountType;

    /**
     * 昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 手机电话
     */
    @TableField(value = "mobile")
    private String mobile;

    /**
     * 邮件
     */
    @TableField(value = "email")
    private String email;

    /**
     * 账户头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 金蝶对应用户编码*/
    @TableField(value = "k3_key")
    private String k3Key;

    /**
     * 金蝶对应员工编码*/
    @TableField(value = "staff_id")
    private String staffId;
    /**
     * 数据权限类型
     */
    @TableField(value = "data_scope")
    private Integer dataScope;

    /**
     * 自定义部门权限
     */
    @TableField(value = "dept_id_list")
    private String deptIdList;

    /**
     * 数据状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created")
    private Date created;

    /**
     * 创建者
     */
    @TableField(value = "created_by")
    private BigInteger createdBy;

    /**
     * 修改时间
     */
    @TableField(value = "modified")
    private Date modified;

    /**
     * 修改者
     */
    @TableField(value = "modified_by")
    private BigInteger modifiedBy;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 删除标识
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;



}
