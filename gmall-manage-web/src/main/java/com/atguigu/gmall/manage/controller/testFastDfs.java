package com.atguigu.gmall.manage.controller;


import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

/**
 * Created by 辉 on 2020/3/7.
 */
public class testFastDfs {

    public static void main(String[] args) throws IOException, MyException {
        // 配置fdfs的全局链接地址
        String tracker = testFastDfs.class.getResource("/tracker.conf").getPath();// 获得配置文件的路径

        ClientGlobal.init(tracker);

        TrackerClient trackerClient = new TrackerClient();

        // 获得一个trackerServer的实例
        TrackerServer trackerServer = trackerClient.getTrackerServer();

        // 通过tracker获得一个Storage链接客户端
        StorageClient storageClient = new StorageClient(trackerServer,null);

        String[] uploadInfos = storageClient.upload_file("C:/Users/辉/Desktop/谷粒商城的测试图片/a.jpg", "jpg", null);

        String url = "http://192.168.196.175";

        for (String uploadInfo : uploadInfos) {
            url += "/"+uploadInfo;

            //url = url + uploadInfo;
        }

        System.out.println(url);
    }
    }

