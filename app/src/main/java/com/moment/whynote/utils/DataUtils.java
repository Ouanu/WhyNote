package com.moment.whynote.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Utils which can deal with data
 */
public class DataUtils {
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
//  We can print its size
//        System.out.println(uriList.size());
        return uriList;
    }


}
