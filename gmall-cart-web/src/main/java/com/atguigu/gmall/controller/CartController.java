package com.atguigu.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 辉 on 2020/3/8.
 */
@Controller
public class CartController {
    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping(value = "/addToCart")
    public String addToCart(String skuId, int quantity, HttpServletRequest request,
                            HttpServletResponse response, HttpSession session) {
        List<OmsCartItem> omsCartItemList = new ArrayList<>(); //new出一个集合，用户装载购物车的数据
        //查出商品的信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId, null);
        //封装商品成购物侧对象
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));

        String memberId = "1";
        omsCartItem.setMemberId(memberId);
        //判断用户有没有登录，没登录购物车保存在cookie，登录了购物车保存在数据库
        if (memberId == null) {  //没有登录

            //取出购物车，判断，如果购物车为空，添加商品到购物车
            String gmallCart = CookieUtil.getCookieValue(request, "gmallCart", true);
            if (gmallCart == null) {
                omsCartItemList.add(omsCartItem);
                CookieUtil.setCookie(request, response, "gmallCart", JSON.toJSONString(omsCartItemList),
                        60 * 60 * 24, true);

            } else {
                //如果购物车不为空，判断商品是否已经添加过
                List<OmsCartItem> omsCartItemList1 = JSON.parseArray(gmallCart, OmsCartItem.class);
                for (OmsCartItem cartItem : omsCartItemList1) {
                    if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                        cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        omsCartItemList1.add(cartItem);
                    } else {
                        omsCartItemList1.add(omsCartItem);
                    }
                }
                CookieUtil.setCookie(request, response, "gmallCart", JSON.toJSONString(omsCartItemList1),
                        60 * 60 * 24, true);
            }


        } else { //已经登录了
            //查询数据库是否已经有这条购物车记录
            OmsCartItem omsCartItem1FromDb = cartService.getCartBySkuidAndMemberId(memberId, skuId);
            //判断omsCartItem1FromDb是否为null，如果为null，新增购物车，如果不为null，更新购物车
            if (omsCartItem1FromDb == null) {
                cartService.addCart(omsCartItem);
            } else {
                omsCartItem1FromDb.setQuantity(omsCartItem1FromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItem1FromDb);
            }
            //更新缓存
            cartService.refreshcache(memberId);
        }
        return "redirect:/success.html";
    }

    /*
      展示购物车信息
     */
    @LoginRequired(loginSuccess = false)
    @RequestMapping(value = "/cartList",method = RequestMethod.GET)
    public   String  cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){
        String  memberId="1";//模拟用户登录与否
        List<OmsCartItem> omsCartItemList=null;
        if (memberId==null){ //用户没有登录
            String gmallCart = CookieUtil.getCookieValue(request, "gmallCart", true);
            omsCartItemList = JSON.parseArray(gmallCart, OmsCartItem.class);
        }else { //用户已经登录   从缓存中取数据
            omsCartItemList=cartService.getCartFromCache(memberId);
        }
        modelMap.put("cartList",omsCartItemList);
        return "cartList";
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("checkCart")
    public   String  checkCart( String isChecked,String skuId,
                               HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){
        String memberId = "1";

        // 调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);

        // 将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.getCartFromCache(memberId);
        modelMap.put("cartList",omsCartItems);
        return "cartListInner";
    }
}
