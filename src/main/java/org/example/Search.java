package org.example;

import com.sun.xml.internal.ws.api.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * 搜索界面
 * @author 杰~
 * @version 1.0
 */
public class Search {
    private JTextField searchField;
    private JTextArea searchResultArea;
    private JRadioButton wordSearchButton;
    private JRadioButton sentenceSearchButton;
    public JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建单选按钮或下拉菜单供用户选择搜索内容
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wordSearchButton = new JRadioButton("搜索单词");
        sentenceSearchButton = new JRadioButton("搜索例句");
        ButtonGroup searchGroup = new ButtonGroup();
        searchGroup.add(wordSearchButton);
        searchGroup.add(sentenceSearchButton);
        radioPanel.add(wordSearchButton);
        radioPanel.add(sentenceSearchButton);

        // 创建搜索输入框和搜索按钮
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        inputPanel.add(new JLabel("关键词:"));
        inputPanel.add(searchField);
        inputPanel.add(searchButton);

        // 创建显示搜索结果的文本区域
        searchResultArea = new JTextArea(10, 50);
        searchResultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(searchResultArea);

        // 添加事件处理，根据选择执行搜索
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    performSearch();
                } catch (SQLException ex) {
                    ex.printStackTrace();

                }
            }
        });

        panel.add(radioPanel, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    public void performSearch() throws SQLException {
        String keyword = searchField.getText().trim();
        searchResultArea.setText(""); // 清空上次搜索结果

        if (wordSearchButton.isSelected()) {
            // 搜索单词
            searchWords(keyword);
        } else if (sentenceSearchButton.isSelected()) {
            // 搜索例句
            searchSentences(keyword);
        }
    }

    public void searchWords(String keyword) throws SQLException {
        try (Connection connection = DB.GetConnection();
             PreparedStatement smt = connection.prepareStatement("SELECT * FROM words WHERE word LIKE ? AND status = 1")) {
            smt.setString(1, "%" + keyword + "%");
            ResultSet rs = smt.executeQuery();
            displaySearchResults(rs);
        }
    }

    public void searchSentences(String keyword) throws SQLException {
        try (Connection connection = DB.GetConnection();
             PreparedStatement smt = connection.prepareStatement("SELECT * FROM sentences WHERE sentence LIKE ? AND status = 1")) {
            smt.setString(1, "%" + keyword + "%");
            ResultSet rs = smt.executeQuery();
            displaySearchResultsSentence(rs);
        }
    }
    public void displaySearchResults(ResultSet rs) throws SQLException {
        StringBuilder result = new StringBuilder();
        while (rs.next()) {
            try {
                String word = rs.getString("word"); // 可以根据具体表结构调整
                String addTime = rs.getString("add_time");
                String updateTime = rs.getString("update_time");
                String addUser = rs.getString("username");
                result.append("内容: ").append(word).append("\n")
                        .append("添加时间: ").append(addTime).append("\n")
                        .append("修改时间: ").append(updateTime).append("\n")
                        .append("添加人: ").append(addUser).append("\n\n");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        searchResultArea.setText(result.toString());
    }
    public void displaySearchResultsSentence(ResultSet rs) throws SQLException {
        StringBuilder result = new StringBuilder();
        while (rs.next()) {
            try {
                String sentence = rs.getString("sentence"); // 可以根据具体表结构调整
                String addTime = rs.getString("add_time");
                String updateTime = rs.getString("update_time");
                String addUser = rs.getString("username");
                result.append("内容: ").append(sentence).append("\n")
                        .append("添加时间: ").append(addTime).append("\n")
                        .append("修改时间: ").append(updateTime).append("\n")
                        .append("添加人: ").append(addUser).append("\n\n");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        searchResultArea.setText(result.toString());
    }
}
