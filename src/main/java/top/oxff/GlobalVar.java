package top.oxff;

import burp.api.montoya.core.HighlightColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GlobalVar {
    public static Set<String> highLightPathSet;
    public static Map<String, HighlightColor> requestPathHighlightColor;

    public static Set<String> highLightSelectStringSet;
    public static Map<String, HighlightColor> selectStringHighlightColor;

    public static void init() {
        highLightPathSet = new HashSet<>();
        requestPathHighlightColor = new HashMap<>();

        highLightSelectStringSet = new HashSet<>();
        selectStringHighlightColor = new HashMap<>();
    }

    synchronized public static void clearHighLightPath() {
        highLightPathSet.clear();
        requestPathHighlightColor.clear();
    }

    synchronized public static void clearHighLightSelectString() {
        highLightSelectStringSet.clear();
        selectStringHighlightColor.clear();
    }

    synchronized public static void addHighLightPath(String path, HighlightColor color) {
        highLightPathSet.add(path);
        requestPathHighlightColor.put(path, color);
    }

    synchronized public static void removeHighLightPath(String path) {
        highLightPathSet.remove(path);
        requestPathHighlightColor.remove(path);
    }

    synchronized public static boolean isHighLightPath(String path) {
        return highLightPathSet.contains(path);
    }

    static synchronized public boolean isHighLightPathEmpty() {
        return null != highLightPathSet && highLightPathSet.isEmpty();
    }

    synchronized public static void addHighLightSelectString(String selectString, HighlightColor color) {
        highLightSelectStringSet.add(selectString);
        selectStringHighlightColor.put(selectString, color);
    }

    synchronized public static void removeHighLightSelectString(String selectString) {
        highLightSelectStringSet.remove(selectString);
        selectStringHighlightColor.remove(selectString);
    }

    synchronized public static boolean isHighLightSelectString(String bodyStr) {
        boolean flag = false;
        for (String selectString : highLightSelectStringSet) {
            if (bodyStr.contains(selectString)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    synchronized public static boolean isHighLightSelectStringEmpty() {
        return null != highLightSelectStringSet && highLightSelectStringSet.isEmpty();
    }
}
