package com.wk.utils;

import com.wk.properties.GlobalProperties;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @Copyright : DuanInnovator
 * @Description : AES加密工具
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/

@Component
@SuppressWarnings("all")
public class AESCipher {
    private final byte[] key;
    private final byte[] iv;



    public AESCipher(GlobalProperties globalProperties) {
        // 确保与 Python 代码相同的密钥生成逻辑
        this.key = globalProperties.getAesKey().getBytes(StandardCharsets.UTF_8);
        this.iv =  globalProperties.getAesKey().getBytes(StandardCharsets.UTF_8);
    }

    // 严格模拟 Python 的 PKCS7 填充逻辑
    private  byte[] pkcs7Pad(byte[] data, int blockSize) {
        int padding = blockSize - (data.length % blockSize);
        byte[] padded = new byte[data.length + padding];
        System.arraycopy(data, 0, padded, 0, data.length);
        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) padding;
        }
        return padded;
    }

    // 严格模拟 Python 的分块逻辑
    private  byte[][] splitBlocks(byte[] data, int blockSize) {
        int numBlocks = (data.length + blockSize - 1) / blockSize;
        byte[][] blocks = new byte[numBlocks][];
        for (int i = 0; i < numBlocks; i++) {
            int start = i * blockSize;
            int end = Math.min(start + blockSize, data.length);
            blocks[i] = new byte[end - start];
            System.arraycopy(data, start, blocks[i], 0, end - start);
        }
        return blocks;
    }

    public  String encrypt(String plaintext) {
        try {
            // 1. 编码明文为 UTF-8 字节
            byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);

            // 2. 手动 PKCS7 填充（与 Python 完全一致）
            byte[] padded = pkcs7Pad(plainBytes, 16);

            // 3. 初始化加密器（CBC 模式，NoPadding 因为已手动填充）
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 4. 分块加密（严格模拟 Python 的逐块处理）
            byte[][] blocks = splitBlocks(padded, 16);
            byte[] encrypted = new byte[0];
            for (byte[] block : blocks) {
                byte[] encryptedBlock = cipher.update(block);
                encrypted = concat(encrypted, encryptedBlock);
            }

            // 5. Base64 编码
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    // 字节数组拼接工具
    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}

