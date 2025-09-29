package com.tihai.helper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Copyright : DuanInnovator
 * @Description : Okhttp协助类
 * @Author : DuanInnovator
 * @CreateTime : 2025/7/28
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Slf4j
@SuppressWarnings("all")
public final class OkHttpSafetyHelper {


    /**
     * 执行请求并返回结果
     * @param client OkHttpClient
     * @param request 请求
     * @return 结果
     * @throws IOException 抛出的异常
     */
    public static SafeResponse executeAndReturnSafeResponse(
            OkHttpClient client,
            Request request
    ) throws IOException {
        return executeAndReturnSafeResponse(client, request, null,null);
    }

    /**
     *
     * 执行请求并返回结果
     * @param client OkHttpClient
     * @param request 请求
     * @return 结果
     * @throws IOException 抛出的异常
     */
    public static SafeResponse executeAndReturnSafeResponse(
            OkHttpClient client,
            Request request,
            Integer... expectedStatuses
    ) throws IOException {
        Response response = null;
        try {
            response = client.newCall(request).execute();
            validateResponseStatus(response, expectedStatuses);
            return new SafeResponse(response);
        } catch (Exception e) {
            closeQuietly(response);
            throw e;
        }
    }

    /**
     *
     * 带安全处理的异步请求
     * @param client OkHttpClient
     * @param request  请求
     * @param callback callback
     */
    public static void enqueueSafe(OkHttpClient client, Request request, Callback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody ignored = response.body()) {
                    callback.onResponse(call, response);
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }
        });
    }


    /**
     * 验证HTTP响应状态码（不检查响应体中的业务状态码）
     * @param response HTTP响应对象
     * @param expectedStatuses 期望的HTTP状态码（如200、302），可接受多个值。若为null或空，则只检查是否为2xx成功状态
     * @throws IOException 当状态码不匹配或响应失败时抛出
     */
    private static void validateResponseStatus(Response response, Integer... expectedStatuses) throws IOException {
        if (response == null) {
            throw new IOException("Response is null");
        }

        // 情况1：未指定期望状态码 → 只检查是否为2xx
        if (expectedStatuses == null || expectedStatuses.length == 0) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP request failed: " + response.code());
            }
            return;
        }

        // 情况2：处理包含null的期望状态码数组
        int actualCode = response.code();

        boolean hasValidExpectation = false;

        for (Integer expected : expectedStatuses) {
            if (expected != null) {
                hasValidExpectation = true;
                if (actualCode == expected) {
                    return; // 匹配成功
                }
            }
        }

        // 根据是否有有效的期望值生成不同的错误信息
        if (!hasValidExpectation) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP request failed: " + actualCode);
            }
        } else {
            String validExpectations = Arrays.stream(expectedStatuses)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new IOException(
                    String.format("Expected HTTP status [%s] but got %d",
                            validExpectations, actualCode)
            );
        }
    }
    private static void closeQuietly(Response response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception ignored) {
                // 忽略关闭时的异常
            }
        }
    }

    /**
     * 响应处理
     * @param <T>
     */
    public interface ResponseHandler<T> {
        T handle(Response response, ResponseBody body) throws IOException;
    }

    /**
     * 封装 Response
     */
    public static final class SafeResponse implements AutoCloseable {
        private final Response response;

        private SafeResponse(Response response) {
            this.response = response;
        }

        public Response getRawResponse() {
            return response;
        }

        @Override
        public void close() {
            response.close();
        }

        public int getStatusCode() {
            return response.code();
        }

    }
}
