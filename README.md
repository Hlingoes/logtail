# logtail
fork的[logtail](https://gitee.com/Jun_X/logtail)，按照阿里iLogtail设计实现一个日志监控服务，满足基本的日志发现与采集。

1. Filebeat, Logstash需要配合其他中间件和服务使用，没有那么大的业务量
2. 核心诉求：轻量级业务，解析日志，通过http发送日志到目标服务器或持久化到数据库
3. 用java开发，支持定制和插件开发，即可作为依赖jar包引入项目，也可以作为agent单独部署使用



#### 开发计划：

1. ##### [Logtail技术分享(一) : Polling + Inotify 组合下的日志保序采集方案](https://developer.aliyun.com/article/204554) 实现日志采集的关键功能点：

   如何发现一个文件?

   点位文件高可用

   如何识别一个文件?

   如何知道文件内容更新了?

   如何安全的释放文件句柄?

2. ##### 支持插件开发，内置日志解析入库demo和http发送接口



#### 参考文章：

##### 1.[Java开发如何写一个日志采集工具](https://www.jianshu.com/p/256cac7765b5)

##### 2.[日志采集中的关键技术分析](https://developer.aliyun.com/article/601754)