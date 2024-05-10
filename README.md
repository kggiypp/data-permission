# 数据权限插件
<br/>


## 核心功能说明
该项目是基于mybatis框架扩展插件的形式开发的，核心处理逻辑借鉴于前辈大佬的pagehelper分页插件，这里再次表示致敬！

核心处理逻辑简言之就是在原始sql提交数据库之前做一次拦截修改，加上权限的过滤条件，形式如下：  
`select t.* from (原始sql) t where t.权限字段 in (权限值集合)`  
当使用者项目同时存在分页插件时，该数据权限插件会在分页插件之前执行。


## maven项目引入使用
在pom.xml文件中添加如下依赖：

```xml
<dependency>
  <groupId>io.github.kggiypp</groupId>
  <artifactId>data-permission-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```
