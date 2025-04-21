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
@TableName("th_wk_user")
@AllArgsConstructor
@NoArgsConstructor
public class WkUser {

    /**
     * 账号
     */
    @TableId(value = "account")
    private String account;

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

    /**
     * cookie信息
     */
    private String cookies;
}

