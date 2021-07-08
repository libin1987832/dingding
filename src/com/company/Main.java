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
import java.util.Iterator;
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
            //type 1 表示我要处理的单子 其它是完成的单子
            usersDingding = dC.parseAll(token, 90,1);
            System.out.println("dingding token:"+token);//dingding token
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        List<User> usersDingding2 = new ArrayList<User>();
        List<User> usersDingding5 =new ArrayList<User>();
        List<User> usersDingding8 =new ArrayList<User>();
        for(User u:usersDingding)
        {
            if(u.getStatus().equals("2"))
                usersDingding2.add(u);
            if(u.getStatus().equals("5"))
                usersDingding5.add(u);
            if(u.getStatus().equals("6"))//延长时间要求暂时和需要开通时间合并
                usersDingding2.add(u);
            if(u.getStatus().equals("8"))
                usersDingding8.add(u);
        }
        usersDingding=null;
        System.out.println("还有"+ usersDingding2.size() +"个用户需要更新时间，有"+ usersDingding5.size() +"个用户需要最后确认时间!");
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> userDatebase = c.get_user(token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String detail26="有问题状态既不是我要确认单，也不是延长时间单子，";

        //单个账号
        for (User u : userDatebase) {
            for (User ud : usersDingding2) {
                if(ud.getStatus().equals("2"))
                    detail26="新的单子，";
                if(ud.getStatus().equals("6"))
                    detail26="延长时长的单子，";
                if (ud.getAccount()==null){
                    ud.setResult(detail26+"无法从钉钉获得账号");
                }else if(ud.getAccount().equals(u.getAccount())) {
                    ud.setUserId(u.getUserId());
                    ud.setExpiryDate(u.getExpiryDate());
                    ud.setJoinDate(u.getJoinDate());
                    // System.out.println(u.getAccount()+" "+sdf.format(u.getExpiryDate())+" "+sdf.format(u.getJoinDate()));
                    if(sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ud.getExpiryDate())) ){
                            ud.setResult(detail26+"已经在系统更新");
                    }
                    else if(ud.getDingding_expiryDate().compareTo(u.getExpiryDate())>0){//更新的时间应该比当前的结束时间后面，主要是续签的账号，失效日期一定要在前一个账号后面才行
                        boolean b = c.update_user(token, ud.getUserId(), ud.getDingding_expiryDate());
                        if (b) {
                                ud.setResult(detail26+"更新成功");
                            ud.setExpiryDate(ud.getDingding_expiryDate());
                        }
                        else{
                            ud.setResult(detail26+"更新不成功，可能是设置更新的时间少于当前七天后的时间");
                        }
                    }
                    else {
                        ud.setResult(detail26+"更新的时间少于数据库失效时间，这种情况不合理（考虑续签的单子第一次时间）");
                    }
                }
            }
        }
        int updateNum = 0;
        //多个账号的情况
        for (User ud : usersDingding2) {
            if (ud.getAccount()==null&&ud.getAccountArray() != null && ud.getAccountArray().length > 1) {
                int successNum = 0;
                for (String account : ud.getAccountArray()) {
                    for (User ua : userDatebase) {
                        if (account.equals(ua.getAccount())) {
                            successNum++;
                            ud.setJoinDate(ua.getJoinDate());
                            if (!sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ua.getExpiryDate()))) {
                                boolean b = c.update_user(token, ua.getUserId(), ud.getDingding_expiryDate());
                                updateNum++;
                                if (!b) {
                                    successNum--;
                                }
                            }
                        }
                    }
                }
                if (successNum == ud.getAccountArray().length) {
                    ud.setResult("全部成功,本次运行更新"+updateNum+"个客户！");
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
                    {  ud.setResult("钉钉和数据库时间一致");ud.setExpiryDate(u.getExpiryDate());ud.setJoinDate(u.getJoinDate());}
                    else
                    {    ud.setResult("钉钉时间和数据库时间不相同");ud.setExpiryDate(u.getExpiryDate());ud.setJoinDate(u.getJoinDate());}
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
                        { successNum++;ud.setJoinDate(ua.getJoinDate());}
                        else
                        {ud.setExpiryDate(ua.getExpiryDate());ud.setJoinDate(ua.getJoinDate());}
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
        System.out.println("延长时间我还没有确认的");
        for (User ud : usersDingding8)
            System.out.print(ud.toString());
    }
    //汇总数据和对比数据库
    public static void allId()
    {
        List<User> usersDingding = null;
        List<User> usersDingdingYichang = new ArrayList<User>();
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
        boolean addYi=true;
        //单个账号
        for (User ud : usersDingding) {
             for (User u : userDatebase){
                if (ud.getAccount()==null&&ud.getAccountArray().length==0){
                    ud.setResult("无法从钉钉获得账号");
                }else if(ud.getAccount()!=null&&ud.getAccount().equals(u.getAccount())) {
                    ud.setUserId(u.getUserId());
                    ud.setExpiryDate(u.getExpiryDate());
                    ud.setJoinDate(u.getJoinDate());
                    // System.out.println(u.getAccount()+" "+sdf.format(u.getExpiryDate())+" "+sdf.format(u.getJoinDate()));
                    if(sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ud.getExpiryDate())) )
                    {  ud.setResult("时间一致");addYi=false;}
                    else
                    { ud.setResult("时间不一致");}
                }
            }
            if(addYi&&ud.getAccountArray()==null)
                usersDingdingYichang.add(ud);
            addYi=true;
        }
        addYi=false;
        //多个账号的情况
        for (User ud : usersDingding) {
            if (ud.getAccount()==null&&ud.getAccountArray() != null && ud.getAccountArray().length > 1) {
                int successNum = 0;
                for (String account : ud.getAccountArray())
                    for (User ua : userDatebase)
                        if (account.equals(ua.getAccount())&&sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ua.getExpiryDate())))
                        {successNum++;ud.setJoinDate(ua.getJoinDate());ud.setExpiryDate(ua.getExpiryDate());}
                if (successNum == ud.getAccountArray().length)
                { ud.setResult("时间一致");addYi=false;}
                else
                {   ud.setResult("时间不一致，只存在"+successNum+"个存在并且时间符合要求");addYi=true;}
            }
            if(addYi)
                usersDingdingYichang.add(ud);
            addYi=false;
        }
        System.out.println("所有账户情况：");
        for (User ud : usersDingding)
            System.out.print(ud.toString());
        System.out.println("异常账户情况：");
        for (User ud : usersDingdingYichang)
            System.out.print(ud.toString());

    }
    public static int[] get_user_info(int type,long day)
    {
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> userDatebase = c.get_join_day(token,200L);

        List<User> usersDingding = null;
        try {
            dingClient dC = new dingClient();
            token = dC.get_access_token();
            usersDingding = dC.parseAll(token, 100L,type);
            System.out.println("dingding token:"+token);//dingding token
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        List<User> oaSign=new ArrayList<>();
        List<User> noaSign = new ArrayList<>();
        //将数据库的数据检测是否在钉钉中有账户
         for (User u:userDatebase)
        {
            boolean addflag = false;
            for (User ud:usersDingding)
            {
                if(ud.getAccount()!=null&&u.getAccount().equals(ud.getAccount()))
                {
                    u.setUser_name(ud.getUser_name());
                    u.setUser_sell(ud.getUser_sell());
                    u.setRemark(ud.getRemark());
                    u.setDingding_joinDate(ud.getDingding_joinDate());
                    u.setDingding_expiryDate(ud.getDingding_expiryDate());
                    u.setResult("钉钉账户和数据库数据一致且账号是一个");
                    oaSign.add(u);
                    addflag=true;
                }
                else if(ud.getAccountArray()!=null){
                    for(String account:ud.getAccountArray())
                        if(u.getAccount().equals(account))
                        {
                            u.setUser_name(ud.getUser_name());
                            u.setUser_sell(ud.getUser_sell());
                            u.setRemark(ud.getRemark());
                            u.setAccount(account);
                            u.setDingding_joinDate(ud.getDingding_joinDate());
                            u.setDingding_expiryDate(ud.getDingding_expiryDate());
                            u.setResult("钉钉账户和数据库数据一致且账号是多个");
                            oaSign.add(u);
                            addflag=true;
                            break;}
                }
                if(addflag)break;
            }
            if(!addflag){
                noaSign.add(u);
            }

        }


         System.out.println("在oA中有记录的账户：");
         Date date = new Date((new Date()).getTime()-day*24*60*60*1000L);
        for (User ud : oaSign)
        {
            if(date.compareTo(ud.getJoinDate())<0)
            System.out.print(ud.toString());
        }
        System.out.println("在oA中没记录的账户：");
        List<User> dayNoSign= new ArrayList<User>();
        for (User item:noaSign) {
            //玉洁账户
            //文静账户

            //周燕
            //余慧敏
            //王子牛
            //千博测试
            //何倩
            //长广千博
            if (item.getAccount().equals("yujie@yujie.com")||
                    item.getAccount().equals("9287235436@qq.com")||
                        item.getAccount().equals("3786823487@qq.com")||
                            item.getAccount().equals("yuhuiming@ceshi.com")||
                                item.getAccount().equals("wangziniu@ceshi.com")||
                                    item.getAccount().equals("4830421821@qq.com")||
                                        item.getAccount().equals("QBHX@TV.COM")||
                                            item.getAccount().equals("cgqb@tv.com")) {
               continue;
            }else if(date.compareTo(item.getJoinDate())<0) {
                dayNoSign.add(item);
                System.out.println(item.getUserId() + " " + item.getAccount());
            }
        }
         int[] deleteid=new int[dayNoSign.size()];
        for (int i=0;i<dayNoSign.size();i++) {
            deleteid[i] = dayNoSign.get(i).getUserId();
            System.out.print(deleteid[i] + "," );
        }
        return deleteid;
    }
    public static void delete_user(int[] useid)
    {
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        c.delete_user(token,useid);
    }
    public static void main(String[] args) {
       myprocess();
       // allId();
       // get_user_info();
        //type = 2 是完成所有的流程的钉钉单 3是在运行中和完成成功的流程单 时间是只输出前day天的申请账号（数据库注册时间）
     // get_user_info(3,10L);
   //delete_user(new int[]{401,400});
    }

}
