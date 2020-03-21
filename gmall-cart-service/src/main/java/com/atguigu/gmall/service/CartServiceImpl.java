package com.atguigu.gmall.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.mapper.CartMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 辉 on 2020/3/8.
 */
@Service
public class CartServiceImpl implements CartService {
    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e = new Example(OmsCartItem.class);

        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());

        cartMapper.updateByExampleSelective(omsCartItem,e);

        // 缓存同步
        refreshcache(omsCartItem.getMemberId());
    }

    @Autowired
    private CartMapper cartMapper;


    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem getCartBySkuidAndMemberId(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setMemberId(memberId);
        OmsCartItem omsCartItem1 = cartMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        cartMapper.insertSelective(omsCartItem);
    }

    @Override
    public void updateCart(OmsCartItem omsCartItem1FromDb) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id", omsCartItem1FromDb.getId());
        cartMapper.updateByExample(omsCartItem1FromDb, example);
    }

    @Override
    public void refreshcache(String memberId) {
        //从数据库中取出用户的购物车
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = cartMapper.select(omsCartItem);
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            Map<String, String> map = new HashMap<>();
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getQuantity().multiply(cartItem.getPrice()));
                map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }
            Long del = jedis.del("gmallCart" + memberId);
            jedis.hmset("gmallCart" + memberId, map);
        } finally {
            jedis.close();
        }
    }

    @Override
    public List<OmsCartItem> getCartFromCache(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();

            List<String> hvals = jedis.hvals("gmallCart" + memberId);

            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            }

        }catch (Exception e){
            // 处理异常，记录系统日志
            e.printStackTrace();
            //String message = e.getMessage();
            //logService.addErrLog(message);
            return null;
        }finally {
            jedis.close();
        }

        return omsCartItems;
    }

}
