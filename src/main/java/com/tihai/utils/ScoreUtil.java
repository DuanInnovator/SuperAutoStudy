package com.tihai.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * @Copyright : DuanInnovator
 * @Description : 分数工具类
 * @Author : DuanInnovator
 * @CreateTime : 2025/5/19
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
public class ScoreUtil {

    /**
     * 解析分数
     *
     * @param html html
     * @return 分数
     */
    public String getScoreFromCourse(String html) {
        Document doc = Jsoup.parse(html);

        Element scoreElement = doc.selectFirst("span.num");

        if (scoreElement != null) {
            return scoreElement.text();
        } else {
            return "0";
        }
    }
}

