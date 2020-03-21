package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;
    @Reference
    AttrService  attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){// 三级分类id、关键字、

        // 调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos =  searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

         //取出平台属性的值id，用set集合去重
        Set<String>   attrValueId=new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                attrValueId.add(valueId);
            }
        }
        // 根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(attrValueId);
        modelMap.put("attrList", pmsBaseAttrInfos);

        //去除点击的平台属性  pmsSearchParam
        // 对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
        String[] delValueIds = pmsSearchParam.getValueId(); //请求中的平台属性值
        if(delValueIds!=null){
            Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
            while(iterator.hasNext()){
                PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                    String valueId = pmsBaseAttrValue.getId();
                    for (String delValueId : delValueIds) {
                        if(delValueId.equals(valueId)){
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
            }
        }
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);

        //制作面包屑
        ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        if (delValueIds!=null){
            // 如果valueIds参数不为空，说明当前请求中包含属性的参数，每一个属性参数，都会生成一个面包屑
            for (String delValueId : delValueIds) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setValueName(delValueId);//暂时用平台属性值的id代替
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam,delValueId));
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
        }
        modelMap.put("attrValueSelectedList", pmsSearchCrumbs);

        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (skuAttrValueList != null) {

            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
            }
        }

        return urlParam;
    }



    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam, String delValueId) {
      String  urlParam="";
      String keyword = pmsSearchParam.getKeyword();
      String catalog3Id = pmsSearchParam.getCatalog3Id();
      String[] valueId = pmsSearchParam.getValueId();
        if (StringUtils.isNotBlank(keyword)){  //keyword不为空
            if (StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"ketword="+keyword;
        }
        if (StringUtils.isNotBlank(catalog3Id)){  //catalog3Id不为空
            if (StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"catalog3Id="+catalog3Id;
        }
        if (valueId!=null){
            for (String s : valueId) {
                if (!s.equals(delValueId))
                    urlParam = urlParam + "&valueId=" + s;
            }
        }
        return urlParam;

    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/")
    public String indexXie(){
        return "index";
    }
}
