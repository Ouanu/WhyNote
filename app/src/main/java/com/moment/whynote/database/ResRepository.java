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
     * @param context 获取上下文
     */
    public ResRepository(Context context) {
        Log.d(TAG, "ResRepository: ");
        mContext = context;
        if(mContext == null){
            Log.d(TAG, "ResRepository: context is null");
        }
        /*
          创建数据库
         */
        ResDatabase database = Room.databaseBuilder(mContext,
                ResDatabase.class,
                DATABASE_NAME).build();
        /*
          获取Dao
         */
        dao = database.resDao();
    }

    /**
     * 单例模式
     * 静态加载实例
     * @return 返回实例
     */
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

    /**
     * @return 获取所有数据
     */
    public LiveData<List<ResData>> getAllResData() {
        return dao.getAll();
    }

    /**
     * @param resData 插入数据
     */
    public void insertData(ResData resData){
        dao.insertResData(resData);
    }

    /**
     * @param id 列表的主键
     * @return 返回被查询的ResData
     */
    public ResData getResDataByUid(int id) {
        return dao.getResDataByUid(id);
    }

    /**
     * @param date 列表的更改时间
     * @return 返回被查询的ResData
     */
    public ResData getResDataByUpdateDate(long date) {
        return dao.getResDataByUpdateDate(date);
    }

    /**
     * @param data 删除ResData
     */
    public void deleteResData(ResData data) {
        dao.deleteResData(data);
    }

    /**
     * @param data 更新ResData
     */
    public void upResData(ResData data) {
        dao.updateResData(data);
    }

}
