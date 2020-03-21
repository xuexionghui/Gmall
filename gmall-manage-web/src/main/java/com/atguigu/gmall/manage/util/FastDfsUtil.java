package com.atguigu.gmall.manage.util;

import com.atguigu.gmall.manage.controller.testFastDfs;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by 辉 on 2020/3/7.
 */
public class FastDfsUtil {

    public static   String upload(MultipartFile multipartFile,String url) {
        // 获得上传的二进制对象
        byte[] bytes = null;
        try {
            bytes = multipartFile.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获得文件后缀名
        String originalFilename = multipartFile.getOriginalFilename();//获得文件的名字
        int i = originalFilename.lastIndexOf(".");//获得最后一个点的位置
        String s = originalFilename.substring(i + 1);  //获得文件的后缀名
        // 配置fdfs的全局链接地址
        String tracker = testFastDfs.class.getResource("/tracker.conf").getPath();// 获得配置文件的路径
        String[] uploadInfos = null;

        try {
            ClientGlobal.init(tracker);
            TrackerClient trackerClient = new TrackerClient();

            // 获得一个trackerServer的实例
            TrackerServer trackerServer = trackerClient.getTrackerServer();

            // 通过tracker获得一个Storage链接客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);

            uploadInfos = storageClient.upload_file(bytes, s, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }


       // String url = "http://192.168.196.175";

        for (String uploadInfo : uploadInfos) {
            url += "/" + uploadInfo;

            //url = url + uploadInfo;
        }

        System.out.println(url);
        return url;
    }
}
