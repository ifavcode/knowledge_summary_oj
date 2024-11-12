# knowledge_summary_oj

知识宝典在线OJ系统 —— 一个博客系统与OJ判题系统结合的系统

一款基于Judge0开发的博客+在线判题系统

博客结合OJ岂不美哉？

## 目前有的功能

- [x] 登录、注册、修改个人信息
- [x] 博客日浏览量分析
- [x] 文章功能 + 评论功能 + 点赞
- [x] 浏览量统计
- [x] 个人发布博客量统计
- [x] 推荐博客
- [x] 博客智能搜索
- [x] 签到功能
- [x] 实时判题
- [x] 统计全栈AC与提交比例
- [x] 问题标签
- [x] 根据题目编码搜索题目
- [x] 一套完整的创建题目MD编译器
- [x] 用户个人文件系统
- [x] 文件选择器—便捷添加测试样例
- [x] PC、移动适配

## 技术栈

**1）前端**

- Node.js v20.15.0
- Vue3
- TypeScript
- Ant Design For Vue 组件库
- monaco editor 代码编辑器
- vditor MD编辑器

**2）后端**

- Java 22
- Spring Boot 3.2.7
- Spring Cloud Alibaba 2023.0.1.2
- Spring Cloud 2023.0.3
- MySQL 
- Redis 
- RocketMQ
- Nacos
- OkHttp
- OSS

**3）**判题

基于Judge0

## 文件夹

knowledge-summary-front - 前端

ks-common - 公共模块

ks-flux-main - 基于spring flux的模块

ks-mvc-main - 基于spring mvc的模块

ks-gateway - 网关

## 快速启动

1. 启动nacos、在项目根目录下pom.xml中的profiles填入nacos对应的地址
2. 启动mysql、redis、rocketmq，在ks-mvc-main对应的application-dev.properties填入对应的地址
3. 启动judge0，在ks-mvc-main对应的application-dev.properties填入对应的地址
4. （可选）在ks-mvc-main对应的application-dev.properties填入OSS信息
5. 前端下载依赖文件，根据`修改按钮空白.txt`指示，删除对应文件
6. 启动、启动

## 项目展示

### 博客



![image-20241111172130277](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111172130277.png)

![image-20241111172220050](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111172220050.png)

![image-20241111172412191](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111172412191.png)

![image-20241111172458055](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111172458055.png)

## OJ

![image-20241111174239748](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111174239748.png)

![image-20241111174314788](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111174314788.png)

![image-20241111174325454](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111174325454.png)

![image-20241111174418717](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111174418717.png)

![image-20241111174448396](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111174448396.png)

![image-20241111174505393](https://www.guetzjb.cn/assets_other/2024-11-08/image-20241111174505393.png)
