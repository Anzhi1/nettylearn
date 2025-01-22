package collection;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


public class PriorityQueueExample {
    public static void main(String[] args){
    }
    //优先级队列-最大堆实现前K个高频元素
    public int[] topKFrequent(int[] nums, int k){
        Map<Integer,Integer> map = new HashMap<>();
        for(int i = 0;i<nums.length;i++){
            map.put(nums[i],map.getOrDefault(nums[i],0)+1);
        }
        PriorityQueue<int[]> queue = new PriorityQueue<>((pair1,pair2)->(pair2[1]-pair1[1]));
        for(var entry:map.entrySet()){
            queue.add(new int[]{entry.getKey(), entry.getValue()});
        }
        int[] res = new int[k];
        for(int i = 0;i<k;i++){
            res[i]=queue.poll()[0];
        }
        return res;
    }

    //优先级队列-最小堆实现前K个高频元素
    public int[] topKFrequent2(int[] nums, int k){
        return null;
    }
}
