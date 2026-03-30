package com.ruoyi.business.entity;

import lombok.Data;

@Data
public  class ButtonInfo {
    public String text; // 按钮显示文本
    public String url;  // 点击打开地址
    public String type; // 按钮类型 primary / default / danger

    public ButtonInfo(String text, String url, String type) {
        this.text = text;
        this.url = url;
        this.type = type;
    }
}
