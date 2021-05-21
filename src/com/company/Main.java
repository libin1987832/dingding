package com.company;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.dingtalk.api.*;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiProcessinstanceGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.taobao.api.ApiException;
import com.alibaba.fastjson.JSON;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {
private final String head="客户名称，业务员，备注，钉钉时间，数据库时间，数据库账号，结果，钉钉编号";
    public static void myprocess()
    {
        List<User> usersDingding = null;
        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            usersDingding = dC.parseAll(token, 50);
            System.out.println("dingding token:"+token);//dingding token
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        //文本处理一些异常情况 包括订单没有账号 或者错误的情况
        try {
            String encoding = "UTF-8";
            File file = new File("D:\\server.txt");
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = null;//考虑到编码格式
                read = new InputStreamReader(
                        new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = bufferedReader.readLine();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    //  System.out.println(lineTxt);
                    String[] data = lineTxt.split(",");
                    User user = new User();
                    user.setUser_name(data[0]);
                    user.setUser_sell(data[1]);
                    user.setRemark(data[2]);
                    user.setDingding_expiryDate(df.parse(data[3]));
                    user.setResult(data[6]);
                    user.setDingdingId(data[7]);
                    user.setResult("数据库未查到（文本）");
                    boolean add = true;
                    for (User ud : usersDingding)
                        if(ud.getUser_name().equals(user.getUser_name())) {
                            add =false;
                            if(ud.getAccount()==null)
                                ud.setRemark(user.getRemark());
                            else {
                                //钉钉的邮箱可能有错误 就从文本中来 但是必须申请的账号和真实相差不大
                                String subAccount = ud.getAccount().substring(0,ud.getAccount().indexOf("@"));
                                if(user.getAccount().contains(subAccount))
                                    ud.setRemark(user.getRemark());
                            }
                            break;
                        }
                    if(add)usersDingding.add(user);
                }
                read.close();
            }
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<User> usersDingding2 = new ArrayList<User>();
        List<User> usersDingding5 =new ArrayList<User>();
        for(User u:usersDingding)
        {
            if(u.getStatus().equals("2"))
                usersDingding2.add(u);
            else
                usersDingding5.add(u);
        }
        usersDingding=null;
        System.out.println("还有"+ usersDingding2.size() +"个用户需要更新时间，有"+ usersDingding5.size() +"个用户需要最后确认时间!");
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> userDatebase = c.get_user(token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //单个账号
        for (User u : userDatebase) {
            for (User ud : usersDingding2) {
                if (ud.getAccount()==null){
                    ud.setResult("无法从钉钉获得账号");
                }else if(ud.getAccount().equals(u.getAccount())) {
                    ud.setUserId(u.getUserId());
                    ud.setExpiryDate(u.getExpiryDate());
                    // System.out.println(u.getAccount()+" "+sdf.format(u.getExpiryDate())+" "+sdf.format(u.getJoinDate()));
                    if(sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ud.getExpiryDate())) ){
                        ud.setResult("已经在系统更新");
                    }
                    else {
                        boolean b = c.update_user(token, ud.getUserId(), ud.getDingding_expiryDate());
                        if (b) {
                            ud.setResult("更新成功");
                            ud.setExpiryDate(ud.getDingding_expiryDate());
                        }
                    }
                }
            }
        }
        //多个账号的情况
        for (User ud : usersDingding2) {
            if (ud.getAccount()==null&&ud.getAccountArray() != null && ud.getAccountArray().length > 1) {
                int successNum = 0;
                for (String account : ud.getAccountArray()) {
                    for (User ua : userDatebase) {
                        if (account.equals(ua.getAccount())) {
                            successNum++;
                            if (!sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ua.getExpiryDate()))) {
                                boolean b = c.update_user(token, ua.getUserId(), ud.getDingding_expiryDate());
                                if (!b) {
                                    successNum--;
                                }
                            }
                        }
                    }
                }
                if (successNum == ud.getAccountArray().length) {
                    ud.setResult("全部成功");
                    ud.setExpiryDate(ud.getDingding_expiryDate());
                } else {
                    ud.setResult(Integer.toString(successNum) + "个成功");
                }
            }
        }

        //确认账号
        for (User u : userDatebase) {
            for (User ud : usersDingding5) {
                if (ud.getAccount()==null){
                    ud.setResult("确认阶段账号，无法从钉钉获得账号");
                }else if(ud.getAccount().equals(u.getAccount())){
                    if(sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(u.getExpiryDate())))
                        ud.setResult("钉钉和数据库时间一致");
                    else
                        ud.setResult("钉钉时间和数据库时间不相同");
                }
            }
        }
        //多个账号的情况
        for (User ud : usersDingding5) {
            if (ud.getAccount()==null&&ud.getAccountArray() != null && ud.getAccountArray().length > 1) {
                int successNum = 0;
                for (String account : ud.getAccountArray())
                    for (User ua : userDatebase)
                        if (account.equals(ua.getAccount())&&sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ua.getExpiryDate())))
                            successNum++;
                if (successNum == ud.getAccountArray().length)
                    ud.setResult("钉钉和数据库时间一致");
                else
                    ud.setResult("钉钉时间和数据库时间不相同");
            }
        }
        System.out.println("更新账号");
        for (User ud : usersDingding2)
            System.out.print(ud.toString());
        System.out.println("确定账号");
        for (User ud : usersDingding5)
            System.out.print(ud.toString());
    }
    public static void allId()
    {
        List<User> usersDingding = null;
        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            usersDingding = dC.parseAll(token, 100);
            System.out.println("dingding token:"+token);//dingding token
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }


        System.out.println("sum:" + usersDingding.size() +"个用户!");
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> userDatebase = c.get_user(token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //单个账号
        for (User u : userDatebase) {
            for (User ud : usersDingding) {
                if (ud.getAccount()==null){
                    ud.setResult("无法从钉钉获得账号");
                }else if(ud.getAccount().equals(u.getAccount())) {
                    ud.setUserId(u.getUserId());
                    ud.setExpiryDate(u.getExpiryDate());
                    // System.out.println(u.getAccount()+" "+sdf.format(u.getExpiryDate())+" "+sdf.format(u.getJoinDate()));
                    if(sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ud.getExpiryDate())) )
                        ud.setResult("时间一致");
                    else
                        ud.setResult("时间不一致");
                }
            }
        }
        //多个账号的情况
        for (User ud : usersDingding) {
            if (ud.getAccount()==null&&ud.getAccountArray() != null && ud.getAccountArray().length > 1) {
                int successNum = 0;
                for (String account : ud.getAccountArray()) {
                    for (User ua : userDatebase) {
             //           System.out.println(account);
          //             System.out.println(ud);
            //            System.out.println(ua);
                        if (account.equals(ua.getAccount())) {
                            successNum++;
                            if (!sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ua.getExpiryDate()))) {
                                boolean b = c.update_user(token, ua.getUserId(), ud.getDingding_expiryDate());
                                if (!b) {
                                    successNum--;
                                }
                            }
                        }
                    }
                }
                if (successNum == ud.getAccountArray().length) {
                    ud.setResult("全部成功");
                    ud.setExpiryDate(ud.getDingding_expiryDate());
                } else {
                    ud.setResult(Integer.toString(successNum) + "个成功");
                }
            }
        }

        //多个账号的情况
        for (User ud : usersDingding) {
            if (ud.getAccount()==null&&ud.getAccountArray() != null && ud.getAccountArray().length > 1) {
                int successNum = 0;
                for (String account : ud.getAccountArray())
                    for (User ua : userDatebase)
                        if (account.equals(ua.getAccount())&&sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ua.getExpiryDate())))
                            successNum++;
                if (successNum == ud.getAccountArray().length)
                    ud.setResult("时间一致");
                else
                    ud.setResult("时间不一致，只存在"+successNum+"个");
            }
        }
        System.out.println("所有账户情况：");
        for (User ud : usersDingding)
            System.out.print(ud.toString());

    }
    public static void main(String[] args) {
        //myprocess();
        allId();
    }

}
