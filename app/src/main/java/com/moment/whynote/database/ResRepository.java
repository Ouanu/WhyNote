package com.moment.whynote.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Room;


import com.moment.whynote.dao.ResDao;
import com.moment.whynote.data.ResData;

import java.util.List;

public class ResRepository {
    @SuppressLint("StaticFieldLeak")
    private volatile static ResRepository instance = null;
    private final static String TAG = "ResRepository";
    private final static String DATABASE_NAME = "RES_DATABASE.db";
    private final ResDao dao;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;

    /**
     * 构造函数
     * @param context
     */
    public ResRepository(Context context) {
        Log.d(TAG, "ResRepository: ");
        mContext = context;
        if(mContext == null){
            Log.d(TAG, "ResRepository: context is null");
        }
        ResDatabase database = Room.databaseBuilder(mContext,
                ResDatabase.class,
                DATABASE_NAME).build();
        dao = database.resDao();
    }

    public static ResRepository getInstance(){
        if(instance == null) {
            synchronized (ResRepository.class) {
                if(instance == null) {
                    instance = new ResRepository(mContext);
                }
            }
        }
        return instance;
    }

    public LiveData<List<ResData>> getAllResData() {
        return dao.getAll();
    }

    public void insertData(ResData resData){
        dao.insertResData(resData);
    }

}
