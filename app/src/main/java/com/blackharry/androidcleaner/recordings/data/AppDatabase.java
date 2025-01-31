package com.blackharry.androidcleaner.recordings.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;
import com.blackharry.androidcleaner.contacts.data.ContactDao;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.contacts.data.PhoneNumberEntity;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.calls.data.CallDao;

@Database(
    entities = {
        RecordingEntity.class,
        CallEntity.class,
        ContactEntity.class,
        PhoneNumberEntity.class
    },
    version = 3
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract RecordingDao recordingDao();
    public abstract CallDao callDao();
    public abstract ContactDao contactDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "app_database"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建contacts表
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS contacts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT, " +
                "is_safe_zone INTEGER NOT NULL DEFAULT 0, " +
                "is_temp_zone INTEGER NOT NULL DEFAULT 0, " +
                "is_blacklist INTEGER NOT NULL DEFAULT 0, " +
                "is_deleted INTEGER NOT NULL DEFAULT 0, " +
                "last_updated INTEGER NOT NULL DEFAULT 0)"
            );

            // 创建phone_numbers表
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS phone_numbers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "contact_id INTEGER NOT NULL, " +
                "phone_number TEXT, " +
                "is_primary INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE)"
            );

            // 为phone_numbers表添加索引
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_phone_numbers_contact_id ON phone_numbers(contact_id)"
            );

            // 为recordings表添加contact_id列
            database.execSQL(
                "ALTER TABLE recordings ADD COLUMN contact_id INTEGER"
            );

            // 为recordings表添加外键索引
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_recordings_contact_id ON recordings(contact_id)"
            );
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建calls表
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS calls (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "phone_number TEXT, " +
                "contact_id INTEGER, " +
                "call_time INTEGER NOT NULL, " +
                "call_duration INTEGER NOT NULL, " +
                "recording_filename TEXT, " +
                "recording_filepath TEXT, " +
                "recording_filesize INTEGER, " +
                "recording_created_time INTEGER, " +
                "FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE SET NULL)"
            );

            // 为calls表添加索引
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_calls_contact_id ON calls(contact_id)"
            );

            // 修改recordings表结构
            database.execSQL("ALTER TABLE recordings RENAME TO recordings_old");
            database.execSQL(
                "CREATE TABLE recordings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "file_name TEXT, " +
                "file_path TEXT, " +
                "file_size INTEGER, " +
                "created_time INTEGER, " +
                "duration INTEGER)"
            );
            database.execSQL(
                "INSERT INTO recordings (id, file_name, file_path, file_size, created_time, duration) " +
                "SELECT id, file_name, file_path, file_size, created_time, duration " +
                "FROM recordings_old"
            );
            database.execSQL("DROP TABLE recordings_old");
        }
    };
} 