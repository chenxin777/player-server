-- auto-generated definition
create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(256)                       null comment '用户名',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 not null comment '状态（0正常）',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 null comment '0-普通用户 1-管理员'
);

CREATE TABLE `tag`
(
    `id`         bigint   NOT NULL AUTO_INCREMENT,
    `tagName`    varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签名',
    `userId`     bigint                                  DEFAULT NULL COMMENT '用户id',
    `parentId`   bigint                                  DEFAULT NULL COMMENT '父标签id',
    `isParent`   tinyint                                 DEFAULT NULL COMMENT '0-不是 1-父标签',
    `createTime` datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   tinyint  NOT NULL                       DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniIdx_tagName` (`tagName`) USING BTREE,
    KEY `idx_userId` (`userId`) USING BTREE
) COMMENT ='标签';


create table team
(
    id           bigint auto_increment primary key,
    name         varchar(256)                       null comment '队伍名称',
    description  varchar(1024)                      null comment '描述',
    maxNum       int                                null comment '最大人数',
    expireTime   datetime                           null comment '过期时间',
    userId       bigint                             not null comment '用户id',
    password     varchar(512)                       null comment '密码',
    status       int      default 0                 not null comment '状态 0公开 1私有 2加密',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除'
) COMMENT ='队伍';

create table user_team
(
    id           bigint auto_increment primary key,
    joinTime   datetime  null comment '加入时间',
    userId       bigint                      not null comment '用户id',
    teamId       bigint                      not null comment '队伍id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
) COMMENT ='用户队伍关系';