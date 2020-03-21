package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * Created by 辉 on 2020/3/8.
 */
public interface CartService {
    OmsCartItem getCartBySkuidAndMemberId(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItem1FromDb);

    void refreshcache(String memberId);

    List<OmsCartItem> getCartFromCache(String memberId);

    void checkCart(OmsCartItem omsCartItem);
}
