package com.tihai.domain.chaoxing;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Copyright : DuanInnovator
 * @Description : 网课-用户信息
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutoStudy">...</a>
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("th_wk_user")
public class WkUser {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 学校
     */
    private String schoolName;

    /**
     * 学校id
     */
    private Long fid;


    public WkUser(String account, String password) {
        this.account = account;
        this.password = password;
    }
}

