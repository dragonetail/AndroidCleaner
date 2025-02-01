package com.blackharry.androidcleaner;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import java.io.File;
import java.text.DecimalFormat;

import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.recordings.data.RecordingDao;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.calls.data.CallDao;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.contacts.data.ContactDao;
import com.blackharry.androidcleaner.contacts.data.Converters;

/**
 * 应用数据库类
 * 使用Room持久化库管理SQLite数据库
 * 包含以下主要功能：
 * 1. 数据库的创建和版本管理
 * 2. 数据访问对象（DAO）的提供
 * 3. 数据库迁移策略的实现
 * 4. 单例模式确保数据库实例的唯一性
 * 5. 数据库大小监控和统计
 *
 * @author BlackHarry
 * @version 1.0
 * @since 2024-01-31
 */
@Database(entities = {RecordingEntity.class, CallEntity.class, ContactEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "android_cleaner.db";
    private static final long WARNING_SIZE_MB = 50; // 数据库大小警告阈值（MB）
    private static final long MAX_SIZE_MB = 100; // 数据库大小最大阈值（MB）
    
    private Context context;
    
    /**
     * 获取录音数据访问对象
     * @return RecordingDao实例
     */
    public abstract RecordingDao recordingDao();

    /**
     * 获取通话记录数据访问对象
     * @return CallDao实例
     */
    public abstract CallDao callDao();

    /**
     * 获取联系人数据访问对象
     * @return ContactDao实例
     */
    public abstract ContactDao contactDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * 获取数据库实例
     * 使用双重检查锁定模式实现单例
     * @param context 应用上下文
     * @return AppDatabase实例
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, 
                            DATABASE_NAME)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    db.execSQL("PRAGMA foreign_keys = ON");
                                    LogUtils.i(TAG, "数据库创建成功，已启用外键约束");
                                }

                                @Override
                                public void onOpen(SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    db.execSQL("PRAGMA foreign_keys = ON");
                                    LogUtils.i(TAG, "数据库打开成功，已启用外键约束");
                                    // 检查数据库大小
                                    ((AppDatabase)INSTANCE).checkDatabaseSize();
                                }
                            })
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .setJournalMode(JournalMode.TRUNCATE)
                            .fallbackToDestructiveMigration()
                            .build();
                    ((AppDatabase)INSTANCE).context = context.getApplicationContext();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 数据库迁移策略（1->2版本）
     * 添加新的字段以支持更多功能：
     * - 联系人表：添加最后通话时间和总通话时长
     * - 通话记录表：添加通话类型和重要标记
     * - 录音表：添加备份状态和备份时间
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            LogUtils.i(TAG, "开始数据库迁移 1->2");
            
            // 为联系人表添加新字段
            database.execSQL("ALTER TABLE contacts ADD COLUMN last_call_time INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE contacts ADD COLUMN total_call_duration INTEGER DEFAULT 0");
            
            // 为通话记录表添加新字段
            database.execSQL("ALTER TABLE calls ADD COLUMN call_type INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE calls ADD COLUMN is_important INTEGER DEFAULT 0");
            
            // 为录音表添加新字段
            database.execSQL("ALTER TABLE recordings ADD COLUMN is_backed_up INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE recordings ADD COLUMN backup_time INTEGER DEFAULT 0");
            
            LogUtils.i(TAG, "数据库迁移完成 1->2");
        }
    };

    /**
     * 数据库迁移策略（2->3版本）
     * 重构联系人表结构：
     * - 添加备注字段
     * - 优化表结构
     * 使用临时表方式确保数据安全迁移
     */
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            LogUtils.i(TAG, "开始数据库迁移 2->3");
            
            // 创建临时表
            database.execSQL("CREATE TABLE IF NOT EXISTS contacts_temp ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "name TEXT NOT NULL,"
                    + "phone_numbers TEXT NOT NULL,"
                    + "is_safe_zone INTEGER NOT NULL DEFAULT 0,"
                    + "is_temp_zone INTEGER NOT NULL DEFAULT 0,"
                    + "is_blacklist INTEGER NOT NULL DEFAULT 0,"
                    + "is_deleted INTEGER NOT NULL DEFAULT 0,"
                    + "created_at INTEGER NOT NULL,"
                    + "updated_at INTEGER NOT NULL,"
                    + "last_call_time INTEGER DEFAULT 0,"
                    + "total_call_duration INTEGER DEFAULT 0,"
                    + "notes TEXT"
                    + ")");
            
            // 复制数据
            database.execSQL("INSERT INTO contacts_temp SELECT "
                    + "id, name, phone_numbers, is_safe_zone, is_temp_zone, "
                    + "is_blacklist, is_deleted, created_at, updated_at, "
                    + "last_call_time, total_call_duration, '' as notes "
                    + "FROM contacts");
            
            // 删除旧表
            database.execSQL("DROP TABLE contacts");
            
            // 重命名新表
            database.execSQL("ALTER TABLE contacts_temp RENAME TO contacts");
            
            LogUtils.i(TAG, "数据库迁移完成 2->3");
        }
    };

    /**
     * 销毁数据库实例
     * 在应用退出或需要重置数据库时调用
     */
    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
        LogUtils.i(TAG, "数据库实例已销毁");
    }

    /**
     * 获取数据库文件大小
     * @return 数据库文件大小（字节）
     */
    public long getDatabaseSize() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.length();
    }

    /**
     * 获取格式化的数据库大小
     * @return 格式化后的数据库大小字符串
     */
    public String getFormattedDatabaseSize() {
        long size = getDatabaseSize();
        DecimalFormat df = new DecimalFormat("#.##");
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return df.format(size / 1024.0) + " KB";
        } else {
            return df.format(size / (1024.0 * 1024.0)) + " MB";
        }
    }

    /**
     * 获取数据库统计信息
     * @return 数据库统计信息字符串
     */
    public String getDatabaseStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("数据库统计信息：\n");
        
        try {
            SupportSQLiteDatabase db = getOpenHelper().getWritableDatabase();
            
            // 获取各表的记录数
            String[] tables = {"recordings", "calls", "contacts"};
            for (String table : tables) {
                try (Cursor cursor = db.query("SELECT COUNT(*) FROM " + table)) {
                    if (cursor.moveToFirst()) {
                        stats.append(table).append("表记录数：")
                             .append(cursor.getLong(0)).append("\n");
                    }
                }
            }
            
            // 添加数据库大小信息
            stats.append("数据库大小：").append(getFormattedDatabaseSize());
            
        } catch (Exception e) {
            LogUtils.e(TAG, "获取数据库统计信息失败", e);
            stats.append("获取统计信息失败：").append(e.getMessage());
        }
        
        return stats.toString();
    }

    /**
     * 检查数据库大小
     * 当数据库大小超过警告阈值时，记录警告日志
     * 当数据库大小超过最大阈值时，记录错误日志
     */
    private void checkDatabaseSize() {
        long sizeInMB = getDatabaseSize() / (1024 * 1024);
        String formattedSize = getFormattedDatabaseSize();
        
        if (sizeInMB >= MAX_SIZE_MB) {
            LogUtils.e(TAG, "数据库大小(" + formattedSize + ")已超过最大限制" + MAX_SIZE_MB + "MB");
            // TODO: 实现数据库清理策略
        } else if (sizeInMB >= WARNING_SIZE_MB) {
            LogUtils.w(TAG, "数据库大小(" + formattedSize + ")已超过警告阈值" + WARNING_SIZE_MB + "MB");
        } else {
            LogUtils.i(TAG, "当前数据库大小：" + formattedSize);
        }
    }
}