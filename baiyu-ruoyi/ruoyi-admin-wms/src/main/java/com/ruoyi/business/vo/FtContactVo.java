package com.ruoyi.business.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.business.entity.CustomerCustomize;
import com.ruoyi.business.entity.FtContact;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;

@Data
@AutoMapper(target = FtContact.class)
public class FtContactVo {
    @TableField("id")
    private Long id;
    /**
     * 富通id
     * */
    private Long fId;

    /**
     * 所属业务员名称
     * */
    private String operatorName;
    /**
     * 姓名
     * */
    private String name;
    /**
     * 性别
     * */
    private String sex;
    /**
     * 职务
     * */
    private String job;
    /**
     * 邮箱
     * */
    private String email;
    /**
     * 生日
     * */
    private String birthday;
    /**
     *电话
     * */
    private String telephone;
    /**
     * 手机
     * */
    private String mobile;
    /**
     * 传真
     * */
    private String fax;
    /**
     * qq号
     * */
    private String qq;
    /**
     * msn账号
     * */
    private String msn;
    /**
     * skype账号
     * */
    private String skype;
    /**
     *微信
     * */
    private String wechat;
    /**
     * 联系住址
     * */
    private String address;
    /**
     * Facebook个人主页
     * */
    private String faceBookPerMain;
    /**
     * 状态 1:启用; 2:停用
     * */
    private String status;
    /**
     * 备注
     * */
    private String remark;
    /**
     * Facebook个人主页
     * */
    private String facebookId;
    /**
     * Facebook账号名
     * */
    private String faceBookName;
    /**
     * twitter账号id
     * */
    private String twitterId;
    /**
     * twitter账号名
     **/
    private String twitterName;
    /**
     * 社交账号id
     * */
    private String socialId;
    /**
     * 社交账号名称
     * */
    private String socialAccount;
    /**
     * 是否默认联系人 0:不是默认; 1:是默认
     * */
    private String defaultContact;
    /**
     * twitter个人主页
     * */
    private String twitterPerMain;
    /**
     * LinkedIn
     * */
    private String linkedin;
    /**
     * whatsApp
     * */
    private String whatsApp;
    /**
     * whatsApp消息最后一次同步时间
     * */
    private String whatsAppTime;
    /**
     * 阿里旺旺账号
     * */
    private String aliwangwang;
    /**
     * 客户自定义字段信息
     * */
    private List<CustomerCustomize> customerCustomizeList;
}
