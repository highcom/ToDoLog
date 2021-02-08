package com.highcom.todolog.datamodel;

import androidx.room.TypeConverter;

import java.sql.Date;

public class DataConverters {
    @TypeConverter
    public static Date fromLogDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long toLogDate(Date date) {
        return date == null ? null : date.getTime();
    }
}
