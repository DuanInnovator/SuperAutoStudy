package com.tihai.dubbo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Copyright : DuanInnovator
 * @Description : 网课用户DTO
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/4
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutoStudy">...</a>
 **/
@Data
@AllArgsConstructor
public class WKUserDTO implements Serializable {

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;
}

