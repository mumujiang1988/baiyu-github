/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : 118.178.144.159:3307
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 20/03/2026 13:39:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for country
-- ----------------------------
DROP TABLE IF EXISTS `country`;
CREATE TABLE `country`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键ID（原始ID）',
  `name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '国家英文名',
  `name_zh` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '国家中文名',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_country_en`(`name_en` ASC) USING BTREE,
  UNIQUE INDEX `uk_country_zh`(`name_zh` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '国家基础资料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of country
-- ----------------------------
INSERT INTO `country` VALUES ('46a524cf-5797-4e46-bd0a-7203fc426d9c', 'China', '中国', 1, '2026-01-21 15:30:15', '2026-01-21 15:30:15');
INSERT INTO `country` VALUES ('60a36bd2301873', 'Romania', '罗马尼亚', 1, '2026-01-21 15:33:21', '2026-01-21 15:33:21');
INSERT INTO `country` VALUES ('60e7ba25d450c8', 'Australia', '澳大利亚', 1, '2026-01-21 15:33:21', '2026-01-21 15:33:21');
INSERT INTO `country` VALUES ('60e7bb69d450ee', 'united kindom', '英国', 1, '2026-02-07 10:53:30', '2026-02-07 10:53:30');
INSERT INTO `country` VALUES ('60e7d402d45475', 'Hongkong', '香港', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7d426d45477', 'Taiwan', '台湾', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7d9f3d454c8', 'Poland', '波兰', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7da89d45544', 'Gdynia', 'Gdynia', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7dd21d455e2', 'Germany', '德国', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7e4f0d456f7', 'Lebanon', '黎巴嫩', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7e566d45700', 'Vietnam', '越南', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7eb09d457d4', 'Colombia', '哥伦比亚', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7ec98d457d9', 'South Africa', '南非', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7f9e3d4592a', 'Russia', '俄罗斯', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e7ff63d4593f', 'India', '印度', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e80134d45953', 'Spain', '西班牙', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e8066fd45997', 'Ecuador', '厄瓜多尔', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e8083dd459f2', 'Jamaica', '牙买加', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e80a3ad459f4', 'Switzerland', '瑞士', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60e80e18d459f6', 'France', '法国', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ebcfd188ffed', 'Costa Rica', '哥斯达黎加', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ebded58904b3', 'Serbia', '塞尔维亚', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ebeb39890836', 'Bulgaria', '保加利亚', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ebef058909eb', 'Brazil', '巴西', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ec09608914ea', 'Chile', '智利', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ed08311a83db', 'Indonesia', '印度尼西亚', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ed35d8db6b97', 'Hungary', '匈牙利', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ed45b9db729c', 'Sri Lanka', '斯里兰卡', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ed50d5db745d', 'Cambodia', '柬埔寨', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ed5247db746f', 'Canada', '加拿大', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ee59c7f3f746', 'Japan', '日本', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ee8d37f3fa56', 'Ukraine', '乌克兰', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ee8d4ff3fa58', 'Uzbekistan', '乌兹别克斯坦', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ee8d60f3fa5a', 'Uruguay', '乌拉圭', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ee8d71f3fa5c', 'Uganda', '乌干达', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60ee9837f3faad', 'Bahrain', '巴林', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60eea32af3fac9', 'New Caledonia', '新喀里多尼亚', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('60f24bf02dae22', 'Thailand', '泰国', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('61124623620d7d', 'Austria', '奥地利', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('61139350aa0855', 'Italy', '意大利', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('6114d602e8be24', 'New Zealand', '新西兰', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('6114d9d9e8be80', 'Trinidad and Tobago', '特立尼达和多巴哥', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('6114e696e8bf15', 'Iraq', '伊拉克', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('6114e740e8bf17', 'Philippines', '菲律宾', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('61161d2928d9a0', 'Luxembourg', '卢森堡', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('6116232428da01', 'Mexico', '墨西哥', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('611628bb28daac', 'Fiji', '斐济', 1, '2026-01-21 15:06:42', '2026-01-21 15:06:42');
INSERT INTO `country` VALUES ('6124bbbca13722', 'Israel', '以色列', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('612de03b8c1ff8', 'Netherlands', '荷兰', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('6135ca9b47c00b', 'Denmark', '丹麦', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('6135dbc047c5f1', 'Saudi Arabia', '沙特阿拉伯', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('61418cbaa337bc', 'Slovenia', '斯洛文尼亚', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('614beb738d8ae9', 'Maldives', '马尔代夫', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('614c41d78d9f5a', 'Czech Republic', '捷克', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('614d28b008977f', 'Greece', '希腊', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('614d831b08a1be', 'Sweden', '瑞典', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('614d86ec08a1fc', 'Algeria', '阿尔及利亚', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('614d942608a208', 'Panama', '巴拿马', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('615023d1d1bbc5', 'Argentina', '阿根廷', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('6152b65f5e864d', 'Slovakia', '斯洛伐克', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('6152d79c5e8f31', 'Portugal', '葡萄牙', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('615583233de4ff', 'Ireland', '爱尔兰', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('615cfe8eaafeae', 'Paraguay', '巴拉圭', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('615ea2dd1ad44a', 'Belgium', '比利时', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('615eb9311ad692', 'Peru', '秘鲁', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('61600a6ad091e2', 'Turkey', '土耳其', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('616139fde20fce', 'Congo', '刚果共和国', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('6163dfa688fc44', 'Lithuania', '立陶宛', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('6163f33888fe9f', 'Guatemala', '危地马拉', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('61664862fc0f9a', 'Bosnia and Herzegovina', '波斯尼亚和黑塞哥维那', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('6167f11e3cf603', 'Myanmar', '缅甸', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('61691862eb4456', 'Norway', '挪威', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('616d1fa2ba61d1', 'Iran', '伊朗', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('616e5738f63f1d', 'Honduras', '洪都拉斯', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('617665aae504bd', 'Rwanda', '卢旺达', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('617a01a744391c', 'Qatar', '卡塔尔', 1, '2026-01-21 15:12:45', '2026-01-21 15:12:45');
INSERT INTO `country` VALUES ('61b1591e95017a', 'Mauritania', '毛里塔尼亚', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('61ce655fed7685', 'Armenia', '亚美尼亚', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('61ce7acbed7811', 'Malta', '马耳他', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('62205ea027d716', 'Angola', '安哥拉', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('626659dddd8a5f', 'Kenya', '肯尼亚', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('62a1a0633ef486', 'Pakistan', '巴基斯坦', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('62e3aa51b46a8b', 'Nigeria', '尼日利亚', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('62f46980b2b7fb', 'Azerbaijan', '阿塞拜疆', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('62f46bf7b2b8cf', 'Belarus', '白俄罗斯', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('63203e0c708773', 'Singapore', '新加坡', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('6320433970899d', 'Afghanistan', '阿富汗', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('632044a6708a6c', 'Zambia', '赞比亚', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('632044e6708a84', 'Yemen', '也门', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('63204538708a9c', 'Vatican City', '梵蒂冈', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('6320458b708ab4', 'Zaire', '扎伊尔', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('632045c7708acc', 'Yugoslavia', '南斯拉夫', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('632046b7708ae4', 'Samoa', '萨摩亚', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('63204aea708c42', 'Mongolia', '蒙古', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('637ec576ad39eb', 'Oman', '阿曼', 1, '2026-01-21 15:43:58', '2026-01-21 15:43:58');
INSERT INTO `country` VALUES ('6613b3e3393fea', 'MALAYSIA', '马来西亚', 1, '2026-03-16 11:04:43', '2026-03-16 11:04:43');
INSERT INTO `country` VALUES ('6699d3e8e011cb', 'South Korea', '韩国', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('66a450d1739184', 'Gabon', '加蓬', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('66a4570773942c', 'Kazakhstan', '哈萨克斯坦', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('66b6beaf66cb51', 'Senegal', '塞内加尔', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('676243d208af50', 'Libya', '利比亚', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('6762441b08af52', 'Bangladesh', '孟加拉国', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('677373ca0a09c1', 'Niger', '尼日尔', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('67a2eca60ce6c4', 'Botswana', '博茨瓦纳', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('67a55e7e0d19c0', 'Tunisia', '突尼斯', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('67a5adde0d2163', 'Malawi', '马拉维', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('67b806c60e6bd5', 'Mali', '马里', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('67c7f2440feae1', 'Liberia', '利比里亚', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('681c7b25181b82', 'French Polynesia', '法属波利尼西亚', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('6848f6f51ba228', 'Mozambique', '莫桑比克', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('6849148b9ab900', 'Côte d’Ivoire', '科特迪瓦', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('685e088b9de467', 'Guinea-Bissau', '几内亚比绍', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('68746619a010e1', 'Venezuela', '委内瑞拉', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('68882094a1bacf', 'Zimbabwe', '津巴布韦', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('688af5fda21ad6', 'Jordan', '约旦', 1, '2026-01-21 15:39:11', '2026-01-21 15:39:11');
INSERT INTO `country` VALUES ('f98a3ac14f0b4397a332b2353f1fc66c', 'America', '美国', 1, '2026-01-21 15:13:42', '2026-01-21 15:13:42');

SET FOREIGN_KEY_CHECKS = 1;
