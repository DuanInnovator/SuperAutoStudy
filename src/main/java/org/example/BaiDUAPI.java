package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URLDecoder;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 百度翻译API
 */
public class BaiDUAPI {
    private static final String API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate?";
    private static final String API_KEY = "nnaDkZVS28jtu28hhMIr";
    private static final String APP_ID = "20240629002088191";


    public static String translate(String english) throws UnsupportedEncodingException {

        String salt = generateSalt(); // 生成随机数
        String q = english; // 待翻译文本
        String from = "auto"; // 翻译源语言，此处设置为自动识别
        String translatedText="";
        String[] split = q.split(" ");
        for (int i = 0; i < split.length; i++) {
            try {

                String sign = generateSign(APP_ID, split[i], salt); // 生成签名

                // 构建HTTP请求
                Map<String, String> params = new HashMap<>();
                params.put("q", URLEncoder.encode(split[i], "UTF-8"));
                params.put("from", from);
                params.put("to", "zh");
                params.put("appid", APP_ID);
                params.put("salt", salt);
                params.put("sign", sign);

                translatedText= translatedText+sendRequest(params); // 发送HTTP请求并获取翻译结果

            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }
        return translatedText;
    }

    // 生成随机数
    private static String generateSalt() {
        return new Random().nextInt(10001)+""; // 这里可以根据需要生成随机数，例如随机字符串或数字
    }

    // 生成签名
    private static String generateSign(String APP_ID, String q, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String rawString = APP_ID +q+ salt + API_KEY;
        return md5(rawString).toLowerCase(); // 将MD5哈希值转换为小写格式作为签名
    }

    // 计算MD5哈希值
    private static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    // 发送HTTP请求
    private static String sendRequest(Map<String, String> params) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);

        try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
            // 构建POST参数
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append("&");
                }
                postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8.toString()));
                postData.append("=");
                postData.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8.toString()));
            }
            wr.write(postData.toString());
            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 获取响应
        int responseCode = conn.getResponseCode();

        StringBuilder response = new StringBuilder();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

        } else {
            System.out.println("POST request not worked");
        }

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
        JsonArray transResultArray = jsonObject.getAsJsonArray("trans_result");
        if (transResultArray != null && transResultArray.size() > 0) {
            JsonObject transResultObject = transResultArray.get(0).getAsJsonObject(); // 取第一个元素
            String dstValue = transResultObject.get("dst").getAsString();


            // 将 Unicode 编码字符串转换成中文
            String chineseText = unicodeToChinese(dstValue);
            return chineseText;
        }
        return "未知错误";
    }

    // Unicode 编码字符串转换成中文
    private static String unicodeToChinese(String unicodeStr) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;

        while ((i = unicodeStr.indexOf("\\u", pos)) != -1) {
            sb.append(unicodeStr.substring(pos, i));
            if (i + 5 < unicodeStr.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
            }
        }

        sb.append(unicodeStr.substring(pos));

        return sb.toString();
    }




}
