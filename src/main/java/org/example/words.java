package org.example;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单词实体类
 * @author 杰~
 * @version 1.0
 */
@Data
public class words {
    private String word;
    private String username;
    private LocalDateTime addTime;
    private LocalDateTime updateTime;


}
