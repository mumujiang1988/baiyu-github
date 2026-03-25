package com.ruoyi.business.util;

/**
 * 数据库记录自动编号生成器
 * */

public class SequenceGeneratorUtil {

    /**
     * 数据库记录自动编号生成器
     */
    public static class CodeGenerator {
        private final String prefix;
        private int currentNumber;
        private final int digitLength;

        /**
         * @param prefix      前缀，如 "JX"
         * @param startNumber 起始数字
         * @param digitLength 数字部分位数
         */
        public CodeGenerator(String prefix, int startNumber, int digitLength) {
            this.prefix = prefix;
            this.currentNumber = startNumber;
            this.digitLength = digitLength;
        }

        /**
         * 生成下一个编号
         */
        public synchronized String next() {
            String numberStr = String.format("%0" + digitLength + "d", currentNumber);
            currentNumber++;
            return prefix + numberStr;
        }

        /**
         * 基于现有最大编号初始化
         */
        public static CodeGenerator fromMaxCode(String maxCode, int digitLength) {
            if (maxCode == null || maxCode.isEmpty()) {
                throw new IllegalArgumentException("最大编号不能为空");
            }

            String prefix = maxCode.replaceAll("\\d+$", "");
            String numberStr = maxCode.replaceAll("^\\D+", "");

            if (numberStr.isEmpty()) {
                return new CodeGenerator(prefix, 1, digitLength);
            }

            int currentNumber = Integer.parseInt(numberStr);
            return new CodeGenerator(prefix, currentNumber + 1, digitLength);
        }

        public int getCurrentNumber() {
            return currentNumber;
        }
    }

}
