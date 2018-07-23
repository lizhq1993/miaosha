package com.lzhq.miaosha.controller;

import com.lzhq.miaosha.domain.MiaoshaUser;
import com.lzhq.miaosha.redis.GoodsKey;
import com.lzhq.miaosha.redis.RedisService;
import com.lzhq.miaosha.result.Result;
import com.lzhq.miaosha.service.GoodsService;
import com.lzhq.miaosha.service.MiaoshaUserService;
import com.lzhq.miaosha.vo.GoodsDetailVo;
import com.lzhq.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

  /*  @RequestMapping("/to_list")
    public String list(HttpServletResponse response, Model model, @CookieValue(value = MiaoshaUserService.COOKIE_NAME_TOKEN, required = false)String cookieToken,
                       @RequestParam(value = MiaoshaUserService.COOKIE_NAME_TOKEN, required = false)String paramToken) {
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return "login";
        }
        String token = StringUtils.isEmpty(cookieToken)? paramToken: cookieToken;
        MiaoshaUser user = miaoshaUserService.getByToken(response, token);
        model.addAttribute("user",user);
        return "goods_list";
    }*/

  /*
  传统向后台请求页面方法
   */
  /*
  @RequestMapping("/to_list")
    public String list(Model model, MiaoshaUser user) {
      model.addAttribute("user",user);
      List<GoodsVo> goods = goodsService.listGoodsVo();
      model.addAttribute("goodsList",goods);
      return "goods_list";
  } */

  /*
  采用页面缓存技术
   */
  @RequestMapping(value = "/to_list", produces = "text/html")
  @ResponseBody
  public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) {
      model.addAttribute("user",user);
      // 取缓存
      String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
      if(!StringUtils.isEmpty(html)) {
          return html;
      }

      //手动渲染
      List<GoodsVo> goodsList = goodsService.listGoodsVo();
      model.addAttribute("goodsList",goodsList);
      SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
              model.asMap(), applicationContext);
      html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
      if(StringUtils.isEmpty(html)) {
          redisService.set(GoodsKey.getGoodsList, "", html);
      }
      return html;
  }


    /*
    传统向后台请求页面方法
     */
    /*
  @RequestMapping("/to_detail/{goodsId}")
    public String detail(Model model, MiaoshaUser user, @PathVariable("goodsId")Long goodsId) {
      model.addAttribute("user",user);
      GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
      model.addAttribute("goods",goods);

      long startAt = goods.getStartDate().getTime();
      long endAt = goods.getEndDate().getTime();
      long now = System.currentTimeMillis();

      int miaoshaStatus = 0;
      int remainSeconds = 0;

      if(now < startAt) {
          miaoshaStatus = 0;
          remainSeconds = (int)((startAt-now)/1000);
      }else if (now > endAt) {
          miaoshaStatus = 2;
          remainSeconds = -1;
      }else {
          miaoshaStatus = 1;
          remainSeconds = 0;
      }

      model.addAttribute("miaoshaStatus",miaoshaStatus);
      model.addAttribute("remainSeconds",remainSeconds);
      return "goods_detail";
  }  */

    /*
    采用URL页面缓存技术
     */
    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user, @PathVariable("goodsId")Long goodsId) {
        model.addAttribute("user",user);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId, String.class);
        if(!StringUtils.isEmpty(html)) {
            return html;
        }

        //手动渲染
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if(now < startAt) {
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt-now)/1000);
        }else if (now > endAt) {
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);

        SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap(), applicationContext);

        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if(StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
        }
        return html;
    }

    /**
     * 前后端分离技术
     * @param request
     * @param response
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user, @PathVariable("goodsId")Long goodsId) {
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if(now < startAt) {
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now)/1000);
        }else if(now > endAt) {
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoods(goods);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        goodsDetailVo.setUser(user);

        return Result.success(goodsDetailVo);
    }

}
