package com.fengx.saltedfish.utils;

import com.fengx.saltedfish.model.enums.LandlordsHandCardsTypeEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 斗地主工具类
 */
public class LandlordsUtil {

    /**
     * 正常排序
     */
    private static final List<String> SORT = Lists.newArrayList(
            "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2", "king", "KING"
    );

    private static final List<String> SPECIAL_BRAND = Lists.newArrayList("2", "king", "KING");

    public static void main(String[] args) {
        System.out.println(validationRules(Lists.newArrayList("3", "4", "5", "6", "7", "8"),
                Lists.newArrayList("3", "4", "5", "6", "7", "8")
                ).getDesc());
    }

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

    /**
     * 欢乐斗地主规则
     * <p>
     * 火箭 即双王（大王和小王），最大的牌
     * 炸弹 四张同数值牌（如：四个 7）
     * 单牌 单个牌（如：红桃 5）
     * 对牌 数值相同的两张牌（如：梅花 4+ 方块 4 ）
     * 三张牌 数值相同的三张牌（如：三个 J ）
     * 三带一 数值相同的三张牌 + 一张单牌或一对牌。（如： 333+6 或 444+99）
     * <p>
     * 单顺 五张或更多的连续单牌（如： 45678 或 78910JQK ）。不包括 2 点和双王
     * 双顺 三对或更多的连续对牌（如： 334455 、 7788991010JJ ）。不包括 2 点和双王
     * 三顺 二个或更多的连续三张牌（如： 333444 、 555666777888 ）。不包括 2 点和双王
     * <p>
     * 飞机带翅膀 三顺+同数量的单牌（或同数量的对牌）（如： 444555+79 或 333444555+7799JJ）
     * 四带二 四张牌+两手牌。（注意：四带二不是炸弹）（如： 5555 + 3 + 8 或 4444 + 55 + 77 ） -》不要了
     *
     * @param paizu                  牌组
     * @param currentAlreadyOutCards 需要对比的牌组，为空则不进行对比合乎规则即可，如果不为空则必须大于用作对比的牌才返回true
     * @return LandlordsHandCardsTypeEnum
     */
    public static LandlordsHandCardsTypeEnum validationRules(List<String> paizu, List<String> currentAlreadyOutCards) {
        LandlordsHandCardsTypeEnum type = getCardsType(paizu);
        if (CollectionUtils.isNotEmpty(currentAlreadyOutCards)) {
            LandlordsHandCardsTypeEnum cardsType = getCardsType(currentAlreadyOutCards);
            if (cardsType.equals(type)) {
                if (paizu.size() != currentAlreadyOutCards.size()) {
                    return LandlordsHandCardsTypeEnum.ERROR;
                }
                switch (type) {
                    case THREE_WITH_ONE:
                    case THREE_BELT_TWO:
                        Map<String, List<String>> collect = paizu.stream().collect(Collectors.groupingBy(String::toString));
                        List<List<String>> lists = collect.values().stream().filter(e -> e.size() == 3).collect(Collectors.toList());
                        int sum1 = getCardsWeightList(lists.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                                .stream().mapToInt(Integer::intValue).sum();
                        Map<String, List<String>> collect1 = currentAlreadyOutCards.stream().collect(Collectors.groupingBy(String::toString));
                        List<List<String>> lists1 = collect1.values().stream().filter(e -> e.size() == 3).collect(Collectors.toList());
                        int sum3 = getCardsWeightList(lists1.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                                .stream().mapToInt(Integer::intValue).sum();
                        if (sum1 <= sum3) {
                            type = LandlordsHandCardsTypeEnum.ERROR;
                        }
                        break;
                    case AIRCRAFT:
                        Map<Integer, List<String>> collect3 = paizu.stream().collect(Collectors.groupingBy(SORT::indexOf));
                        List<Integer> sorts = Lists.newArrayList();
                        Map<Integer, List<String>> collect4 = currentAlreadyOutCards.stream().collect(Collectors.groupingBy(SORT::indexOf));
                        List<Integer> sorts2 = Lists.newArrayList();
                        collect3.forEach((i, values) -> {
                            if (values.size() >= 3) {
                                sorts.add(i);
                            }
                        });
                        collect4.forEach((i, values) -> {
                            if (values.size() >= 3) {
                                sorts2.add(i);
                            }
                        });
                        int sum4 = sorts.stream().mapToInt(Integer::intValue).sum();
                        int sum5 = sorts2.stream().mapToInt(Integer::intValue).sum();
                        if (sum4 <= sum5) {
                            type = LandlordsHandCardsTypeEnum.ERROR;
                        }
                        break;
                    default: {
                        int sum = getCardsWeightList(paizu).stream().mapToInt(Integer::intValue).sum();
                        int sum2 = getCardsWeightList(currentAlreadyOutCards).stream().mapToInt(Integer::intValue).sum();
                        if (sum <= sum2) {
                            type = LandlordsHandCardsTypeEnum.ERROR;
                        }
                    }
                }
            } else {
                if (!type.equals(LandlordsHandCardsTypeEnum.BOMB) && !type.equals(LandlordsHandCardsTypeEnum.KING_BOMB)) {
                    type = LandlordsHandCardsTypeEnum.ERROR;
                } else {
                    if (cardsType.equals(LandlordsHandCardsTypeEnum.KING_BOMB)) {
                        type = LandlordsHandCardsTypeEnum.ERROR;
                    }
                }
            }
        }
        return type;
    }

    private static LandlordsHandCardsTypeEnum getCardsType(List<String> cards) {
        LandlordsHandCardsTypeEnum typeEnum = LandlordsHandCardsTypeEnum.ERROR;
        switch (cards.size()) {
            case 1:
                typeEnum = LandlordsHandCardsTypeEnum.SINGLE;
                break;
            case 2:
                if (cards.get(0).equals(cards.get(1))) {
                    typeEnum = LandlordsHandCardsTypeEnum.PAIR;
                } else if (cards.contains("king") && cards.contains("KING")) {
                    typeEnum = LandlordsHandCardsTypeEnum.KING_BOMB;
                }
                break;
            case 3:
                if (cards.get(0).equals(cards.get(1)) && cards.get(0).equals(cards.get(2))) {
                    typeEnum = LandlordsHandCardsTypeEnum.THREE_CARDS;
                }
                break;
            case 4: {
                HashSet<String> hashSet = Sets.newHashSet(cards);
                if (hashSet.size() == 2) {
                    Map<String, List<String>> collect = cards.stream().collect(Collectors.groupingBy(String::toString));
                    if (collect.values().stream().anyMatch(e -> e.size() == 3)) {
                        typeEnum = LandlordsHandCardsTypeEnum.THREE_WITH_ONE;
                    }
                } else if (hashSet.size() == 1) {
                    typeEnum = LandlordsHandCardsTypeEnum.BOMB;
                }
            }
            break;
            case 5: {
                HashSet<String> hashSet = Sets.newHashSet(cards);
                if (hashSet.size() == 2) {
                    Map<String, List<String>> collect = cards.stream().collect(Collectors.groupingBy(String::toString));
                    if (collect.values().stream().anyMatch(e -> e.size() == 3)) {
                        typeEnum = LandlordsHandCardsTypeEnum.THREE_BELT_TWO;
                    }
                } else if (isStraight(cards)) {
                    typeEnum = LandlordsHandCardsTypeEnum.STRAIGHT;
                }
            }
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12: {
                if (isStraight(cards)) {
                    typeEnum = LandlordsHandCardsTypeEnum.STRAIGHT;
                } else if (isCouple(cards, 2)) {
                    typeEnum = LandlordsHandCardsTypeEnum.COUPLE;
                } else if (isCouple(cards, 3)) {
                    typeEnum = LandlordsHandCardsTypeEnum.SANSHUN;
                } else if (isAircraft(cards)) {
                    typeEnum = LandlordsHandCardsTypeEnum.AIRCRAFT;
                }
            }
            break;
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20: {
                if (isCouple(cards, 2)) {
                    typeEnum = LandlordsHandCardsTypeEnum.COUPLE;
                } else if (isCouple(cards, 3)) {
                    typeEnum = LandlordsHandCardsTypeEnum.SANSHUN;
                } else if (isAircraft(cards)) {
                    typeEnum = LandlordsHandCardsTypeEnum.AIRCRAFT;
                }
            }
            break;
            default:
        }
        return typeEnum;
    }

    private static Boolean exist2OrKing(List<String> cards) {
        AtomicBoolean is = new AtomicBoolean(false);
        SPECIAL_BRAND.forEach(item -> {
            if (cards.contains(item)) {
                is.set(true);
            }
        });
        return is.get();
    }

    /**
     * 飞机？
     */
    private static Boolean isAircraft(List<String> cards) {
        if (cards.size() < 8) {
            return false;
        }
        Map<Integer, List<String>> collect = cards.stream().collect(Collectors.groupingBy(SORT::indexOf));
        List<Integer> sorts = Lists.newArrayList();
        List<String> other = Lists.newArrayList();
        collect.forEach((i, values) -> {
            if (values.size() >= 3) {
                sorts.add(i);
                if (values.size() > 3) {
                    for (int j = 3; j < values.size(); j++) {
                        other.add(values.get(j));
                    }
                }
            } else {
                other.addAll(values);
            }
        });
        sorts.sort(Comparator.comparingInt(o -> o));
        for (int i = 0; i < sorts.size() - 1; i++) {
            if (!sorts.get(i).equals(sorts.get(i + 1) - 1)) {
                return false;
            }
        }
        List<Integer> cardsWeightList = getCardsWeightList(other);
        HashSet<Integer> hashSet = Sets.newHashSet(cardsWeightList);
        if (hashSet.size() > sorts.size()) {
            return false;
        }
        if (hashSet.size() == sorts.size()) {
            // 判断是否是对子
            Map<Integer, List<Integer>> collect1 = cardsWeightList.stream().collect(Collectors.groupingBy(Integer::intValue));
            return collect1.values().stream().anyMatch(e -> e.size() == 2);
        }
        return true;
    }

    /**
     * 是否是双顺/连对, 三顺
     */
    private static Boolean isCouple(List<String> cards, Integer number) {
        if (exist2OrKing(cards) || cards.size() < 6) {
            return false;
        }
        List<Integer> cardsWeightList = getCardsWeightList(cards);
        Map<Integer, List<Integer>> collect = cardsWeightList.stream().collect(Collectors.groupingBy(Integer::intValue));
        cardsWeightList = collect.keySet().stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < cardsWeightList.size() - 1; i++) {
            if (!cardsWeightList.get(i).equals(cardsWeightList.get(i + 1) - 1)) {
                return false;
            }
        }
        return collect.values().stream().noneMatch(e -> e.size() != number);
    }

    /**
     * 是否是顺子
     */
    private static Boolean isStraight(List<String> cards) {
        HashSet<String> strings = Sets.newHashSet(cards);
        if (exist2OrKing(cards) || strings.size() < 5 || strings.size() != cards.size()) {
            return false;
        }
        List<Integer> cardsWeightList = getCardsWeightList(cards);
        boolean is = true;
        for (int i = 0; i < cardsWeightList.size() - 1; i++) {
            if (!cardsWeightList.get(i).equals(cardsWeightList.get(i + 1) - 1)) {
                is = false;
                break;
            }
        }
        return is;
    }

    private static List<Integer> getCardsWeightList(List<String> cards) {
        return cards.stream().map(SORT::indexOf).sorted().collect(Collectors.toList());
    }

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
}
