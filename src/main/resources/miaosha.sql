CREATE  table `user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT comment '主键id',
  `name` varchar(20) NOT NULL DEFAULT '' comment '姓名',
  primary key (`id`)
)ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE table `miaosha_user` (
  `id` bigint(20) NOT NULL comment '用户id，手机号码',
  `nickname` varchar(255) NOT NULL,
  `password` varchar(32) DEFAULT NULL comment 'MD5(MD5(密码明文+固定salt）+随机salt）',
  `salt` varchar(10) DEFAULT  NULL,
  `head` varchar(128) DEFAULT NULL comment '头像，云存储的id',
  `register_date` datetime DEFAULT NULL comment '注册时间',
  `last_login_date` datetime DEFAULT NULL,
  `login_count` int(11) DEFAULT '0' comment '登陆次数',
  PRIMARY KEY(`id`)
)ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT comment '商品id',
  `goods_name` varchar(16) DEFAULT NULL comment '商品名称',
  `goods_title` varchar(64) DEFAULT NULL comment '商品标题',
  `goods_img` varchar(64) DEFAULT NULL comment '商品图片地址',
  `goods_detail` longtext,
  `goods_price` decimal(10,2) DEFAULT '0.00',
  `goods_stock` int(11) DEFAULT '0' comment '商品库存,-1表示没有限制',
  PRIMARY KEY(`id`)
)ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `miaosha_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` bigint(20) DEFAULT NULL comment '商品id',
  `miaosha_price` decimal(10,2) DEFAULT '0.00',
  `stock_count` int(11) DEFAULT NULL comment '库存数量',
  `start_date` datetime DEFAULT NULL comment '秒杀开始时间',
  `end_date` datetime DEFAULT NULL comment '秒杀结束时间',
  PRIMARY KEY(id)
)ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `goods_id` bigint(20) DEFAULT NULL,
  `delivery_addr_id` bigint(20) DEFAULT NULL comment '收获地址id',
  `goods_name` varchar(16) DEFAULT NULL,
  `goods_count` int(11) DEFAULT '0',
  `goods_price` decimal(10,2) DEFAULT '0.00',
  `order_channel` tinyint(4) DEFAULT '0' comment '1pc,2android,3ios',
  `status` tinyint(4) DEFAULT '0' comment '订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成',
  `create_date` datetime DEFAULT NULL,
  `pay_date` datetime DEFAULT NULL,
  PRIMARY KEY(id)
)ENGINE = InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `miaosha_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `goods_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY(id)
)ENGINE = InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;