package com.moment.whynote.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Utils which can deal with data
 */
public class DataUtils {
    /**
     * @param str The String which need us to deal with
     * @return the list of uri
     */
    public List<String> getUris(String str) {
        List<String> uriList = new ArrayList<>();
        String webUrl = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
//  get a regular String and matcher it
        Pattern pattern = Pattern.compile(webUrl);
        Matcher matcher = pattern.matcher(str);
//  if we found what we need, we can add it in the list
        while (matcher.find()) {
            uriList.add(matcher.group());
        }
//  We can print its size if necessary.
//        System.out.println(uriList.size());
        return uriList;
    }

    /**
     * @param uriList The uriList which we need to deal with
     * @return A String of uri
     */
    public String getUriString(List<String> uriList) {
        StringBuilder builder = new StringBuilder();
        for (String s : uriList) {
            builder.append(s).append("\r\n");
        }
        return builder.toString();
    }

    /**
     * @return the date of this system right now
     */
    public String getNowDateDefault(long date) {
//        Date date = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat mat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        return mat.format(date);
    }


}
