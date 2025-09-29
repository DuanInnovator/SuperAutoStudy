package com.tihai.config;

import com.tihai.manager.GlobalCookieManager;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.EventListener;

import javax.net.ssl.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.tihai.common.GlobalConst.*;

/**
 * @Copyright : DuanInnovator
 * @Description : OkHttpClient初始化
 * @Author : DuanInnovator
 * @CreateTime : 2025/7/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
public class OkHttpClientInit {

    // 使用三元组键来区分不同的客户端类型
    private static final Map<String, OkHttpClient> normalClientMap = new ConcurrentHashMap<>();    // 普通请求
    private static final Map<String, OkHttpClient> videoClientMap = new ConcurrentHashMap<>();      // 视频请求
    private static final Map<String, OkHttpClient> audioClientMap = new ConcurrentHashMap<>();      // 音频请求

    private static volatile OkHttpClient ipProxyClient;

    // 全局共享的连接池（所有 Client 实例复用）
    private static final ConnectionPool sharedConnectionPool = new ConnectionPool(500, 30, TimeUnit.MINUTES);

    // 预初始化的 SSL 组件（避免重复创建）
    private static final X509TrustManager trustAllManager = createTrustAllManager();
    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();

    private OkHttpClientInit() {
    }

    /**
     * 生成客户端映射的键
     */
    private static String generateClientKey(String account, boolean isVideo, boolean isAudio) {
        return account + "_" + (isVideo ? "video" : "") + (isAudio ? "audio" : "") + (isVideo || isAudio ? "" : "normal");
    }

    /**
     * 获取或创建指定账号的 OkHttpClient 实例
     *
     * @param account 账号标识（用于隔离 Cookie）
     * @param isVideo 是否为视频请求
     * @param isAudio 是否为音频请求
     */
    public static OkHttpClient getClient(String account, boolean isVideo, boolean isAudio) {
        String clientKey = generateClientKey(account, isVideo, isAudio);

        if (isVideo) {
            return videoClientMap.computeIfAbsent(clientKey, k -> buildClient(isVideo, isAudio));
        } else if (isAudio) {
            return audioClientMap.computeIfAbsent(clientKey, k -> buildClient(isVideo, isAudio));
        } else {
            return normalClientMap.computeIfAbsent(clientKey, k -> buildClient(isVideo, isAudio));
        }
    }

    /**
     * 获取或创建用于请求代理池的OkHttpClient实例
     *
     * @return OkHttpClient
     */
    public static OkHttpClient getIpProxyAndUAClient() {
        if (ipProxyClient == null) {
            synchronized (OkHttpClientInit.class) {
                if (ipProxyClient == null) {
                    ipProxyClient = buildIpProxyClient();
                }
            }
        }
        return ipProxyClient;
    }

    /**
     * 构建专用于请求代理池的OkHttpClient 实例（共享连接池和 SSL 配置）
     */
    private static OkHttpClient buildIpProxyClient() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllManager)
                .hostnameVerifier((hostname, session) -> true) // 跳过主机名验证
                .connectionPool(sharedConnectionPool) // 共享连接池
                .connectTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .addNetworkInterceptor(new LoggingInterceptor()) // 请求日志
                .eventListener(new ConnectionEventListener()) // 监控连接状态
                .addInterceptor(new HeaderInterceptor(false, false)) // 默认不是视频也不是音频
                .build();
    }

    /**
     * 构建新的 OkHttpClient 实例（共享连接池和 SSL 配置）
     */
    private static OkHttpClient buildClient(boolean isVideo, boolean isAudio) {
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllManager)
                .hostnameVerifier((hostname, session) -> true) // 跳过主机名验证
                .cookieJar(GlobalCookieManager.getInstance())
                .connectionPool(sharedConnectionPool) // 共享连接池
                .connectTimeout(3600, TimeUnit.SECONDS)
                .readTimeout(3600, TimeUnit.SECONDS)
                .writeTimeout(3600, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .addNetworkInterceptor(new LoggingInterceptor()) // 请求日志
                .eventListener(new ConnectionEventListener()) // 监控连接状态
                .addInterceptor(new HeaderInterceptor(isVideo, isAudio))
                .build();
    }

    /**
     * 初始化 SSL 套接字工厂（全局唯一）
     */
    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustAllManager}, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("SSL initialization failed", e);
        }
    }

    /**
     * 创建信任所有证书的 TrustManager
     */
    private static X509TrustManager createTrustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    /**
     * 获取指定账号的 CookieJar（返回全局管理器的包装器）
     *
     * @param account 账号
     * @return CookieJar
     */
    public static CookieJar getCookieJar(String account) {
        return new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                GlobalCookieManager.setCurrentAccount(account);
                GlobalCookieManager.getInstance().saveFromResponse(url, cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                GlobalCookieManager.setCurrentAccount(account);
                return GlobalCookieManager.getInstance().loadForRequest(url);
            }
        };
    }

    /**
     * 清理指定账号的 Client 资源
     */
    public static void removeClient(String account) {
        // 清理所有类型的客户端
        removeFromMap(normalClientMap, account);
        removeFromMap(videoClientMap, account);
        removeFromMap(audioClientMap, account);
        GlobalCookieManager.clearAccount(account);
    }

    /**
     * 从指定映射中移除指定账号的客户端
     */
    private static void removeFromMap(Map<String, OkHttpClient> map, String account) {
        map.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(account + "_")) {
                cleanupClient(entry.getValue());
                return true;
            }
            return false;
        });
    }

    /**
     * 清理所有 Client 资源
     */
    public static void clearAllClients() {
        normalClientMap.values().forEach(OkHttpClientInit::cleanupClient);
        videoClientMap.values().forEach(OkHttpClientInit::cleanupClient);
        audioClientMap.values().forEach(OkHttpClientInit::cleanupClient);
        normalClientMap.clear();
        videoClientMap.clear();
        audioClientMap.clear();
    }

    /**
     * 关闭连接池和线程池
     */
    private static void cleanupClient(OkHttpClient client) {
        if (client != null) {
            client.dispatcher().executorService().shutdown(); // 关闭线程池
            client.connectionPool().evictAll(); // 清除所有连接
        }
    }

    /**
     * 拦截器：自动添加公共请求头（根据 isVideo 和 isAudio 参数添加不同的头）
     */
    private static class HeaderInterceptor implements Interceptor {
        private final boolean isVideo;
        private final boolean isAudio;

        public HeaderInterceptor(boolean isVideo, boolean isAudio) {
            this.isVideo = isVideo;
            this.isAudio = isAudio;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder();
//                    .header("User-Agent", UserAgentGeneratorUtil.generateUserAgent());

            Map<String, String> headersToAdd;

            if (isVideo) {
                headersToAdd = VIDEO_HEADERS;
            } else if (isAudio) {
                headersToAdd = AUDIO_HEADERS;
            } else {
                headersToAdd = HEADERS;
            }

            for (Map.Entry<String, String> entry : headersToAdd.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }

            Request newRequest = requestBuilder.build();
            return chain.proceed(newRequest);
        }
    }

    /**
     * 日志拦截器
     */
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
//            System.out.printf("--> %s %s%n", request.method(), request.url());
            Response response = chain.proceed(request);
//            System.out.printf("<-- %d %s%n", response.code(), response.request().url());
            return response;
        }
    }

    /**
     * 连接事件监听极
     */
    private static class ConnectionEventListener extends EventListener {
        // 连接事件监听实现
    }
}