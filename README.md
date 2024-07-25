Hedis，基于Java和Netty开发的Redis，完成了绝⼤部分Redis6.0的功能。主要功能如下:
实现了5种基本数据类型（String、hash、list、set、zset）和3种特殊类型（geog、hplog、bitmap），选⽤⾼效的数据结构⼆次封装
Netty框架实现NIO异步，多线程处理客⼾端连接提⾼性能，单线程池解析命令保证原⼦性。
实现了定时异步的RDB持久化⽅式，提⾼性能。AOF⽇志⽤于集群的同步。实现了定期删除+惰性删除的数据过期策略，实现了LRU内
存淘汰机制。
配置使⽤yaml格式，相⽐redis.conf可读性更强，实现了类似Spring的命令⾏和系统环境变量加载，对Yaml进⾏增强实现嵌套解析枚举
类型。
⾃定义netty编码器,采⽤kyro对Java对象进⾏序列化,⼿动register class提⾼性能。使⽤ThreadLocal来避免kryo线程不安全问题。
命令解析器Parser使⽤策略模式，并且内部使⽤模版⽅法进⾏解析流程，可轻松对解析器进⾏扩展，通过@command注解配合反射
invoke来执⾏命令，提⾼开发效率
对进⾏netty的客⼾端和服务端组件⼆次封装，实现初始化/启动/断线重连/关闭，减少冗余代码，关注核⼼逻辑的实现。
实现了2种⾼可⽤性的集群，主从复制集群和Cluster分⽚集群
maven配置，打包成jar可以独⽴运⾏server，并且开发了hedis-springboot-starter

优化记录
1.aof优化，非修改值命令不记录（避免 get ttl keys等耗时命令）
2.数据存储的设计 Map<String, HedisData>
3.策略模式，运行在支持Epoll的系统上使用EpollEventLoopGroup
4.粘包问题 kryo序列化 Buffer underflow.
5.主节点发送数据到从节点，但是接受到的list都是空的
Queue<HedisDataChange> list = dataManager.getQueue();
// 主节点向从节点发送数据
if (!list.isEmpty() && clusterManager.isMaster() && channelFuture != null) {
dataManager.offerUpdateData();
channelFuture.channel().writeAndFlush(list);
list.clear();
}