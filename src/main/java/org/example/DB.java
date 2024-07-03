package org.example;

import java.sql.Connection;

/**
 * 数据库连接
 * @author 杰~
 * @version 1.0
 */
public class DB {
    private static Connection connection=null;
    private static final String url="jdbc:mysql://localhost:3306/worddb?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
    private static final String user="root";
    private static final String password="123456";
    public static Connection GetConnection()
    {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
