package com.tihai.manager;

import com.tihai.helper.AccountIndependentCookieJarHelper;
import okhttp3.Cookie;
import java.util.List;
/**
 * @Copyright : DuanInnovator
 * @Description : Cookie管理
 * @Author : DuanInnovator
 * @CreateTime : 2025/8/8
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class GlobalCookieManager {

    private static final AccountIndependentCookieJarHelper globalCookieJar = new AccountIndependentCookieJarHelper();

    /**
     * 获取全局CookieJar实例
     */
    public static AccountIndependentCookieJarHelper getInstance() {
        return globalCookieJar;
    }

    /**
     * 设置当前线程的账号上下文
     */
    public static void setCurrentAccount(String account) {
        globalCookieJar.setCurrentAccount(account);
    }

    /**
     * 清除当前线程的账号上下文
     */
    public static void clearCurrentAccount() {
        globalCookieJar.clearCurrentAccount();
    }

    /**
     * 清除指定账号的所有Cookie
     */
    public static void clearAccount(String account) {
        globalCookieJar.clearAccount(account);
    }

    /**
     * 获取指定账号的所有Cookie（调试用）
     */
    public static List<Cookie> getAccountCookies(String account) {
        return globalCookieJar.getAllCookiesForAccount(account);
    }
}

