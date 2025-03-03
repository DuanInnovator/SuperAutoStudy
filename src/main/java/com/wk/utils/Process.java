package com.wk.utils;

import com.wk.config.GlobalConst;

/**
 * @Copyright : DuanInnovator
 * @Description : 进度工具
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class Process {
    /**
     * 将秒数转换为时间字符串
     * 如果小时数不为0，格式为 H:MM:SS，
     * 否则为 MM:SS，如果秒数为0则返回 "--:--"
     *
     * @param sec 秒数
     * @return 格式化后的时间字符串
     */
    public static String sec2time(int sec) {
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;
        if (h != 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        if (sec != 0) {
            return String.format("%02d:%02d", m, s);
        }
        return "--:--";
    }

    /**
     * 显示任务进度条，类似于 Python 版本的 show_progress 方法
     *
     * @param name   任务名称
     * @param start  开始进度（秒）
     * @param span   本次任务耗时（按加速速率计算，单位秒）
     * @param total  总时长（秒）
     * @param speed  加速倍率
     */
    public static void showProgress(String name, int start, int span, int total, double speed) {
        double startTime = System.currentTimeMillis() / 1000.0;
        // 循环直到经过的真实时间达到 span / speed
        while ((System.currentTimeMillis() / 1000.0 - startTime) < (span / speed)) {
            double elapsed = System.currentTimeMillis() / 1000.0 - startTime;
            int current = start + (int)(elapsed * speed);
            int percent = (int)(((double) current / total) * 100);
            int length = (int)(percent * 40.0 / 100.0);
            String progress = generateProgressBar(length, 40);
            // 使用回车符更新同一行
            System.out.print(String.format("\r当前任务: %s |%s| %d%%  %s/%s",
                    name, progress, percent, sec2time(current), sec2time(total)));
            System.out.flush();
            try {
                // GlobalConst.THRESHOLD 预设为秒（如 0.1），转换为毫秒
                Thread.sleep((long)(GlobalConst.THRESHOLD * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 辅助方法：生成指定长度的进度条字符串
     *
     * @param filledLength 已填充部分长度（# 字符数量）
     * @param totalLength  总长度
     * @return 进度条字符串（已填充部分用 # 填充，其余部分为空格）
     */
    private static String generateProgressBar(int filledLength, int totalLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filledLength; i++) {
            sb.append("#");
        }
        for (int i = filledLength; i < totalLength; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

}

