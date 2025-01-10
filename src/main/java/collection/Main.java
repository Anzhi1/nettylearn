package collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main (String[] args){
        List<Integer> integers = new ArrayList<>();
        for(int i = 0;i<10;i++){
            integers.add(i);
        }
        for(Integer j:integers){
            j++;
        }
        System.out.println("test");


        Map<Integer,Integer> map = new HashMap<>();
        map.put(1,2);
        map.put(3,4);


    }
}
