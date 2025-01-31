package com.blackharry.androidcleaner.contacts;

import com.blackharry.androidcleaner.common.utils.LogUtils;
import android.database.Cursor;

public class ContactExceptionHandler {
    private static final String TAG = "ContactExceptionHandler";

    public static class ContactException extends Exception {
        private final ErrorType type;

        public ContactException(ErrorType type, String message) {
            super(message);
            this.type = type;
        }

        public ContactException(ErrorType type, String message, Throwable cause) {
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

    public static void validateContactCursor(Cursor cursor) throws ContactException {
        if (cursor == null) {
            LogUtils.e(TAG, "联系人游标为空");
            throw new ContactException(ErrorType.CONTENT_PROVIDER_ERROR, 
                "无法访问联系人");
        }
    }

    public static void handleDatabaseError(Exception e) throws ContactException {
        LogUtils.e(TAG, "数据库操作失败", e);
        throw new ContactException(ErrorType.DATABASE_ERROR, 
            "数据库操作失败: " + e.getMessage(), e);
    }

    public static void handlePermissionError(String permission) throws ContactException {
        LogUtils.e(TAG, String.format("缺少权限: %s", permission));
        throw new ContactException(ErrorType.PERMISSION_DENIED, 
            "没有必要的权限: " + permission);
    }

    public static void handleSyncError(Exception e) throws ContactException {
        LogUtils.e(TAG, "同步联系人失败", e);
        throw new ContactException(ErrorType.SYNC_ERROR, 
            "同步联系人失败: " + e.getMessage(), e);
    }

    public static String getErrorMessage(Exception e) {
        if (e instanceof ContactException) {
            ContactException ce = (ContactException) e;
            return String.format("%s: %s", ce.getType().getDescription(), e.getMessage());
        }
        return e.getMessage();
    }
} 