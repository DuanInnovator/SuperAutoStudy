package com.tihai.helper;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 账号独立的CookieJar实现，支持多线程环境下不同账号的Cookie隔离
 */
public class AccountIndependentCookieJarHelper implements CookieJar {

    // 每个账号对应一个独立的Cookie存储
    private final Map<String, AccountCookieStore> accountCookieStores = new ConcurrentHashMap<>();

    // 当前线程的账号标识
    private final ThreadLocal<String> currentAccount = new ThreadLocal<>();

    /**
     * 设置当前线程的账号
     */
    public void setCurrentAccount(String account) {
        currentAccount.set(account);
    }

    /**
     * 获取当前线程的账号
     */
    public String getCurrentAccount() {
        return currentAccount.get();
    }

    /**
     * 清除当前线程的账号信息
     */
    public void clearCurrentAccount() {
        currentAccount.remove();
    }

    /**
     * 清除指定账号的所有Cookie
     */
    public void clearAccount(String account) {
        accountCookieStores.remove(account);
    }

    /**
     * 获取指定账号的Cookie存储
     */
    private AccountCookieStore getAccountStore() {
        String account = getCurrentAccount();
        if (account == null) {
            throw new IllegalStateException("当前线程未设置账号，请先调用 setCurrentAccount()");
        }
        return accountCookieStores.computeIfAbsent(account, k -> new AccountCookieStore());
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl url, List<Cookie> cookies) {
        if (cookies.isEmpty()) return;

        AccountCookieStore store = getAccountStore();
        store.saveFromResponse(url, cookies);
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl url) {
        AccountCookieStore store = getAccountStore();
        return store.loadForRequest(url);
    }

    /**
     * 获取指定账号的所有Cookie（用于调试）
     */
    public List<Cookie> getAllCookiesForAccount(String account) {
        AccountCookieStore store = accountCookieStores.get(account);
        if (store == null) {
            return new ArrayList<>();
        }
        return store.getAllCookies();
    }

    /**
     * 账号级别的Cookie存储
     */
    private static class AccountCookieStore {
        private final Map<String, CopyOnWriteArrayList<Cookie>> cookieStore = new ConcurrentHashMap<>();
        private final Map<String, ReentrantLock> domainLocks = new ConcurrentHashMap<>();

        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            if (cookies.isEmpty()) return;

            Map<String, List<Cookie>> grouped = cookies.stream()
                    .collect(Collectors.groupingBy(c -> normalizeDomain(c.domain())));

            grouped.forEach((domain, cookieList) -> {
                ReentrantLock lock = domainLocks.computeIfAbsent(domain, k -> new ReentrantLock());
                lock.lock();
                try {
                    CopyOnWriteArrayList<Cookie> existing = cookieStore.computeIfAbsent(
                            domain, k -> new CopyOnWriteArrayList<>()
                    );

                    cookieList.forEach(newCookie -> {
                        existing.removeIf(existingCookie -> isSameCookie(existingCookie, newCookie));

                        if (newCookie.expiresAt() > System.currentTimeMillis()) {
                            existing.add(newCookie);
                        }
                    });
                } finally {
                    lock.unlock();
                }
            });
        }


        public List<Cookie> getAllCookies() {
            List<Cookie> allCookies = new ArrayList<>();
            cookieStore.values().forEach(allCookies::addAll);
            return allCookies.stream()
                    .filter(cookie -> cookie.expiresAt() > System.currentTimeMillis())
                    .collect(Collectors.toList());
        }

        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> matchedCookies = new ArrayList<>();
            String host = url.host();

            cleanExpiredCookies();

            cookieStore.forEach((domain, cookies) -> {
                ReentrantLock lock = domainLocks.get(domain);
                if (lock != null) {
                    lock.lock();
                    try {
                        for (Cookie cookie : cookies) {
                            if (isValidForRequest(cookie, url)) {
                                matchedCookies.add(cookie);
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            });

            return matchedCookies;
        }

        private boolean isValidForRequest(Cookie cookie, HttpUrl url) {
            // 检查过期时间
            if (cookie.expiresAt() <= System.currentTimeMillis()) {
                return false;
            }

            String host = url.host();
            String cookieDomain = cookie.domain();

            // 域名匹配 - 使用更宽松的匹配逻辑
            boolean domainMatch = host.equals(cookieDomain) || host.endsWith("." + cookieDomain);

            // 路径匹配
            boolean pathMatch = url.encodedPath().startsWith(cookie.path());

            // HTTPS检查
            boolean secureMatch = !cookie.secure() || url.isHttps();

            return domainMatch && pathMatch && secureMatch;
        }

        private void cleanExpiredCookies() {
            long currentTime = System.currentTimeMillis();
            cookieStore.forEach((domain, cookies) -> {
                ReentrantLock lock = domainLocks.get(domain);
                if (lock != null) {
                    lock.lock();
                    try {
                        cookies.removeIf(cookie -> cookie.expiresAt() <= currentTime);
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }

        private boolean isSameCookie(Cookie c1, Cookie c2) {
            return c1.name().equals(c2.name())
                    && normalizeDomain(c1.domain()).equals(normalizeDomain(c2.domain()))
                    && c1.path().equals(c2.path());
        }

        private String normalizeDomain(String domain) {
            return domain.startsWith(".") ? domain : "." + domain;
        }
    }

}
