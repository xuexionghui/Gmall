package com.atguigu.gmall.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.mapper.UserMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

/**
 * Created by 辉 on 2020/3/21.
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public void addQQUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
    }

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:"+memberId+":token",60*60*2,token);

        jedis.close();
    }

    @Override
    public UmsMember checkQQUser(UmsMember umsCheck) {
        List<UmsMember> select = userMapper.select(umsCheck);
        UmsMember umsMember = select.get(0);
        return umsMember;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String s = jedis.get("user:" + umsMember.getUsername() + ":info");
            UmsMember umsMember1 = JSON.parseObject(s, UmsMember.class);
            if (umsMember1 == null) {  //redis中没有，从数据库中查  设置分布式锁
                // 设置分布式锁
                String token = UUID.randomUUID().toString();
                String OK = jedis.set("user:" + umsMember.getUsername() + ":lock", token, "nx", "px", 10 * 1000);// 拿到锁的线程有10秒的过期时间
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                    UmsMember umsMemberFronMysql = getUserFromDb(umsMember);
                    //删除锁
                    String lockToken = jedis.get("user:" + umsMember.getUsername() + ":lock");
                    if (StringUtils.isNotBlank(lockToken) && lockToken.equals(token)) {
                        //jedis.eval("lua");可与用lua脚本，在查询到key的同时删除该key，防止高并发下的意外的发生
                        jedis.del("user:" + umsMember.getUsername() + ":lock");// 用token确认删除的是自己的sku的锁
                    }
                    if (umsMemberFronMysql == null) {
                        jedis.setex("user:" + umsMember.getUsername() + ":info", 5, "");
                        return null;
                    } else {
                        jedis.setex("user:" + umsMember.getUsername() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberFronMysql));
                        return umsMemberFronMysql;
                    }
                } else {//没有拿到锁，重定向访问方法
                    return login(umsMember);
                }
            } else {//redis中有这个用户，比对密码
                if (umsMember1.getPassword() != null && umsMember.getPassword().equals(umsMember1.getPassword())) {
                    return umsMember1;
                } else {
                    return null;
                }
            }

        } catch (Exception e) {

        } finally {
            jedis.close();
        }
        return null;
    }

    private UmsMember getUserFromDb(UmsMember umsMember) {
        List<UmsMember> select = userMapper.select(umsMember);
        UmsMember member = select.get(0);
        return member;
    }
}
