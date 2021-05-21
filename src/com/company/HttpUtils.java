package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
    public static final String CHARSET = "UTF-8";
    // 发送get请求 url?a=x&b=xx形式
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlName = "";
            if (param.length() != 0) {
                urlName = url + "?" + param;
            } else
                urlName = url;
            URL resUrl = new URL(urlName);
            URLConnection urlConnec = resUrl.openConnection();
            urlConnec.setRequestProperty("accept", "*/*");
            urlConnec.setRequestProperty("connection", "Keep-Alive");
            urlConnec.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            urlConnec.connect();
            Map<String, List<String>> map = urlConnec.getHeaderFields();
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(urlConnec.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送get请求失败" + e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 发送post请求
    public static String sendPost(String url, MultipartHttpServletRequest param) {
        String result = "";
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            URL resUrl = new URL(url);
            URLConnection urlConnec = resUrl.openConnection();
            urlConnec.setRequestProperty("accept", "*/*");
            urlConnec.setRequestProperty("connection", "Keep-Alive");
            urlConnec.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            urlConnec.setDoInput(true);
            urlConnec.setDoOutput(true);

            out = new PrintWriter(urlConnec.getOutputStream());
            out.print(param);// 发送post参数
            out.flush();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(urlConnec.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("post请求发送失败" + e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    //post请求方法
    public static  String sendPost(String url, Map<String,Object> params) {
        String response = null;
        System.out.println(url);
        System.out.println(params);
        try {
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<NameValuePair>(params.size());
                for (String key : params.keySet()) {
                    pairs.add(new BasicNameValuePair(key, params.get(key).toString()));
                }
            }
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse httpresponse = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(url);
                // StringEntity stringentity = new StringEntity(data);
                if (pairs != null && pairs.size() > 0) {
                    httppost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
                }

                httpresponse = httpclient.execute(httppost);
                response = EntityUtils
                        .toString(httpresponse.getEntity());
                System.out.println(response);
            } finally {
                if (httpclient != null) {
                    httpclient.close();
                }
                if (httpresponse != null) {
                    httpresponse.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("funCode","QB101001");
        map.put("prodCode","SM01");
        map.put("token","null");
        Map<String,Object> mapargs = new HashMap<String,Object>();
        mapargs.put("deviceMAC","unkown");
        mapargs.put("deviceName","unkown");
        mapargs.put("ip","127.0.0.1");
        mapargs.put("loginId","admin001");
        mapargs.put("password","Qb123456#");
        map.put("args",mapargs);

        System.out.print(sendPost("http://ent.qbaidata.com/sladmin/service/QB101001",map));
    }
    /**
     * 测试
     * 说明：这里用新浪股票接口做get测试,新浪股票接口不支持jsonp,至于post,因为本人用的公司的接口就不展示了,一样的,一个url,一个数据包
     */
    /*
     * public static void main(String[] args) { // TODO Auto-generated method
     * stub String resultGet = sendGet("http://hq.sinajs.cn/list=sh600389","");
     * System.out.println(resultGet); }
     */

}