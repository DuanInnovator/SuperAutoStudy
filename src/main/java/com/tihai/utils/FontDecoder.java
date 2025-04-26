package com.tihai.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fontbox.ttf.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright : DuanInnovator
 * @Description : 字体解析器
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/23
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/

public class FontDecoder {

    private static final String FONT_MAP_TABLE_FILE_PATH = "font_map_table.json";

    // 康熙部首替换表
    private static final Map<Integer, Character> KX_RADICALS_TAB = new HashMap<>();
    static {
        String keys = "⼀⼁⼂⼃⼄⼅⼆⼇⼈⼉⼊⼋⼌⼍⼎⼏⼐⼑⼒⼓⼔⼕⼖⼗⼘⼙⼚⼛⼜⼝⼞⼟⼠⼡⼢⼣⼤⼥⼦⼧⼨⼩⼪⼫⼬⼭⼮⼯⼰⼱⼲⼳⼴⼵⼶⼷⼸⼹⼺⼻⼼⼽⼾⼿⽀⽁⽂⽃⽄⽅⽆⽇⽈⽉⽊⽋⽌⽍⽎⽏⽐⽑⽒⽓⽔⽕⽖⽗⽘⽙⽚⽛⽜⽝⽞⽟⽠⽡⽢⽣⽤⽥⽦⽧⽨⽩⽪⽫⽬⽭⽮⽯⽰⽱⽲⽳⽴⽵⽶⽷⽸⽹⽺⽻⽼⽽⽾⽿⾀⾁⾂⾃⾄⾅⾆⾇⾈⾉⾊⾋⾌⾍⾎⾏⾐⾑⾒⾓⾔⾕⾖⾗⾘⾙⾚⾛⾜⾝⾞⾟⾠⾡⾢⾣⾤⾥⾦⾧⾨⾩⾪⾫⾬⾭⾮⾯⾰⾱⾲⾳⾴⾵⾶⾷⾸⾹⾺⾻⾼髙⾽⾾⾿⿀⿁⿂⿃⿄⿅⿆⿇⿈⿉⿊⿋⿌⿍⿎⿏⿐⿑⿒⿓⿔⿕⺠⻬⻩⻢⻜⻅⺟⻓";
        String values = "一丨丶丿乙亅二亠人儿入八冂冖冫几凵刀力勹匕匚匸十卜卩厂厶又口囗土士夂夊夕大女子宀寸小尢尸屮山巛工己巾干幺广廴廾弋弓彐彡彳心戈戶手支攴文斗斤方无日曰月木欠止歹殳毋比毛氏气水火爪父爻爿片牙牛犬玄玉瓜瓦甘生用田疋疒癶白皮皿目矛矢石示禸禾穴立竹米糸缶网羊羽老而耒耳聿肉臣自至臼舌舛舟艮色艸虍虫血行衣襾見角言谷豆豕豸貝赤走足身車辛辰辵邑酉采里金長門阜隶隹雨青非面革韋韭音頁風飛食首香馬骨高高髟鬥鬯鬲鬼魚鳥鹵鹿麥麻黃黍黑黹黽鼎鼓鼠鼻齊齒龍龜龠民齐黄马飞见母长";
        for (int i = 0; i < keys.length(); i++) {
            KX_RADICALS_TAB.put((int) keys.charAt(i), values.charAt(i));
        }
    }

    /** 原始字体hashmap DAO */
    public static class FontHashDAO {
        private final Map<String, String> charMap;
        private final Map<String, String> hashMap;

        public FontHashDAO(String filePath) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            if (inputStream == null) {
                throw new FileNotFoundException("File not found in classpath: " + filePath);
            }
            charMap = mapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
            hashMap = new HashMap<>();
            for (Map.Entry<String, String> e : charMap.entrySet()) {
                hashMap.put(e.getValue(), e.getKey());
            }
        }

        public String findChar(String fontHash) {
            return hashMap.get(fontHash);
        }

