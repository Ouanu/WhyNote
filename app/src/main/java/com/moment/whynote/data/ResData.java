package com.moment.whynote.data;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * 数据格式
 */
@Entity
public class ResData implements Serializable{

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "title")
    public String title = "";

    @ColumnInfo(name = "desc")
    public String desc = "";

    @ColumnInfo(name = "uri")
    public String uri;

    @ColumnInfo(name = "updateDate")
    public long updateDate = System.currentTimeMillis();

    @ColumnInfo(name = "dirPath")
    public String dirPath= "";

    @ColumnInfo(name = "dirName")
    public String dirName= "";

}
