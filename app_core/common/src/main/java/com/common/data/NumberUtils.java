package com.common.data;


import java.util.Random;
import java.util.Stack;


/**
 * Created by Arisono on 2016/6/2.
 */
public class NumberUtils {

    private static final String[] CHINESE_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    private static final ChineseUnit[] CHINESE_UNIT = {ChineseUnit.zero, ChineseUnit.ten
            , ChineseUnit.hundred, ChineseUnit.thousand, ChineseUnit.ten_thousand,
            ChineseUnit.billion, ChineseUnit.million, ChineseUnit.ten_million, ChineseUnit.hundred_mullion};

    private static final String[] CHINESE_NUMBERS_2 = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};


    /**
     * 这是典型的随机洗牌算法。
     * 流程是从备选数组中选择一个放入目标数组中，将选取的数组从备选数组移除（放至最后，并缩小选择区域）
     * 算法时间复杂度O(n)
     *
     * @return 随机8为不重复数组
     */
    public static String generateNumber() {
        String no = "";
        //初始化备选数组
        int[] defaultNums = new int[10];
        for (int i = 0; i < defaultNums.length; i++) {
            defaultNums[i] = i;
        }

        Random random = new Random();
        int[] nums = new int[LENGTH];
        //默认数组中可以选择的部分长度
        int canBeUsed = 10;
        //填充目标数组
        for (int i = 0; i < nums.length; i++) {
            //将随机选取的数字存入目标数组
            int index = random.nextInt(canBeUsed);
            nums[i] = defaultNums[index];
            //将已用过的数字扔到备选数组最后，并减小可选区域
            swap(index, canBeUsed - 1, defaultNums);
            canBeUsed--;
        }
        if (nums.length > 0) {
            for (int i = 0; i < nums.length; i++) {
                no += nums[i];
            }
        }

        return no;
    }

    private static final int LENGTH = 8;

    private static void swap(int i, int j, int[] nums) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static String generateNumber2() {
        String no = "";
        int num[] = new int[8];
        int c = 0;
        for (int i = 0; i < 8; i++) {
            num[i] = new Random().nextInt(10) + 1;
            c = num[i];
            for (int j = 0; j < i; j++) {
                if (num[j] == c) {
                    i--;
                    break;
                }
            }
        }
        if (num.length > 0) {
            for (int i = 0; i < num.length; i++) {
                no += num[i];
            }
        }
        return no;
    }

    /**
     * @param number
     * @return
     */
    public static String translateNumber2Chinese(int number) {
        String s = String.valueOf(number);
        if (number <= 10) {
            return CHINESE_NUMBERS[number];
        }
        Stack<NumberUnit> stack = new Stack<>();
        int index = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            NumberUnit numberUnit = new NumberUnit();
            numberUnit.chineseNumber = CHINESE_NUMBERS_2[Integer.parseInt(String.valueOf(s.charAt(i)))];
            numberUnit.chineseUnit = CHINESE_UNIT[index];
            numberUnit.originalNumber = Integer.parseInt(String.valueOf(s.charAt(i)));
            stack.push(numberUnit);
            index++;
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (!stack.isEmpty()) {
            NumberUnit numberUnit = stack.pop();
            if (numberUnit.originalNumber > 0) {
                stringBuilder.append(numberUnit.chineseNumber);
                if (numberUnit.chineseUnit != ChineseUnit.zero) {
                    stringBuilder.append(numberUnit.chineseUnit.getValue());
                }
            } else if (numberUnit.chineseUnit != ChineseUnit.zero) {
                NumberUnit nextNumber = stack.peek();
                if (nextNumber != null && nextNumber.originalNumber != 0) {
                    stringBuilder.append(numberUnit.chineseNumber);
                }
            }
        }
        return stringBuilder.toString();
    }

    private static class NumberUnit {

        protected ChineseUnit chineseUnit;

        protected String chineseNumber;

        protected int originalNumber;
    }

    enum ChineseUnit {
        /**
         *
         */
        zero("零"),
        /**
         *
         */
        ten("十"),
        /**
         *
         */
        hundred("百"),
        thousand("千"),
        ten_thousand("万"),
        billion("十"),
        million("百"),
        ten_million("千"),
        hundred_mullion("亿");

        private String value;

        ChineseUnit(String value) {
            this.value = value;
        }

        /**
         * Getter method for property <tt>value</tt>.
         *
         * @return property value of value
         */
        public String getValue() {
            return value;
        }

        /**
         * Setter method for property <tt>value</tt>.
         *
         * @param value value to be assigned to property value
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}
