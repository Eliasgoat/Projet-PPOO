package ch.epfl.javelo.gui;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestBrouillon {
    public static void main(String[] args) {
        LinkedHashMap<Integer, String> testMap = new LinkedHashMap<>() {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > 3;
            }
        };

        testMap.put(1,"map1");
        testMap.put(2,"map2");
        testMap.put(3,"map3");
        testMap.put(4,"map4");
        System.out.println(testMap.size());
        System.out.println(testMap);
    }
}
