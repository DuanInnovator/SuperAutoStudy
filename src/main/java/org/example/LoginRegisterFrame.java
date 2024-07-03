package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 登录注册界面
 */

@SuppressWarnings({"all"})
public class LoginRegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private Connection connection;


    public LoginRegisterFrame() {
        setTitle("英语学习助手");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("欢迎登录", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28)); // 设置大字体
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout()); // 使用 GridBagLayout 布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(new Font("宋体", Font.PLAIN, 20));
        inputPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("宋体", Font.PLAIN, 18));
        inputPanel.add(usernameField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passLabel = new JLabel("密码:");
        passLabel.setFont(new Font("宋体", Font.PLAIN, 20));
        inputPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("宋体", Font.PLAIN, 18));
        inputPanel.add(passwordField, gbc);

        panel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton loginButton = new JButton("登录");
        loginButton.setFont(new Font("宋体", Font.BOLD, 20));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginRegisterFrame.this, "请输入用户名和密码");
                    return;
                }
                //登录验证的代码
                connection = DB.GetConnection();
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = connection.prepareStatement("select * from users where username=? and password=?");
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    if (preparedStatement.executeQuery().next()) {
                        JOptionPane.showMessageDialog(LoginRegisterFrame.this, "登录成功");
                        if (username.equals("root")) {
                            new WordManagerFrame(username, true);
                        } else {
                            new WordManagerFrame(username, false);
                        }

                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginRegisterFrame.this, "用户名或密码错误");
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }


            }
        });
        buttonPanel.add(loginButton);

        JButton registerButton = new JButton("注册");
        registerButton.setFont(new Font("宋体", Font.BOLD, 20));
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 打开注册界面
                new RegisterFrame();
            }
        });
        buttonPanel.add(registerButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginRegisterFrame();
            }
        });
    }
}

class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private  DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RegisterFrame() {
        setTitle("注册界面");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("欢迎注册", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28)); // 设置大字体
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(new Font("宋体", Font.PLAIN, 20));
        inputPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("宋体", Font.PLAIN, 18));
        inputPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passLabel = new JLabel("密码:");
        passLabel.setFont(new Font("宋体", Font.PLAIN, 20));
        inputPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("宋体", Font.PLAIN, 18));
        inputPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel confirmPassLabel = new JLabel("确认密码:");
        confirmPassLabel.setFont(new Font("宋体", Font.PLAIN, 20));
        inputPanel.add(confirmPassLabel, gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setFont(new Font("宋体", Font.PLAIN, 18));
        inputPanel.add(confirmPasswordField, gbc);


        panel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton registerButton = new JButton("注册");
        registerButton.setFont(new Font("宋体", Font.BOLD, 20));
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                try {
                    if (username.isEmpty() && password.isEmpty()) {
                        JOptionPane.showMessageDialog(RegisterFrame.this, "用户名或密码不能为空");
                    } else if (!password.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(RegisterFrame.this, "两次密码不一致");
                    } else {
                        Connection connection = DB.GetConnection();
                        PreparedStatement preparedStatement = connection.prepareStatement("select * from users where username=?");
                        preparedStatement.setString(1, username);
                        if (preparedStatement.executeQuery().next()) {
                            JOptionPane.showMessageDialog(RegisterFrame.this, "用户名已存在");
                        } else {
                            if (password.length() < 6) {
                                JOptionPane.showMessageDialog(RegisterFrame.this, "密码长度不能小于6位");
                            } else {
                                PreparedStatement preparedStatement1 = connection.prepareStatement("insert into users(username,password,register_time,status) values(?,?,?,1)");
                                preparedStatement1.setString(1, username);
                                preparedStatement1.setString(2, password);
                                preparedStatement1.setString(3, LocalDateTime.now().format(DATE_FORMAT));
                                preparedStatement1.execute();
                                JOptionPane.showMessageDialog(RegisterFrame.this, "注册成功");
                                connection.close();
                            }
                        }
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        buttonPanel.add(registerButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }
}
