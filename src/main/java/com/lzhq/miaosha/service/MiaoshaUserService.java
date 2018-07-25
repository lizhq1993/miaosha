package com.lzhq.miaosha.service;

import com.lzhq.miaosha.dao.MiaoshaUserDao;
import com.lzhq.miaosha.domain.MiaoshaUser;
import com.lzhq.miaosha.domain.User;
import com.lzhq.miaosha.exception.GlobalException;
import com.lzhq.miaosha.redis.MiaoshaUserKey;
import com.lzhq.miaosha.redis.RedisService;
import com.lzhq.miaosha.result.CodeMsg;
import com.lzhq.miaosha.result.Result;
import com.lzhq.miaosha.util.MD5Util;
import com.lzhq.miaosha.util.UUIDUtil;
import com.lzhq.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
public class MiaoshaUserService {

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    public static final String COOKIE_NAME_TOKEN = "token";

    /*
    访问数据库获得user
     */
    /*
    public MiaoshaUser getById(long id) {
        return miaoshaUserDao.getById(id);
    } */

    /*
    采用对象缓存技术获得user
     */
    public MiaoshaUser getById(Long id) {
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
        if(user != null) {
            return user;
        }

        //取数据库
        user = miaoshaUserDao.getById(id);
        if(user != null) {
            redisService.set(MiaoshaUserKey.getById, ""+id, user);
        }
        return user;
    }

    public Result<String> updatePassword(String token, Long id, String formPass) {
        MiaoshaUser user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        /*
        更新：先把数据存到数据库中，成功后，再把缓存失效
         */

        //更新数据库
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass,user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);

        //处理缓存
        redisService.delete(MiaoshaUserKey.getById, ""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token, token, user);
        return Result.success("密码更新成功");
    }

    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        if(user != null) {
            addCookie(response,token,user);
        }
        return user;
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        MiaoshaUser user = miaoshaUserDao.getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass,saltDB);
        if(!calcPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;
    }

    public boolean register(MiaoshaUser user) {
        if(user == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        // 判断用户是否被注册
        Long id = user.getId();
        MiaoshaUser isExist = miaoshaUserDao.getById(id);
        if(isExist != null) {
            throw new GlobalException(CodeMsg.USER_EXIST);
        }

        user.setRegisterDate(new Date());
        String dbPass = MD5Util.formPassToDBPass(user.getPassword(), "1a2b3c4d");
        user.setPassword(dbPass);
        miaoshaUserDao.addUser(user);
        return true;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
