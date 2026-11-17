数据库文件目录
=============================================================

本目录用于存放 H2 文件模式的数据库文件：
    chatroom.mv.db        用户表 users + 消息表 messages

说明：
1. 程序第一次启动时（DBInitListener 初始化数据库），H2 会自动
   在本目录生成 chatroom.mv.db，请不要手动修改文件名。
2. 该文件就是“数据持久化”的载体：Tomcat 重启、重新部署 WAR 都不会
   影响它，所以重启后历史消息依然存在。
3. 想清空数据重新开始：先停掉 Tomcat，再删除 chatroom.mv.db，
   下次启动会自动重新建库建表。
4. 整个 db 目录被删掉也没关系，启动时 DBUtil 会自动重建目录。
5. 数据文件不建议提交到版本库（.gitignore 已忽略 *.mv.db）。

位置与连接参数在 src/main/resources/db.properties 中配置。