        public String findHash(String ch) {
            return charMap.get(ch);
        }
    }

    private static final FontHashDAO fonthashDao;

    static {
        try {

            fonthashDao = new FontHashDAO(FONT_MAP_TABLE_FILE_PATH);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** ttf 字形曲线转 MD5 hash */
    public static String hashGlyph(GlyphDescription desc) {
        StringBuilder sb = new StringBuilder();
        int last = 0;
        int contourCount = desc.getContourCount();
        for (int i = 0; i < contourCount; i++) {
            int endPt = desc.getEndPtOfContours(i);
            for (int j = last; j <= endPt; j++) {
                sb.append(desc.getXCoordinate(j))
                        .append(desc.getYCoordinate(j))
                        .append(desc.getFlags(j) & 0x01);
            }
            last = endPt + 1;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** 解析 Base64 或文件路径，生成 glyphName → hash 映射 */
    public static Map<String, String> font2map(String fontData) throws IOException {
        byte[] bytes;
        if (fontData.startsWith("data:")) {
            String b64 = fontData.substring(fontData.indexOf(',') + 1);
            bytes = Base64.getDecoder().decode(b64);
        } else {
            bytes = Files.readAllBytes(new File(fontData).toPath());
        }
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            TrueTypeFont font = new TTFParser(false).parse(is);

            CmapTable cmapTable = font.getCmap();
            CmapSubtable cmapSubtable = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS, CmapTable.ENCODING_WIN_UNICODE_BMP);

            Map<String, String> map = new HashMap<>();
            GlyphTable glyphTable = font.getGlyph();
            GlyphData[] glyphs = glyphTable.getGlyphs();

            if (cmapSubtable != null) {
                for (char c = 0; c < 0xFFFF; c++) {
                    int glyphId = cmapSubtable.getGlyphId(c);
                    if (glyphId != 0 && glyphId < glyphs.length) {
                        GlyphData gd = glyphs[glyphId];
                        if (gd == null) {
                            continue;
                        }
                        GlyphDescription desc = gd.getDescription();
                        // 使用字符的十六进制编码作为 key
                        String hexName = Integer.toHexString(c);
                        map.put("uni" + hexName.toUpperCase(), hashGlyph(desc));
                    }
                }
            }
            font.close();
            return map;
        }
    }

    /** 解密目标字符串 */
    public static String decrypt(Map<String, String> dstMap, String dstStr) {
        StringBuilder ori = new StringBuilder();
        for (char c : dstStr.toCharArray()) {
            String key = String.format("uni%04X", (int) c);
            String hash = dstMap.get(key);
            if (hash != null) {
                String uni = fonthashDao.findChar(hash);
                if (uni != null) {
                    int code = Integer.parseInt(uni.substring(3), 16);
                    ori.append((char) code);
                }
            } else {
                ori.append(c);
            }
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < ori.length(); i++) {
            char ch = ori.charAt(i);
            out.append(KX_RADICALS_TAB.getOrDefault((int) ch, ch));
        }
        return out.toString();
    }

    private final Map<String, String> fontMap;

    public FontDecoder(String html) throws IOException {
        Document doc = Jsoup.parse(html);
        Element style = doc.getElementById("cxSecretStyle");
        Pattern p = Pattern.compile("base64,([\\w\\W]+?)'");
        Matcher m = p.matcher(style.html());
        if (m.find()) {
            this.fontMap = font2map("data:application/font-ttf;charset=utf-8;base64," + m.group(1));
        } else {
            throw new IllegalArgumentException("未找到字体 Base64 数据");
        }
    }

    public static String readHtmlFile(String htmlFilePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(htmlFilePath))) {
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                contentBuilder.append(currentLine).append("\n");
            }

            return contentBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String decode(String target) {
        return decrypt(this.fontMap, target);
    }

    public static void main(String[] args) throws IOException {

        String htmlFilePath = "D:\\Develop\\project\\test\\font-2\\src\\main\\java\\com\\example\\font2\\demo\\test.html";
        String htmlContent = readHtmlFile(htmlFilePath);

        FontDecoder decoder = new FontDecoder(htmlContent);


    }
}
