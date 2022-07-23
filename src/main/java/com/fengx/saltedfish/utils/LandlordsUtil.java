package com.fengx.saltedfish.utils;

import com.google.common.collect.Lists;

import java.util.*;

/**
 * 斗地主工具类
 */
public class LandlordsUtil {

    /**
     * 其余数字按正常排序
     */
    private static final List<String> SORT = Lists.newArrayList(
            "10", "J", "Q", "K", "A", "2", "king", "KING"
    );

    /**
     * 牌组排序
     */
    public static Comparator<String> comparator = (o1, o2) -> {
        if (o1.equals(o2)) {
            return 0;
        }
        if (!SORT.contains(o1) && !SORT.contains(o2)) {
            return o1.compareTo(o2);
        } else if (SORT.contains(o1) && SORT.contains(o2)) {
            return SORT.indexOf(o1) - SORT.indexOf(o2);
        } else if (SORT.contains(o1)) {
            return 1;
        } else {
            return -1;
        }
    };

    /**
     * 发牌
     */
    public static List<List<String>> dealCards() {
        // 1.准备牌
        ArrayList<String> poker = new ArrayList<>(54);
        String[] colors = {"♥", "♠", "♦", "♣"};
        String[] numbers = {"A", "J", "Q", "K", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        // 存储大小王
        poker.add("KING");
        poker.add("king");
        //循环遍历两个数组，组装52张牌
        for (String color : colors) {
            for (String number : numbers) {
                // 无花色
//                poker.add(color + number);
                poker.add(number + "");
            }
        }
        /*
         *  2.洗牌
         *  使用集合的工具类Collections中的方法
         *  static void shuffle(List<?> list) 使用默认随机源对指定列表进行置换
         */
        Collections.shuffle(poker);
        /*
         *  3.发牌
         */
        //定义4个集合，存储玩家的牌和底牌
        ArrayList<String> player01 = new ArrayList<>(17);
        ArrayList<String> player02 = new ArrayList<>(17);
        ArrayList<String> player03 = new ArrayList<>(17);
        ArrayList<String> dipai = new ArrayList<>(3);

        /*
            遍历poker集合，获取每一张牌
            使用poker集合的索引%3给3个玩家轮流发牌
            剩余3张牌给底牌
            注意： 先判断底牌（i >=51）
        */
        for (int i = 0; i < poker.size(); i++) {
            String p = poker.get(i);
            if (i >= 51) {
                dipai.add(p);
            } else if (i % 3 == 0) {
                player01.add(p);
            } else if (i % 3 == 1) {
                player02.add(p);
            } else {
                player03.add(p);
            }
        }

        // 返回
        List<List<String>> pai = new ArrayList<>(4);
        player01.sort(comparator);
        player02.sort(comparator);
        player03.sort(comparator);
        dipai.sort(comparator);
        pai.add(player01);
        pai.add(player02);
        pai.add(player03);
        pai.add(dipai);
        return pai;
    }

    public static void main(String[] args) {
        dealCards().forEach(System.out::println);
    }
}
