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
//处理要更新时间和我确认时间
    public static void myprocess()
    {
        List<User> usersDingding = null;
        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            usersDingding = dC.parseAll(token, 50,1);
            System.out.println("dingding token:"+token);//dingding token
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ApiException e) {
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
                    ud.setJoinDate(u.getJoinDate());
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
                                ud.setJoinDate(ua.getJoinDate());
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
                    {  ud.setResult("钉钉和数据库时间一致");ud.setExpiryDate(u.getExpiryDate());}
                    else
                    {    ud.setResult("钉钉时间和数据库时间不相同");ud.setExpiryDate(u.getExpiryDate());}
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
                        { successNum++;}
                        else
                        {ud.setExpiryDate(ua.getExpiryDate());}
                if (successNum == ud.getAccountArray().length)
                { ud.setResult("钉钉和数据库时间一致");ud.setExpiryDate(ud.getDingding_expiryDate());}
                else
                    ud.setResult("钉钉时间和数据库时间不相同");
            }
        }
        if(usersDingding2.size()>0)
        System.out.println("更新账号");
        for (User ud : usersDingding2)
            System.out.print(ud.toString());
        if(usersDingding5.size()>0)
        System.out.println("确定账号");
        for (User ud : usersDingding5)
            System.out.print(ud.toString());
    }
    //汇总数据和对比数据库
    public static void allId()
    {
        List<User> usersDingding = null;
        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            usersDingding = dC.parseAll(token, 100,2);
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
                    ud.setJoinDate(u.getJoinDate());
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
                for (String account : ud.getAccountArray())
                    for (User ua : userDatebase)
                        if (account.equals(ua.getAccount())&&sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ua.getExpiryDate())))
                        {successNum++;ud.setJoinDate(ua.getJoinDate());ud.setExpiryDate(ua.getExpiryDate());}
                if (successNum == ud.getAccountArray().length)
                { ud.setResult("时间一致");}
                else
                {   ud.setResult("时间不一致，只存在"+successNum+"个");}
            }
        }
        System.out.println("所有账户情况：");
        for (User ud : usersDingding)
            System.out.print(ud.toString());

    }
    public static void main(String[] args) {
        myprocess();
        //allId();
    }

}
