package com.atguigu.gmall.manage.util;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.jboss.logging.Logger;

import java.io.File;


/**
 * OSS上传工具类
 * 2017/10/26
 *
 * @author Cheivin
 * @version 1.0
 */
public class AliOSSUtil {

    private final static Logger logger = Logger.getLogger(AliOSSUtil.class);

    /**
     * 管理控制台里面获取EndPoint
     */
    private final static String END_POINT = "oss-cn-beijing.aliyuncs.com";//"oss-cn-shanghai.aliyuncs.com";
    /**
     * 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建
     */
    private final static String ACCESS_KEY_ID = "LTAI4FqQU2VTpngJB1tT4FzL";   //"这里填你的ACCESSKEY";
    private final static String ACCESS_KEY_SECRET = "rsf1rP3QgDp1dvZCcidK04eQQHg4zI";//"这里填你的ACCESSKEYSECRET";
    /**
     * 上传的BUCKET名称
     */
    private final static String BUCKET_NAME = "xuexionghui";//"这里填你的BUCKET名称";
    /**
     * 管理控制台里面获取的访问域名
     */
    private final static String FILE_HOST ="xuexionghui.oss-cn-beijing.aliyuncs.com";// "这里填访问域名";

    /**
     * 上传文件到bucket
     *
     * @param file     本地文件
     * @param dir      bucket存放目录(末尾要加/)
     * @param fileName 上传文件名
     * @return 访问地址
     */
    public static String uploadLocalFile(File file, String dir, String fileName) {
        if (file == null || !file.exists()) {
            return null;
        }
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(END_POINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        try {
            // 上传文件
            PutObjectResult result = ossClient.putObject(new PutObjectRequest(BUCKET_NAME, dir + fileName, file));
            if (null != result) {
                return FILE_HOST + dir + fileName;
            } else {
                return null;
            }
        } catch (OSSException | ClientException oe) {
            logger.error("上传OSS失败:", oe);
            oe.printStackTrace();
            return null;
        } finally {
            // 关闭OSS服务
            ossClient.shutdown();
        }
    }

    /**
     * 上传文件到bucket
     *
     * @param file 本地文件
     * @param dir  bucket目录
     * @return 访问地址
     */
    public static String uploadLocalFile(File file, String dir) {
        if (file == null) {
            return null;
        }
        String filePath = dir + file.getName();
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(END_POINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        try {
            // 上传文件
            PutObjectResult result = ossClient.putObject(new PutObjectRequest(BUCKET_NAME, filePath, file));
            if (null != result) {
                // 拼装访问地址
                return FILE_HOST + filePath;
            } else {
                return null;
            }
        } catch (OSSException | ClientException oe) {
            logger.error("OSS上传失败:", oe);
            oe.printStackTrace();
            return null;
        } finally {
            // 关闭OSS服务
            ossClient.shutdown();
        }
    }
}