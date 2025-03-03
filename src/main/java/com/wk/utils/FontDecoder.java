package com.wk.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright : DuanInnovator
 * @Description : 字体解码器
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class FontDecoder {
    private Map<String, String> fontMap = new HashMap<>();

    public FontDecoder(String htmlContent) {
        if (htmlContent != null) {
            initFontMap(htmlContent);
        }
    }

    private void initFontMap(String html) {
        try {
            Document doc = Jsoup.parse(html);
            Element styleTag = doc.selectFirst("style#cxSecretStyle");
            if (styleTag == null) return;

            Matcher matcher = Pattern.compile("base64,([\\w\\W]+?)'").matcher(styleTag.html());
            if (matcher.find()) {
                byte[] fontData = Base64.getDecoder().decode(matcher.group(1));
                parseFont(new ByteArrayInputStream(fontData));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 使用 Apache PDFBox 解析字体（需依赖）
    private void parseFont(InputStream fontStream) throws IOException {
        TTFFileParser parser = new TTFFileParser(); // 自定义 TTF 解析器
        parser.parse(fontStream);
        this.fontMap = parser.getCharMap();
    }

    public String decode(String encodedStr) {
        StringBuilder decoded = new StringBuilder();
        for (char c : encodedStr.toCharArray()) {
            String key = String.format("uni%04x", (int) c);
            decoded.append(fontMap.getOrDefault(key, String.valueOf(c)));
        }
        return decoded.toString();
    }
}

