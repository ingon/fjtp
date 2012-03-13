package org.fjtp.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String[] split(String s, char c){
        List<String> res = new ArrayList<String>();
        int index = -1;
        int len = s.length();
        while(true) {
            int end = s.indexOf(c, ++index);
            boolean br = end < 0;
            
            if(br)
                end = len;
            if(index != end)
                res.add(s.substring(index, index = end));
            if(br || end == len)
                break;
        }
        
        return res.toArray(new String[res.size()]);
    }
}
