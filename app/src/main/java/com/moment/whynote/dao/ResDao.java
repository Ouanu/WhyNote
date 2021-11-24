package com.moment.whynote.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.moment.whynote.data.ResData;

import java.util.List;

@Dao
public interface ResDao {

    @Query("SELECT * FROM resdata")
    LiveData<List<ResData>> getAll();

    @Insert
    void insertResData(ResData data);

}
