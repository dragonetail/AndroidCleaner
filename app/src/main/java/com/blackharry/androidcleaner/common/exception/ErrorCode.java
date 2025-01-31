package com.blackharry.androidcleaner.common.exception;

public enum ErrorCode {
    // 系统错误 (1000-1999)
    SYSTEM_ERROR(1000, "系统错误"),
    NETWORK_ERROR(1001, "网络错误"),
    DATABASE_ERROR(1002, "数据库错误"),
    FILE_ERROR(1003, "文件操作错误"),
    
    // 业务错误 (2000-2999)
    INVALID_PARAMETER(2000, "无效的参数"),
    RESOURCE_NOT_FOUND(2001, "资源未找到"),
    OPERATION_FAILED(2002, "操作失败"),
    
    // 录音相关错误 (3000-3999)
    RECORDING_NOT_FOUND(3000, "录音文件未找到"),
    RECORDING_DELETE_FAILED(3001, "录音删除失败"),
    RECORDING_ACCESS_DENIED(3002, "无法访问录音文件"),
    
    // 通话记录相关错误 (4000-4999)
    CALL_LOG_ACCESS_DENIED(4000, "无法访问通话记录"),
    CALL_LOG_NOT_FOUND(4001, "通话记录未找到"),
    
    // 联系人相关错误 (5000-5999)
    CONTACT_ACCESS_DENIED(5000, "无法访问联系人"),
    CONTACT_NOT_FOUND(5001, "联系人未找到"),
    CONTACT_OPERATION_FAILED(5002, "联系人操作失败");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
} 