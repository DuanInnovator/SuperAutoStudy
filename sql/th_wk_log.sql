/*
 Navicat Premium Dump SQL

 Source Server         : 8.136.123.221
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 8.136.123.221:3306
 Source Schema         : tihai

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 21/04/2025 16:07:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for th_wk_log
-- ----------------------------
DROP TABLE IF EXISTS `th_wk_log`;
CREATE TABLE `th_wk_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `login_account` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登陆账号',
  `course_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `current_chapter_index` int(11) NULL DEFAULT 0 COMMENT '当前章节id',
  `current_job_id` int(11) NULL DEFAULT 0 COMMENT '当前任务点id',
  `current_job` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '当前任务点',
  `current_progress` decimal(10, 2) NULL DEFAULT NULL COMMENT '实时进度',
  `status` int(1) NULL DEFAULT NULL COMMENT ' 0-待处理，1-进行中，2-队列中，3-考试中，4-已完成，5-异常',
  `error_message` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `machine_num` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所处机器编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2025032717175912002 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '网课进度日志表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
