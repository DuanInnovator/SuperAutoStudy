package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 词库管理
 */

public class WordManagerFrame extends JFrame {

    private JTable wordTable;
    private DefaultTableModel wordTableModel;
    private JTextField wordField;
    private JTextField addUserField;

    private JTable sentenceTable;
    private DefaultTableModel sentenceTableModel;
    private JTextField sentenceField;
    private JTextField addSentenceUserField;



    private JLabel userLabel;
    private String currentUser;
    private boolean isAdmin;
    private Connection connection;
    private int currentPage = 1;
    private int rowsPerPage = 20; // 单词每页显示的行数

    private int currentPageSentence = 1;
    private int rowsPerPageSentence = 20; // 例句每页显示的行数

    private JLabel jLabel;
    private JPanel bottomPanel;

    private JLabel jLabelSentence;
    private JPanel bottomPanelSentence;

    JButton prevPageButton=null;
    JButton nextPageButton=null;

    JButton prevPageButtonSentence=null;
    JButton nextPageButtonSentence=null;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WordManagerFrame(String currentUser, boolean isAdmin) throws SQLException {
        connection = DB.GetConnection();
        this.currentUser = currentUser;
        this.isAdmin = isAdmin;

        setTitle("英语单词和例句管理");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 创建用户信息标签
        userLabel = new JLabel("当前用户: " + currentUser);
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT); // 改为左对齐显示

