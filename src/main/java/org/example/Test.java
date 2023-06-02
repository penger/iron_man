package org.example;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("1",null);
        map.put("2","happy");
        map.put("3","gongpeng");

        System.out.println(map.size());

        Object remove = map.remove("2");
        System.out.println(remove);

        System.out.println(map.size());

    }
}
