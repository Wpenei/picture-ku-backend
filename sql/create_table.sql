-- 数据库
create database if not exists picture_ku;
use picture_ku;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;
-- 添加用户邮箱
ALTER TABLE user
    ADD COLUMN email VARCHAR(256) NULL COMMENT '用户邮箱',
    ADD UNIQUE KEY uk_email (email);


-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

-- 添加图片审核字段
ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus  INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512)  NULL COMMENT '审核信息',
    ADD COLUMN reviewerId    BIGINT        NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime    DATETIME      NULL COMMENT '审核时间';

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);
-- 添加缩略图字段
ALTER TABLE picture
    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url',
    ADD INDEX idx_thumbnailUrl (thumbnailUrl);

-- 添加点赞数
ALTER TABLE picture
    ADD COLUMN likeCount bigint DEFAULT 0 NOT NULL COMMENT '点赞数';
-- 添加点赞数
ALTER TABLE picture
    ADD COLUMN commentCount bigint DEFAULT 0 NOT NULL COMMENT '评论数',
    ADD COLUMN shareCount   bigint DEFAULT 0 NOT NULL COMMENT '分享数',
    ADD COLUMN viewCount    bigint DEFAULT 0 NOT NULL COMMENT '浏览量';



-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    index idx_userId (userId),        -- 提升基于用户的查询效率
    index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;

-- 图片表中添加新列记录 空间id
ALTER TABLE picture
    ADD COLUMN spaceId bigint null comment '空间 id（为空表示公共空间）';

-- 创建索引
CREATE INDEX idx_spaceId ON picture (spaceId);

-- 图片表中添加新列记录 图片主色调
ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';

-- 在空间表中添加一个spaceType字段,用来标识私有空间和公共空间0,1,默认为0
ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型：0-私有空间 1-公共空间';
-- 并添加索引
CREATE INDEX idx_spaceType ON space (spaceType);

-- 创建一个空间成员表,其中包括空间id,用户id以及用户在空间中的角色,并对字段添加唯一索引和索引
-- 空间成员表
create table if not exists space_user
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                             not null comment '空间id',
    userId     bigint                             not null comment '用户id',
    spaceRole  int                                not null comment '用户在空间中的角色：0-浏览者 1-编辑者 2-管理员 3-创建人',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_spaceId_userId (spaceId, userId),
    index idx_spaceId (spaceId),
    index idx_userId (userId)
) comment '空间用户关联表' collate = utf8mb4_unicode_ci;

-- 分类表
CREATE TABLE category
(
    id           bigint AUTO_INCREMENT COMMENT '分类id'
        PRIMARY KEY,
    categoryName varchar(256)                       NOT NULL COMMENT '分类名称',
    categoryType tinyint  DEFAULT 0                 NOT NULL COMMENT '分类类型：0-图片分类 1-帖子分类',
    createTime   datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime     datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '分类编辑时间',
    updateTime   datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '分类更新时间',
    isDelete     tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除'
)
    COMMENT '分类' COLLATE = utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE if not exists tag
(
    id         bigint AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    tagName    varchar(256)                       NOT NULL COMMENT '标签名称',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime   datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除'
)
    COMMENT '标签' COLLATE = utf8mb4_unicode_ci;

-- 通用点赞表
CREATE TABLE like_record
(
    id            bigint AUTO_INCREMENT COMMENT 'id'   PRIMARY KEY,
    userId        bigint                               NOT NULL COMMENT '用户 ID',
    targetId      bigint                               NOT NULL COMMENT '被点赞内容的ID',
    targetType    tinyint                              NOT NULL COMMENT '内容类型：1-图片 2-帖子 3-空间',
    targetUserId  bigint                               NOT NULL COMMENT '被点赞内容所属用户ID',
    isLiked       boolean                              NOT NULL COMMENT '是否点赞',
    firstLikeTime datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '第一次点赞时间',
    lastLikeTime  datetime                             NOT NULL COMMENT '最近一次点赞时间',
    isRead        tinyint(1) DEFAULT 0                 NOT NULL COMMENT '是否已读（0-未读，1-已读）',
    CONSTRAINT uk_user_target
        UNIQUE (userId, targetId, targetType)
)
    COMMENT '通用点赞表' COLLATE = utf8mb4_unicode_ci;

-- 评论表
CREATE TABLE comment
(
    id              bigint AUTO_INCREMENT COMMENT 'id'   PRIMARY KEY,
    userId          bigint                               NOT NULL COMMENT '用户id',
    targetId        bigint                               NOT NULL COMMENT '评论目标ID',
    targetType      tinyint    DEFAULT 1                 NOT NULL COMMENT '评论目标类型：1-图片 2-帖子',
    targetUserId    bigint                               NOT NULL COMMENT '评论目标所属用户ID',
    content         text                                 NOT NULL COMMENT '评论内容',
    createTime      datetime   DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    parentCommentId bigint     DEFAULT 0                 NULL COMMENT '父评论ID,0表示顶级',
    isDelete        tinyint(1) DEFAULT 0                 NULL COMMENT '是否删除',
    likeCount       bigint     DEFAULT 0                 NULL COMMENT '点赞数',
    dislikeCount    bigint     DEFAULT 0                 NULL COMMENT '点踩数',
    isRead          tinyint(1) DEFAULT 0                 NOT NULL COMMENT '是否已读（0-未读，1-已读）'
)
    COMMENT '评论表' COLLATE = utf8mb4_unicode_ci;
ALTER TABLE comment
    ADD COLUMN commentCount bigint DEFAULT 0 COMMENT '评论数';

CREATE INDEX idx_target
    ON comment (targetId, targetType);

CREATE INDEX idx_targetUserId_isRead
    ON comment (targetUserId, isRead);