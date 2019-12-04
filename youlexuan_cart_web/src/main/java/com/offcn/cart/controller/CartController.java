package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.entity.Result;
import com.offcn.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.rmi.CORBA.Util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    @SuppressWarnings("all")
    private HttpServletResponse response;

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //获取当前登录用户用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户:" + username);

        List<Cart> cartList_Cookie = null;
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        if (cartListString != null && !cartListString.equals("")) {
            cartList_Cookie = JSON.parseArray(cartListString, Cart.class);
        }

        //如果未登录
        if (username != null && username.equals("anonymousUser")) {
            //读取本地cookie购物车
            if (cartList_Cookie == null) {
                return new ArrayList<>();
            }
            return cartList_Cookie;
        } else {//如果已登录
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);//从redis中获取
            if (cartList_Cookie != null && cartList_Cookie.size() > 0) {//如果本地存在购物车
                //合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_Cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
            }
            return cartList_redis;

        }
    }


    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    // 使用@CrossOrigin注解  可解决跨域访问的问题
    // allowCredentials  缺省值
    @CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //获取当前登录用户用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户:" + username);

//        response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
//        response.setHeader("Access-Control-Allow-Credentials","true");

        try {
            List<Cart> cartList = findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if (username.equals("anonymousUser")) {//如果是未登录,保存到cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");
            } else {//如果是未登录,保存到redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

}
