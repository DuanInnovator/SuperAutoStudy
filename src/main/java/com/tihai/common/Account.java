package com.tihai.common;

import lombok.Data;

/**
 * @Copyright : DuanInnovator
 * @Description : 账户
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class Account {
    private String username;
    private String password;
    private boolean isSuccess;

    public Account(String username, String password){
        this.username = username;
        this.password = password;
    }
}

