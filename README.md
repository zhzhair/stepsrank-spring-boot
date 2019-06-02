# stepsrank-spring-boot
spring boot集成redis和mongodb实现计步排名。
1.创建32个分表，用定时任务插入计步数据模拟用户上传步数；
2.项目启动初始化：将32个表的前200名记录插入mongodb的一个集合（表），清空后插入前200名记录，
并将第200名的步数（阈值）放到redis；
3.上传步数时，当用户的步数大于阈值时，就插入mongodb，否则不插入记录到mongodb；
4.用定时任务每隔10秒删除mongodb表中205名以后的记录；
5.用定时任务每隔1秒更新第200名的步数（阈值）到redis，同时将前200名记录放进redis的队列；
6.查询步数排名先到redis队列，查不到就去mongodb表查。
7.jmeter并发测试看查询性能。
