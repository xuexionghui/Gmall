package com.atguigu.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.QQutil.QQHttpClient;
import com.atguigu.gmall.QQutil.QQStateErrorException;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 辉 on 2020/3/21.
 */
@Controller
public class QQController {
    @Reference
    UserService userService;


    /**
     * 发起请求
     *
     * @param session
     * @return
     */
    @GetMapping("/qq/login")
    public String qq(HttpSession session) throws UnsupportedEncodingException {
        //QQ互联中的回调地址
        String backUrl = "http://xuexionghui.natapp1.cc/qqLoginBack";
        //用于第三方应用防止CSRF攻击
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        session.setAttribute("state", uuid);

        //Step1：获取Authorization Code
        //生成QQ登录授权链接，看电脑上有哪些QQ登录了可以使用，没有登录就账号密码登录QQ
        String url = "https://graph.qq.com/oauth2.0/authorize?response_type=code" +
                "&client_id=" + QQHttpClient.APPID +
                "&redirect_uri=" + URLEncoder.encode(backUrl, "utf-8") +
                "&state=" + uuid;

        return "redirect:" + url;
    }

    /**
     * QQ回调
     *
     * @param request /qq/callback
     * @return
     */
    //Step2：用户选择账号后，使用重定向方式跳转回调地址，返回一个Authorization Code值
    //这里的访问路径要设置成QQ回调的地址
    @GetMapping("/qqLoginBack")
    //@ResponseBody  需要重定向到页面，那么这里就不能加这个注解，如果加这个注解，
    //不会重定向到页面，只会返回字符串
    public String qqcallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String token = null;
        HttpSession session = request.getSession();
        //qq返回的信息
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String uuid = (String) session.getAttribute("state");

        if (uuid != null) {
            if (!uuid.equals(state)) {
                throw new QQStateErrorException("QQ,state错误");
            }
        }


        //Step3：通过Authorization Code获取Access Token
        String backUrl = "http://xuexionghui.natapp1.cc/qqLoginBack";
        String url = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code" +
                "&client_id=" + QQHttpClient.APPID +
                "&client_secret=" + QQHttpClient.APPKEY +
                "&code=" + code +
                "&redirect_uri=" + backUrl;

        String access_token = QQHttpClient.getAccessToken(url);

        //Step4: 通过Access Token 获取回调后的 openid 值
        url = "https://graph.qq.com/oauth2.0/me?access_token=" + access_token;
        String openid = QQHttpClient.getOpenID(url);

        //Step5：获取QQ用户信息
        url = "https://graph.qq.com/user/get_user_info?access_token=" + access_token +
                "&oauth_consumer_key=" + QQHttpClient.APPID +
                "&openid=" + openid;

        //返回用户的信息
        JSONObject jsonObject = QQHttpClient.getUserInfo(url);

        //也可以放到Redis和mysql中，只取出了部分数据，根据自己需要取
//        session.setAttribute("openid",openid);  //openid,用来唯一标识qq用户
//        session.setAttribute("nickname",(String)jsonObject.get("nickname")); //QQ名
//        session.setAttribute("figureurl_qq_2",(String)jsonObject.get("figureurl_qq_2")); //大小为100*100像素的QQ头像URL
        UmsMember umsMember = new UmsMember();
        umsMember.setAccessCode(code);
        umsMember.setAccessCode(access_token);
        umsMember.setGender((String) jsonObject.get("gender"));
        umsMember.setSourceType("2");
        umsMember.setNickname((String) jsonObject.get("nickname"));
        umsMember.setSourceUid(openid);
        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(openid);
        UmsMember umsMemberCheck = userService.checkQQUser(umsCheck);
        if (umsMemberCheck == null) {
            userService.addQQUser(umsMember);
        } else {
            umsMember = umsMemberCheck;
        }

        // 用jwt制作token
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId", memberId);
        userMap.put("nickname", nickname);


        String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();// 从request中获取ip
            if (StringUtils.isBlank(ip)) {
                ip = "127.0.0.1";
            }
        }

        // 按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2019gmall0105", userMap, ip);

        // 将token存入redis一份
        userService.addUserToken(token, memberId);
        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }
}
