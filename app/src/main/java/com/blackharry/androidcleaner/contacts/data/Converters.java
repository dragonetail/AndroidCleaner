package com.blackharry.androidcleaner.contacts.data;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {
    private static final String TAG = "Converters";
    private static final Gson gson = new GsonBuilder()
        .serializeNulls()
        .create();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    @TypeConverter
    public static String fromStringList(List<String> list) {
        try {
            if (list == null) {
                return null;
            }
            return gson.toJson(list, STRING_LIST_TYPE);
        } catch (Exception e) {
            LogUtils.logError(TAG, "序列化List<String>失败", e);
            return "[]";
        }
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        try {
            if (value == null) {
                return new ArrayList<>();
            }
            List<String> result = gson.fromJson(value, STRING_LIST_TYPE);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            LogUtils.logError(TAG, "反序列化List<String>失败", e);
            return new ArrayList<>();
        }
    }
} 