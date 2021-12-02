package com.moment.whynote.data;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据格式
 */
@Entity
public class ResData {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name="title")
    public String title="";

    @ColumnInfo(name="desc")
    public String desc="";

    @ColumnInfo(name="uri")
    public String uri;

    @ColumnInfo(name="updateDate")
    public long updateDate = System.currentTimeMillis();

    @ColumnInfo(name="imgUrl")
    public String imgUrl = "";

}
