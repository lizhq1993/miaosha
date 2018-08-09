package com.lzhq.miaosha.controller;

import com.lzhq.miaosha.access.AccessLimit;
import com.lzhq.miaosha.domain.MiaoshaOrder;
import com.lzhq.miaosha.domain.MiaoshaUser;
import com.lzhq.miaosha.domain.OrderInfo;
import com.lzhq.miaosha.rabbitmq.MQSender;
import com.lzhq.miaosha.rabbitmq.MiaoshaMessage;
import com.lzhq.miaosha.redis.*;
import com.lzhq.miaosha.result.CodeMsg;
import com.lzhq.miaosha.result.Result;
import com.lzhq.miaosha.service.GoodsService;
import com.lzhq.miaosha.service.MiaoshaService;
import com.lzhq.miaosha.service.OrderService;
import com.lzhq.miaosha.util.MD5Util;
import com.lzhq.miaosha.util.UUIDUtil;
import com.lzhq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender sender;

    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    /**
     * 系统初始化，将商品库存数量加载到redis缓存中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList == null) {
            return;
        }
        for(GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsVoList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.deleteAll(OrderKey.getMiaoshaOrderByUidGid);
        redisService.deleteAll(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsVoList);
        return Result.success(true);
    }

    /**
     * 不采用消息队列
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    /*
    @RequestMapping(value="/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> miaosha(Model model, MiaoshaUser user,
                                     @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
    }
    */

    /**
     * 采用消息队列
     */
    @RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId")long goodsId, @PathVariable("path")String path) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        boolean over = localOverMap.get(goodsId);
        if(over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }

        // 预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
        if(stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        // 请求入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setMiaoshaUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0);
    }

    /**
     * orderId：秒杀成功 -1：秒杀失败 0：继续轮询
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user, @RequestParam("goodsId")Long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    /**
     * 隐藏秒杀地址，生成随机path
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, Model model, MiaoshaUser user,
                                         @RequestParam("goodsId")Long goodsId, @RequestParam("verifyCode")int verifyCode) {
        model.addAttribute("user", user);

        // 校验验证号是否正确
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, MiaoshaUser user, @RequestParam("goodsId")Long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
