package com.xmq.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改用户个人信息请求参数")
public class UpdateUserInfoDTO {
//    头像
    private String avatar;

//    是否接收文章推送
    private Integer isReceivePush;
}
