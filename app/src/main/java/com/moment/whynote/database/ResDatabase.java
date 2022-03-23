package com.moment.whynote.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.moment.whynote.dao.ResDao;
import com.moment.whynote.data.ResData;

@Database(entities = {ResData.class}, version = 2, exportSchema = false)
public abstract class ResDatabase extends RoomDatabase {
    public abstract ResDao resDao();
}
