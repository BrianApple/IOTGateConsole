/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50640
Source Host           : localhost:3306
Source Database       : iotgatedb

Target Server Type    : MYSQL
Target Server Version : 50640
File Encoding         : 65001

Date: 2019-04-02 17:09:25
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for strategy
-- ----------------------------
DROP TABLE IF EXISTS `strategy`;
CREATE TABLE `strategy` (
  `id` varchar(64) NOT NULL,
  `pId` int(11) NOT NULL,
  `pName` varchar(255) NOT NULL,
  `isBigEndian` bit(1) NOT NULL,
  `beginHexVal` varchar(4) NOT NULL,
  `lengthFieldOffset` int(11) NOT NULL,
  `lengthFieldLength` int(11) DEFAULT NULL,
  `isDataLenthIncludeLenthFieldLenth` bit(1) NOT NULL,
  `exceptDataLenth` int(11) NOT NULL,
  `port` int(11) NOT NULL,
  `highControll` bit(1) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of strategy
-- ----------------------------
INSERT INTO `strategy` VALUES ('36a88e38-eda1-469f-9c11-f00fd104be35', '2', '默认规约02-误删', '', '-1', '0', '4', '\0', '0', '9812', '\0', null);
INSERT INTO `strategy` VALUES ('cc73d8ed-9d62-4e80-8e42-1b096819a03f', '1', '默认规约01-误删', '\0', '-1', '1', '2', '', '1', '9811', '\0', null);
