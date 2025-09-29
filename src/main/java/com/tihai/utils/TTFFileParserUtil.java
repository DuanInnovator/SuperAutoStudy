package com.tihai.utils;

import org.apache.fontbox.ttf.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description : TTF解析
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutoStudy">...</a>
 **/
@SuppressWarnings("all")
public class TTFFileParserUtil {
    private final Map<String, String> charMap = new HashMap<>();

    public void parse(InputStream fontStream) throws IOException {
        TrueTypeFont font = new TTFParser().parse(fontStream);
        CmapLookup cmap = font.getUnicodeCmapLookup();

        if (cmap != null) {
            for (int i = 0; i < 65536; i++) {
                int glyphId = cmap.getGlyphId(i);
                if (glyphId > 0) {
                    charMap.put("uni" + String.format("%04X", i), String.valueOf((char) i));
                }
            }
        }
    }

    private String getGlyphName(TrueTypeFont font, int glyphId) {
        try {
            PostScriptTable postTable = font.getPostScript();
            if (postTable != null) {
                return postTable.getName(glyphId);
            }
        } catch (Exception ignored) {
        }
        return "glyph_" + glyphId;
    }





    public Map<String, String> getCharMap() {
        return charMap;
    }

    // 康熙部首替换初始化
    private static final Map<Character, Character> KX_RADICALS = new HashMap<>();

    static {
        KX_RADICALS.put('⼀', '一');
        KX_RADICALS.put('⼁', '丨');
        KX_RADICALS.put('⼂', '丶');
        KX_RADICALS.put('⼃', '丿');
        KX_RADICALS.put('⼄', '乙');
        KX_RADICALS.put('⼅', '亅');
        KX_RADICALS.put('⼆', '二');
        // ... 补充完整 214 个部首
    }

    public String replaceRadicals(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = KX_RADICALS.getOrDefault(chars[i], chars[i]);
        }
        return new String(chars);
    }
}