        JButton exit = new JButton("退出");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(WordManagerFrame.this,
                        "确定要退出登录吗？", "确认退出", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    LoginRegisterFrame loginRegisterFrame = new LoginRegisterFrame();
                    dispose(); // 关闭当前窗口
                }
            }
        });
        // 添加退出按钮到顶部右侧
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(userLabel, BorderLayout.CENTER);
        topPanel.add(exit, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        JTabbedPane tabbedPane = new JTabbedPane();

        // 单词管理面板
        JPanel wordPanel = new JPanel(new BorderLayout());

        wordTableModel = new DefaultTableModel(new String[]{"单词", "添加时间", "修改时间", "添加人"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (!isAdmin) {
                    return false; // 普通用户不能编辑任何单元格
                }
                return true;
            }
        };
        wordTable = new JTable(wordTableModel);

        wordPanel.add(new JScrollPane(wordTable), BorderLayout.CENTER);

        JPanel wordInputPanel = new JPanel(new GridLayout(2, 1));

        JPanel wordInputFields = new JPanel();
        wordField = new JTextField(20);
        addUserField = new JTextField(20);
        wordInputFields.add(new JLabel("单词:"));
        wordInputFields.add(wordField);


        JPanel wordButtons = new JPanel();
        JButton addWordButton = new JButton("添加");
        JButton editWordButton = new JButton("修改");
        JButton deleteWordButton = new JButton("删除");
        JButton translateWordButton= new JButton("翻译");

        FlushTable();

        addWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addWord();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        editWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    editWord();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        deleteWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    deleteWord();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        translateWordButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    translateSelectedWord();
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        wordButtons.add(addWordButton);
        wordButtons.add(editWordButton);
        wordButtons.add(deleteWordButton);
        wordButtons.add(translateWordButton);

        // 普通用户只能看到添加按钮，管理员可以看到所有按钮
        if (!isAdmin) {
            editWordButton.setVisible(false);
            deleteWordButton.setVisible(false);
        }

        wordInputPanel.add(wordInputFields);
        wordInputPanel.add(wordButtons);
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 使用流式布局从左侧对齐
        jLabel = new JLabel("总记录数: " + getTotalRecordCount());
        bottomPanel.add(jLabel);
        WordPagenator(); //分页
        wordInputPanel.add(bottomPanel);

        wordPanel.add(wordInputPanel, BorderLayout.SOUTH);


        tabbedPane.addTab("单词管理", wordPanel);

        // 例句管理面板
        JPanel sentencePanel = new JPanel(new BorderLayout());

        sentenceTableModel = new DefaultTableModel(new String[]{"例句", "添加时间", "修改时间", "添加人"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (!isAdmin) {
                    return false; // 普通用户不能编辑任何单元格
                }
                return true;
            }
        };
        sentenceTable = new JTable(sentenceTableModel);

        sentencePanel.add(new JScrollPane(sentenceTable), BorderLayout.CENTER);

        JPanel sentenceInputPanel = new JPanel(new GridLayout(2, 1));

        JPanel sentenceInputFields = new JPanel();
        sentenceField = new JTextField(20);
        addSentenceUserField = new JTextField(20);
        sentenceInputFields.add(new JLabel("例句:"));
        sentenceInputFields.add(sentenceField);


        JPanel sentenceButtons = new JPanel();
        JButton addSentenceButton = new JButton("添加");
        JButton editSentenceButton = new JButton("修改");
        JButton deleteSentenceButton = new JButton("删除");
        JButton translateSentenceButton = new JButton("翻译"); // 新增翻译按钮
        FlushSentenceTable();
        addSentenceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addSentence();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        editSentenceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    editSentence();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        deleteSentenceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    deleteSentence();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        translateSentenceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    translateSelectedSentence();
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        sentenceButtons.add(addSentenceButton);
        sentenceButtons.add(editSentenceButton);
        sentenceButtons.add(deleteSentenceButton);
        sentenceButtons.add(translateSentenceButton); // 将翻译按钮添加到界面

        // 普通用户只能看到添加按钮，管理员可以看到所有按钮
        if (!isAdmin) {
            editSentenceButton.setVisible(false);
            deleteSentenceButton.setVisible(false);
        }

        sentenceInputPanel.add(sentenceInputFields);
        sentenceInputPanel.add(sentenceButtons);
        bottomPanelSentence = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 使用流式布局从左侧对齐
        jLabelSentence = new JLabel("总记录数: " + getTotalRecordCountSentence());
        bottomPanelSentence.add(jLabelSentence);
        sentenceInputPanel.add(bottomPanelSentence);
        sentencePanel.add(sentenceInputPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("例句管理", sentencePanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(userLabel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        SentencePagenator();
        add(mainPanel);
        // 添加检索面板
        JPanel searchPanel = new Search().createSearchPanel();
        tabbedPane.addTab("检索", searchPanel);

        setVisible(true);


    }

    private void WordPagenator() throws SQLException {  //添加单词分页按钮

        if (getTotalRecordCount() > rowsPerPage) {
            if(prevPageButton==null) {
                prevPageButton = new JButton("上一页");
                prevPageButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (currentPage > 1) {
                            currentPage--;
                            try {
                                FlushTable();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }
            bottomPanel.add(prevPageButton);

            if(nextPageButton==null) {
                nextPageButton = new JButton("下一页");
                nextPageButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        currentPage++;
                        try {
                            FlushTable();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            bottomPanel.add(nextPageButton);
        } else {
            if (prevPageButton != null) {
                bottomPanel.remove(prevPageButton);
            }
            if (nextPageButton != null) {
                bottomPanel.remove(nextPageButton);
            }

            repaint();
        }
    }

    private void SentencePagenator() throws SQLException {  //添加例句分页按钮

        if (getTotalRecordCountSentence() > rowsPerPageSentence) {
            prevPageButtonSentence = new JButton("上一页");
            prevPageButtonSentence.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentPageSentence > 1) {
                        currentPageSentence--;
                        try {
                            FlushSentenceTable();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            bottomPanelSentence.add(prevPageButtonSentence);

            nextPageButtonSentence = new JButton("下一页");
            nextPageButtonSentence.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentPage++;
                    try {
                        FlushSentenceTable();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            bottomPanelSentence.add(nextPageButtonSentence);
        } else {
            if (prevPageButtonSentence != null) {
                bottomPanelSentence.remove(prevPageButtonSentence);
            }
            if (nextPageButtonSentence != null) {
                bottomPanelSentence.remove(nextPageButtonSentence);
            }

            repaint();
        }
    }

    private void updateTotalRecordLabel() throws SQLException {   // 更新单词总记录数标签
        jLabel.setText("总记录数: " + getTotalRecordCount()); // 更新总记录数文本
        bottomPanel.revalidate(); // 重新验证布局
        bottomPanel.repaint(); // 重新绘制界面

        }
    private void updateSentenceTotalRecordLabel() throws SQLException {   // 更新例句总记录数标签
        jLabelSentence.setText("总记录数: " + getTotalRecordCountSentence()); // 更新总记录数文本
        bottomPanel.revalidate(); // 重新验证布局
        bottomPanel.repaint(); // 重新绘制界面

    }


    private int getTotalRecordCount() throws SQLException {  // 获取单词表的 总记录数
        int totalCount = 0;
        try {
            Connection connection = DB.GetConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM words where status=1");
            if (rs.next()) {
                totalCount = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            connection.close();
        }
        return totalCount;
    }

    private int getTotalRecordCountSentence() throws SQLException {  // 获取单词表的 总记录数
        int totalCount = 0;
        try {
            Connection connection = DB.GetConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM sentences where status=1");
            if (rs.next()) {
                totalCount = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            connection.close();
        }
        return totalCount;
    }

    private void translateSelectedWord() throws UnsupportedEncodingException {
        int selectedRow = wordTable.getSelectedRow();
        if (selectedRow != -1 ) {
            String word = (String) wordTableModel.getValueAt(selectedRow, 0);
            // Implement translation logic here, e.g., using an API like Baidu Translate
            // Display translation in a dialog
            String translation = BaiDUAPI.translate(word);
            JOptionPane.showMessageDialog(this, "单词翻译结果:\n" + translation, "单词翻译", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "请选择要翻译的单词", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void translateSelectedSentence() throws UnsupportedEncodingException {
        int selectedRow = sentenceTable.getSelectedRow();
        if (selectedRow != -1) {
            String sentence = (String) sentenceTableModel.getValueAt(selectedRow, 0);
            // 调用翻译方法并显示翻译内容的对话框
            String translation =BaiDUAPI.translate(sentence);
            JOptionPane.showMessageDialog(this, "例句 '" + sentence + "' 的翻译是:\n" + translation, "例句翻译", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "请选择要翻译的例句", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Boolean IsDuplicate(String word) throws SQLException {  // 判断单词是否重复

        String sql = "select * from words where word=?";
        try {
            connection = DB.GetConnection();
            PreparedStatement smt = connection.prepareStatement(sql);
            smt.setString(1, word);
            ResultSet rs = smt.executeQuery();
            return rs.next();  //有结果返回true
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            connection.close();
        }
    }

    private void FlushTable() throws SQLException {  // 刷新单词表
        wordTableModel.setRowCount(0);  // 清空表格
        try {
            connection = DB.GetConnection();
            PreparedStatement smt = connection.prepareStatement("SELECT * FROM words where status=1 LIMIT ?,? ");
            smt.setInt(1,(currentPage-1)*rowsPerPage);
            smt.setInt(2,rowsPerPage);
            ResultSet rs = smt.executeQuery();
            while (rs.next()) {

                String word = rs.getString("word");
                String addTime = rs.getString("add_time");
                String updateTime = rs.getString("update_time");
                String addUser = rs.getString("username");
                wordTableModel.addRow(new Object[]{word, addTime, updateTime, addUser});
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            connection.close();
        }
    }


    private void FlushSentenceTable() throws SQLException {  // 刷新例句表
        sentenceTableModel.setRowCount(0);  // 清空表格
        try {
            connection = DB.GetConnection();
            PreparedStatement smt = connection.prepareStatement("SELECT * FROM sentences where status=1");
            ResultSet rs = smt.executeQuery();
            while (rs.next()) {

                String sentence = rs.getString("sentence");
                String addTime = rs.getString("add_time");
                String updateTime = rs.getString("update_time");
                String addUser = rs.getString("username");
                sentenceTableModel.addRow(new Object[]{sentence, addTime, updateTime, addUser});

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            connection.close();
        }
    }

    private void addWord() throws SQLException {  // 添加单词

        String word = wordField.getText().trim();
        if (!word.isEmpty()) {
            try {

                connection = DB.GetConnection();

                PreparedStatement smt = connection.prepareStatement("INSERT INTO words (word,username,add_time,status) VALUES (?, ?, ?,1)");
                smt.setString(1, word);
                smt.setString(2, currentUser);
                smt.setString(3, LocalDateTime.now().format(DATE_FORMAT));
                if (IsDuplicate(word)) {
                    JOptionPane.showMessageDialog(this, "单词已存在", "提示", JOptionPane.WARNING_MESSAGE);
                } else {
                    smt.execute();
                    JOptionPane.showMessageDialog(this, "单词" + word + "添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                    updateTotalRecordLabel();
                    WordPagenator();
                 }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            FlushTable();
            connection.close();
            wordField.setText("");

        } else {
            JOptionPane.showMessageDialog(this, "请输入单词", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editWord() throws SQLException {  // 修改单词
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "普通用户无权修改单词", "权限错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = wordTable.getSelectedRow();
        if (selectedRow != -1) {
            // 获取当前行的数据
            String currentWord = (String) wordTableModel.getValueAt(selectedRow, 0);
            String currentAddTime = (String) wordTableModel.getValueAt(selectedRow, 1);
            String currentUpdateTime = (String) wordTableModel.getValueAt(selectedRow, 2);
            String currentAddUser = (String) wordTableModel.getValueAt(selectedRow, 3);

            // 创建输入字段并设置默认值
            JTextField wordField = new JTextField(currentWord);
            JTextField addTimeField = new JTextField(currentAddTime);
            JTextField updateTimeField = new JTextField(currentUpdateTime);
            JTextField addUserField = new JTextField(currentAddUser);

            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("单词:"));
            panel.add(wordField);
            panel.add(new JLabel("添加时间:"));
            panel.add(addTimeField);
            panel.add(new JLabel("修改时间:"));
            panel.add(updateTimeField);
            panel.add(new JLabel("添加人:"));
            panel.add(addUserField);

            int result = JOptionPane.showConfirmDialog(this, panel, "修改单词", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String updatedWord = wordField.getText().trim();
                String updatedAddTime = addTimeField.getText().trim();
                String updatedUpdateTime = updateTimeField.getText().trim();
                String updatedAddUser = addUserField.getText().trim();

                if (!updatedWord.isEmpty() && !updatedAddTime.isEmpty() && !updatedUpdateTime.isEmpty() && !updatedAddUser.isEmpty()) {
                    try (Connection connection = DB.GetConnection()) {
                        try (PreparedStatement smt = connection.prepareStatement(
                                "UPDATE words SET word = ?, add_time = ?, update_time = ?, username = ? WHERE word = ?")) {
                            smt.setString(1, updatedWord);
                            smt.setString(2, updatedAddTime);
                            smt.setString(3, updatedUpdateTime);
                            smt.setString(4, updatedAddUser);
                            smt.setString(5, currentWord);
                            smt.executeUpdate();
                            JOptionPane.showMessageDialog(WordManagerFrame.this, "修改成功");
                            FlushTable();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } finally {
                        connection.close();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "所有字段都是必填项", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要修改的单词", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void deleteWord() throws SQLException {  //删除单词
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "普通用户无权删除单词", "权限错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        getTotalRecordCount();
        int[] selectedRows = wordTable.getSelectedRows();
        if (selectedRows.length > 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要删除选中的单词吗？", "确认删除", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection connection = DB.GetConnection()) {
                    connection.setAutoCommit(false); // 开启事务

                    try (PreparedStatement smt = connection.prepareStatement("update words set status=0 WHERE word = ?")) {
                        for (int selectedRow : selectedRows) {
                            String word = (String) wordTableModel.getValueAt(selectedRow, 0);
                            smt.setString(1, word);
                            smt.addBatch();
                        }
                        smt.executeBatch();
                    }

                    connection.commit(); // 提交事务
                    JOptionPane.showMessageDialog(this, "选中的单词已成功删除", "删除成功", JOptionPane.INFORMATION_MESSAGE);
                    FlushTable();
                    updateTotalRecordLabel();
                    WordPagenator();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "删除过程中发生错误", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要删除的单词", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void addSentence() throws SQLException { //添加例句
        String sentence = sentenceField.getText().trim();
        if (!sentence.isEmpty()) {
            try {

                connection = DB.GetConnection();

                PreparedStatement smt = connection.prepareStatement("INSERT INTO sentences (sentence,username,add_time,status) VALUES (?, ?, ?,1)");
                smt.setString(1, sentence);
                smt.setString(2, currentUser);
                smt.setString(3, LocalDateTime.now().format(DATE_FORMAT));

                smt.execute();
                JOptionPane.showMessageDialog(this, "例句添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            FlushSentenceTable();
           updateSentenceTotalRecordLabel();
            SentencePagenator();
            connection.close();
            sentenceField.setText("");

        } else {
            JOptionPane.showMessageDialog(this, "请输入例句", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editSentence() throws SQLException {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "普通用户无权修改例句", "权限错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = sentenceTable.getSelectedRow();
        if (selectedRow != -1) {
            // 获取当前行的数据
            String currentSentence = (String) sentenceTableModel.getValueAt(selectedRow, 0);
            String currentAddTime = (String) sentenceTableModel.getValueAt(selectedRow, 1);
            String currentUpdateTime = (String) sentenceTableModel.getValueAt(selectedRow, 2);
            String currentAddUser = (String) sentenceTableModel.getValueAt(selectedRow, 3);

            // 创建输入字段并设置默认值
            JTextField wordField = new JTextField(currentSentence);
            JTextField addTimeField = new JTextField(currentAddTime);
            JTextField updateTimeField = new JTextField(currentUpdateTime);
            JTextField addUserField = new JTextField(currentAddUser);

            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("例句:"));
            panel.add(wordField);
            panel.add(new JLabel("添加时间:"));
            panel.add(addTimeField);
            panel.add(new JLabel("修改时间:"));
            panel.add(updateTimeField);
            panel.add(new JLabel("添加人:"));
            panel.add(addUserField);

            int result = JOptionPane.showConfirmDialog(this, panel, "修改单词", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String updatedSentence = wordField.getText().trim();
                String updatedAddTime = addTimeField.getText().trim();
                String updatedUpdateTime = updateTimeField.getText().trim();
                String updatedAddUser = addUserField.getText().trim();

                if (!updatedSentence.isEmpty() && !updatedAddTime.isEmpty() && !updatedUpdateTime.isEmpty() && !updatedAddUser.isEmpty()) {
                    try (Connection connection = DB.GetConnection()) {
                        try (PreparedStatement smt = connection.prepareStatement(
                                "UPDATE sentences SET sentence = ?, add_time = ?, update_time = ?, username = ? WHERE sentence = ?")) {
                            smt.setString(1, updatedSentence);
                            smt.setString(2, updatedAddTime);
                            smt.setString(3, updatedUpdateTime);
                            smt.setString(4, updatedAddUser);
                            smt.setString(5, currentSentence);
                            smt.executeUpdate();
                            JOptionPane.showMessageDialog(WordManagerFrame.this, "修改成功");
                            FlushSentenceTable();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } finally {
                        connection.close();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "所有字段都是必填项", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要修改的例句", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSentence() throws SQLException {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "普通用户无权删除例句", "权限错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int[] selectedRows = sentenceTable.getSelectedRows();
        if (selectedRows.length > 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要删除选中的例句吗？", "确认删除", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection connection = DB.GetConnection()) {
                    connection.setAutoCommit(false); // 开启事务

                    try (PreparedStatement smt = connection.prepareStatement("update sentences set status=0 WHERE sentence = ?")) {
                        for (int selectedRow : selectedRows) {
                            String sentence = (String) sentenceTableModel.getValueAt(selectedRow, 0);
                            smt.setString(1, sentence);
                            smt.addBatch();
                        }
                        smt.executeBatch();
                    }

                    connection.commit(); // 提交事务
                    JOptionPane.showMessageDialog(this, "选中的例句已成功删除", "删除成功", JOptionPane.INFORMATION_MESSAGE);
                    FlushSentenceTable();
                   updateSentenceTotalRecordLabel();
                    SentencePagenator();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "删除过程中发生错误", "错误", JOptionPane.ERROR_MESSAGE);
                }finally {
                    connection.close();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要删除的例句", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }


}
