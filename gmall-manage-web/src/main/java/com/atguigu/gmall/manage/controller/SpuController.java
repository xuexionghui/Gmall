package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.manage.util.AliOSSUtil;
import com.atguigu.gmall.service.SpuService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    private static Logger logger = Logger.getLogger(SpuController.class);
    /**
     * 本地存放目录
     */
    private static String uoloadPath = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "upload/";


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);
        return pmsProductImages;
    }


    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }



    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file) {
        // 将图片或者音视频上传到分布式的文件存储系统
        // 将图片的存储路径返回给页面
        String url = null;
        if (!file.isEmpty()) {
            try {
                // 上传文件信息
                logger.info("OriginalFilename：" + file.getOriginalFilename());
                logger.info("ContentType：" + file.getContentType());
                logger.info("Name：" + file.getName());
                logger.info("Size：" + file.getSize());
                //TODO:文件大小、名称、类型检查的业务处理
                // 检查上传目录
                File targetFile = new File(uoloadPath);
                if (!targetFile.exists()) {
                    targetFile.mkdirs();
                }
                // 实例化输出流
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(uoloadPath + file.getOriginalFilename()));
                out.write(file.getBytes());
                out.flush();
                out.close();

                // 上传到OSS
                url = AliOSSUtil.uploadLocalFile(new File(uoloadPath + file.getOriginalFilename()), "upload/avatar/");
                if (url == null) {
                    //TODO:上传失败的业务处理
                    return null;
                }
                logger.info("上传完毕,访问地址:" + url);
                return url;
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("上传失败", e);
                return "上传失败";
            }
        }
        return "上传失败，因为文件是空的.";
    }


    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody  PmsProductInfo pmsProductInfo){

        spuService.saveSpuInfo(pmsProductInfo);

        return "success";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);

        return pmsProductInfos;
    }
}
