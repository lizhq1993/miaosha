# SpringBoot项目
1. 技术选型
> 数据库：mysql

> 缓存：redis

> 消息队列： rabbitMQ

> 后端框架： SSM

> 前端 ： html css jquery ajax bootstrap

2. 使用SpringBoot简化配置，快速开发
3. 采用redis实现分布式session， 页面缓存，URL缓存， 限流防刷（控制一个时间段只能接受固定次数的访问）
4. 采用rabbitMQ接受请求，预减库存，后端根据处理能力去拉取请求进行异步处理

优化
---
1. 取消页面缓存和URL缓存，采用ajax技术实现前后端分离
2. 采用自定义注解和springboot提供的处理拦截器HandlerInterceptorAdapter，减少部分硬编码
