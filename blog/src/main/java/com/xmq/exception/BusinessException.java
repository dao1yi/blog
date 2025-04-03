package com.xmq.exception;

import com.xmq.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
    
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
} 