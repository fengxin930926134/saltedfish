package com.fengx.saltedfish.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class RandomUtil {
    private static final String[] CHARS = new String[]{"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    /**
     * 生成随机数
     *
     * @param max 0-max 不包括max
     * @return int
     */
    public static int randomInt(int max) {
        Random random = new Random();
        return random.nextInt(max);
    }

    /**
     * 生成随机数数组(不重复)
     *
     * @param max 0-max 不包括max
     * @param number 数量
     * @return Set<Integer>
     */
    public static Set<Integer> randomInts(int max, int number) {
        Set<Integer> values = new HashSet<>(number);
        if (number >= max) {
            for (int i = 0; i < max; i++) {
                values.add(i);
            }
        } else {
            Random random = new Random();
            for (int i = 0; i < number; i++) {
                int value = random.nextInt(max);
                values.add(value);
            }
            if (values.size() < number) {
                values.addAll(randomInts(max, number - values.size()));
            }
        }
        return values;
    }


    /**
     * 生成短UUID（8位）
     */
    public static String generateShortUuid() {
        return generateUuid(0);
    }

    /**
     * 指定位数的UUID 最少8位且偶数
     *
     * @param figure 位
     * @return UUID
     */
    public static String generateUuid(int figure) {
        // 基础最低位数
        int subLen = 8;
        if (figure < subLen) {
            figure = subLen;
        }
        if (figure % subLen > 0) {
            figure--;
        }
        StringBuilder shortBuffer = new StringBuilder();
        int num = figure / subLen;
        for (int i = 0; i < num; i++) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            for (int j = 0; j < subLen; j++) {
                String str = uuid.substring(j * 4, j * 4 + 4);
                int x = Integer.parseInt(str, 16);
                shortBuffer.append(CHARS[x % 0x3E]);
            }
        }
        return shortBuffer.toString();
    }
}