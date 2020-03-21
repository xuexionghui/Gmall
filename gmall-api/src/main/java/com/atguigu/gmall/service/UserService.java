package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UmsMember;

public interface UserService {


    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String memberId);

    void addQQUser(UmsMember umsMember);

    UmsMember checkQQUser(UmsMember umsCheck);
}
