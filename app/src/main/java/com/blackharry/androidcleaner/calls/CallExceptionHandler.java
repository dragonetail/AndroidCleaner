package com.blackharry.androidcleaner.calls;

import com.blackharry.androidcleaner.common.utils.LogUtils;
import android.database.Cursor;

public class CallExceptionHandler {
    private static final String TAG = "CallExceptionHandler";

    public static class CallException extends Exception {
        private final ErrorType type;

        public CallException(ErrorType type, String message) {
            super(message);
            this.type = type;
        }

        public CallException(ErrorType type, String message, Throwable cause) {
            super(message, cause);
            this.type = type;
        }

        public ErrorType getType() {
            return type;
        }
    }

    public enum ErrorType {
        PERMISSION_DENIED("权限被拒绝"),
        DATABASE_ERROR("数据库错误"),
        CONTENT_PROVIDER_ERROR("内容提供者错误"),
        INVALID_DATA("无效数据"),
        SYNC_ERROR("同步错误"),
        UNKNOWN_ERROR("未知错误");

        private final String description;

        ErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static void validateCallLogCursor(Cursor cursor) throws CallException {
        if (cursor == null) {
            LogUtils.e(TAG, "通话记录游标为空");
            throw new CallException(ErrorType.CONTENT_PROVIDER_ERROR, 
                "无法访问通话记录");
        }
    }

    public static void handleDatabaseError(Exception e) throws CallException {
        LogUtils.e(TAG, "数据库操作失败", e);
        throw new CallException(ErrorType.DATABASE_ERROR, 
            "数据库操作失败: " + e.getMessage(), e);
    }

    public static void handlePermissionError(String permission) throws CallException {
        LogUtils.e(TAG, String.format("缺少权限: %s", permission));
        throw new CallException(ErrorType.PERMISSION_DENIED, 
            "没有必要的权限: " + permission);
    }

    public static void handleSyncError(Exception e) throws CallException {
        LogUtils.e(TAG, "同步通话记录失败", e);
        throw new CallException(ErrorType.SYNC_ERROR, 
            "同步通话记录失败: " + e.getMessage(), e);
    }

    public static String getErrorMessage(Exception e) {
        if (e instanceof CallException) {
            CallException ce = (CallException) e;
            return String.format("%s: %s", ce.getType().getDescription(), e.getMessage());
        }
        return e.getMessage();
    }
} 