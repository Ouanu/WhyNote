package com.moment.whynote.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.moment.whynote.data.ResData;

import java.util.List;

@Dao
public interface ResDao {

    @Query("SELECT * FROM resdata order by updateDate desc")
    LiveData<List<ResData>> getAll();

    @Query("SELECT * FROM resdata WHERE uid = :id")
    ResData getResDataByUid(int id);

    @Query("SELECT * FROM resdata WHERE updateDate = :updateDate")
    ResData getResDataByUpdateDate(long updateDate);

    @Insert
    void insertResData(ResData data);

    @Delete
    void deleteResData(ResData data);

    @Update
    void updateResData(ResData data);



}
