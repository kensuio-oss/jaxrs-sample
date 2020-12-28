package io.kensu.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    public static <T> List<T> orEmptyList(List<T> list) {
        return (list == null) ? new ArrayList<>() : list;
    }
}
