package com.ruoyi.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 联系人
 * */
@Data
public class Contact {
    private String id;
    private String operatorName;
    private String name;
    private String sex;
    private String job;
    private String email;
    private String birthday;
    private String telephone;
    private String mobile;
    private String fax;
    private String qq;
    private String msn;
    private String skype;
    private String wechat;
    private String address;
    private String faceBookPerMain;
    private String status;
    private String remark;
    private String facebookId;
    private String facebookName;
    private String twitterId;
    private String twitterName;
    private String socialId;
    private String socialAccount;
    private String defaultContact;
    private String twitterPerMain;
    private String linkedin;
    private String whatsApp;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime whatsAppTime;
    private String aliwangwang;
    private List<CustomerCustomize> customerCustomizeList;

}
