package cn.jun.test;

import java.util.*;

public class Test {

    @org.junit.Test
    public void test() {
        List<String> list = new ArrayList<>(4);
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        Set<String> set = new HashSet<>(4);
        set.add("s1");
        set.add("s4");
        set.add("s2");
        set.add("s3");
        String[] arr = new String[4];
        for (int i = 0; i < 4; i++) {
            arr[i] = "arr" + i;
        }

        System.out.println("---------------------");
        System.out.println(toStrArr(list));
        System.out.println(toStrArr(set));
        System.out.println(toStrArr(arr));
        System.out.println(toStrArr("1,2,3,4"));
    }

    public String[] toStrArr(Object t) {
        String[] ss;
        if (t instanceof Collection) {
            Collection<Object> c = (Collection) t;
            ss = c.stream().map(String::valueOf).toArray(String[]::new);
        } else if (t.getClass().isArray()) {
            Object[] objs = (Object[]) t;
            ss = Arrays.stream(objs).map(String::valueOf).toArray(String[]::new);
        } else {
            ss = String.valueOf(t).split(",");
        }
        return ss;
    }
}
