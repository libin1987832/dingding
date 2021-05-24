package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JavaNetURLRESTFulClient {

    private static final String targetURL1 = "http://ent.qbaidata.com/sladmin/service/QB101001";
    private static final String targetURL2 = "http://ent.qbaidata.com/sladmin/service/QB601012";
    private static final String targetURL3 = "http://ent.qbaidata.com/sladmin/service/QB601013";
    public String get_token()
    {
        try {

            URL targetUrl = new URL(targetURL1);

            HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");

            String input1 =  "{\"funCode\":\"QB101001\",\"prodCode\":\"SM01\",\"token\":null,\"args\":{\"loginId\":\"admin001\",\"password\":\"Qb123456#\",\"ip\":\"127.0.0.1\",\"deviceName\":\"unkown\",\"deviceMAC\":\"unkown\"}}";

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(input1.getBytes());
            outputStream.flush();

            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (httpConnection.getInputStream())));

            String output;
            //System.out.println("Output from Server:\n");
            while ((output = responseBuffer.readLine()) != null) {
               // System.out.println(output);
                JSONObject object = JSONObject.parseObject(output);
                JSONObject objsub = JSON.parseObject(object.getJSONObject("data").toJSONString());
                String token=objsub.getString("accessToken");
                return token;
            }

            httpConnection.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return null;
    }

    public List<User> get_user(String token)
    {
        try {

            URL targetUrl = new URL(targetURL2);

            HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");

          String input2 =  "{\"funCode\":\"QB601012\",\"prodCode\":\"SM01\",\"token\":\""+token+"\",\"args\":{\"userId\":0}}";
           OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(input2.getBytes());
            outputStream.flush();

            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (httpConnection.getInputStream())));

            String output;
           // System.out.println("Output from Server:\n");
            List<User> users = new ArrayList<User>();
            while ((output = responseBuffer.readLine()) != null) {
              //  System.out.println(output);
                JSONObject object = JSONObject.parseObject(output);
                List<JSONObject> objsublist = JSON.parseArray(object.getJSONArray("data").toJSONString(),JSONObject.class);
                System.out.println("数据库包含用户数:"+Integer.toString(objsublist.size()));
                for(JSONObject j:objsublist)
                {
                    User user = new User();
                    String account = j.getString("account");
                    int userId = j.getInteger("uuid");
                    Date expiryDate = j.getDate("expiryDate");
                    Date joinDate = j.getDate("joinDate");
                    user.setAccount(account);
                    user.setUserId(userId);
                    user.setExpiryDate(expiryDate);
                    user.setJoinDate(joinDate);
                    user.setResult("数据在数据库,未在钉钉");
                    if(account!=null&&!account.trim().equals(""))
                        users.add(user);
                }
                return users;
            }

            httpConnection.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return null;
    }
    public boolean update_user(String token, int userId, Date expiryDate)
    {
        try {
            Date current7day = new Date((new Date()).getTime()+7*24*60*60*1000L);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");  //yyyy-MM-dd'T'HH:mm:ss.SSSZ
            String  date = df.format(expiryDate);
            // 确保跟新的时间至少在当前时间的七天以后
            if(current7day.compareTo(expiryDate)>0)
            {
                System.out.println("跟新的时间少于七天后，更新失败！跟新的时间："+date);
                return false;
            }

            URL targetUrl = new URL(targetURL3);

            HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");

            String input3 =  "{\"funCode\":\"QB601013\",\"prodCode\":\"SM01\",\"token\":\""+token+"\",\"args\":{\"userId\":"+Integer.toString(userId)+",\"addRemTime\":null,\"expiryDate\":\""+date.substring(0,23)+"Z\"}}";
            //System.out.println(input3);
            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(input3.getBytes());
            outputStream.flush();

            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (httpConnection.getInputStream())));

            String output;
         //   System.out.println("Output from Server:\n");
            while ((output = responseBuffer.readLine()) != null) {
           //    System.out.println(output);
                JSONObject object = JSONObject.parseObject(output);
                String m=object.getString("message");
                if("Succeed".equals(m))
                    return true;
                else
                    return false;
            }

            httpConnection.disconnect();
        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return false;
    }
    public List<User> get_join_day(String token,long day)
    {
        List<User> user= get_user(token);
        Date currentday = new Date((new Date()).getTime()-day*24*60*60*1000L);
        List<User> userd=new ArrayList<>();
        for (User u:user)
        {
            if(currentday.compareTo(u.getJoinDate())<0)
            {
                userd.add(u);
            }
        }
        return userd;
    }
    public static void main(String[] args) throws ParseException {
        Date current= new Date();
        Date current7day = new Date((new Date()).getTime()+7*24*60*60*1000);
        System.out.print(current7day.compareTo(current));
        System.out.print(current7day);
  /*      JavaNetURLRESTFulClient c= new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> user= c.get_user(token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(User u:user)
        {
            System.out.println(u.getAccount()+" "+sdf.format(u.getExpiryDate())+" "+sdf.format(u.getJoinDate()));
            if(u.getAccount().equals("6439713981@qq.com"))
            {
                boolean b = c.update_user(token,u.getUserId(),sdf.parse("2021-10-10"));
                if(b)
                    System.out.println("success");
                else
                    System.out.println("fail");
            }
        }*/

    }
}