package com.lzhq.miaosha.controller;


import com.lzhq.miaosha.domain.User;
import com.lzhq.miaosha.rabbitmq.MQSender;
import com.lzhq.miaosha.redis.RedisService;
import com.lzhq.miaosha.redis.UserKey;
import com.lzhq.miaosha.result.CodeMsg;
import com.lzhq.miaosha.result.Result;
import com.lzhq.miaosha.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    RedisService redisService;

    @Autowired
    UserService userService;

    @Autowired
    MQSender sender;

    @RequestMapping("/hello")
    @ResponseBody
   public Result<String> home(){
        return Result.success("hello world");
    }

    @RequestMapping("/boolean")
    @ResponseBody
    public Result<Boolean> testBoolean() {return Result.success(true);}

    @RequestMapping("/error")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SESSION_ERROR);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }

    @RequestMapping("redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("lee");
        boolean flag = redisService.set(UserKey.getById,""+1,user);
        if(!flag){
            return Result.error(CodeMsg.SERVER_ERROR);
        }
        return Result.success(true);
    }

    @RequestMapping("db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("db/add")
    @ResponseBody
    public Result<Boolean> dbAdd(){
        User user = new User();
        user.setId(2);
        user.setName("weijing");
        int n = userService.addUser(user);
        if(n<=0){
            return Result.error(CodeMsg.SERVER_ERROR);
        }
        return Result.success(true);
    }

    /*
    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
        sender.send("hello, rabbitmq!");
        return Result.success("hello world");
    }

    @RequestMapping("mq/topic")
    @ResponseBody
    public Result<String> topic() {
        sender.sendTopic("hello rabbitmq topic!");
        return Result.success("hello topic!");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout() {
        sender.sendFanout("hello, rabbitmq fanout!");
        return Result.success("hello,fanout!");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header() {
        sender.sendHeader("hello, rabbitmq header!");
        return Result.success("hello,header!");
    }
    */
}

