package com.lzhq.miaosha.controller;


import com.lzhq.miaosha.domain.MiaoshaUser;
import com.lzhq.miaosha.domain.OrderInfo;
import com.lzhq.miaosha.redis.RedisService;
import com.lzhq.miaosha.result.CodeMsg;
import com.lzhq.miaosha.result.Result;
import com.lzhq.miaosha.service.GoodsService;
import com.lzhq.miaosha.service.MiaoshaService;
import com.lzhq.miaosha.service.OrderService;
import com.lzhq.miaosha.vo.GoodsVo;
import com.lzhq.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user, @RequestParam("orderId")Long orderId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        OrderInfo order = orderService.getOrderById(orderId);
        if(order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }

        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
