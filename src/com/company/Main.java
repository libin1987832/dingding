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
import java.util.*;

import static com.company.readlog.readTxtFile;

/*调用本接口前，请注意以下限制：

        如果只传了start_time参数，这个时间距离当前时间不能超过120天，end_time不传则默认取当前时间。

        如果传了start_time和end_time，时间范围不能超过120天，同时start_time时间距当前时间不能超过365天。

        批量获取的实例ID个数（循环获取），最多不能超过10000个*/
public class Main {

    private final String head="客户名称，业务员，备注，钉钉时间，数据库时间，数据库账号，结果，钉钉编号";
    //正式版本的账户放这里 防止被处理
/*    private static String[] acc_formal={"yssrmtzxjs@tv.com",// 2022-09-13 营山融媒体中心
            "hjxrmtzx2@tv.com",// 2022-08-31 合江县融媒体中心
            "arqrmtzx@tv.com",// 2022-08-19 阿荣旗融媒体中心 开通时长：6小时
            "qxqrmtzx2@tv.com",// 2022-07-31 南京栖霞区人民广播电台 一代录播计时版60个小时
            "jmdst@tv.com",// 2022-07-27 江门广播电视台
            "xyrmtzx@tv.com",// 2022-05-15 信宜市融媒体中心
            "xnsdst2@tv.com",// 2022-07-30 西宁市广播电视台
            "xssgbdst@tv.com",// 2023-04-30 襄阳广播电视台
    };*/
    //特殊版本的账户
    //玉洁账户//文静账户//周燕
    // 余慧敏//王子牛//千博测试
    // 何倩//长广千博//单位二代测试账号
    // 邓总账号//湖北经视临时用//胡蓉
    //胡舟//公司电脑一代系统//120演播室//江门电视台
    //长沙电视台测试账号//长沙电视台测试账号//(作废)西宁电视台新账号
    //(作废)南京栖霞区人民广播电台(正式版)//(作废)营山融媒体中心(正式版)
    //西藏赠送的台当时电脑有问题所以随便申请的账号（停止按钮不出现） 暂时不要删掉 后期确定后删掉
    //郴州电视台说网卡问题改了账号 但是没有确定这个账号不用了 揭阳正式两个账号
    //石河子电视台//千博宴老师的账号
    // 湛江电视台
    final static String[] acc_special={"yujie@yujie.com","9287235436@qq.com","3786823487@qq.com",
            "yuhuiming@ceshi.com", "wangziniu@ceshi.com","4830421821@qq.com",
            "QBHX@TV.COM","cgqb@tv.com","ceshi2rev@tv.com",
            "dengjiale@tv.com","hbjspd@tv.com","hurongshiyong@tv.com",
            "huzhouceshi1@tv.com","ceshi17@tv.com","yanboshi120@tv.com","jmdst@tv.com",
            "changshadianshitai2@ceshi.com","changshadianshitai@ceshi.com",//"xnsdst2@tv.com",
          //  "qxqrmtzx2@tv.com","yssrmtzxjs@tv.com",
            "2222936085@qq.com","0492625158@qq.com",
            "czdst2@tv.com","jieyanggbdst@tv.com","jieyanggbdst1@tv.com",
            "shihezirmtzx@tv.com","641775425@qq.com",
            "zhanjiang1@tv.com","zhanjiang2@tv.com","zhanjiang3@tv.com"};
//处理要更新时间和我确认时间
    public static void myprocess() throws ParseException, ApiException {
        List<User> usersDingding = new ArrayList<User>();
        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            //type 1 表示我要处理的单子 其它是完成的单子 （需要修改时间，最后不是我评论的，延长时间的）
           // usersDingding = dC.parseAll(token, 120,1);
           // System.out.println("dingding token:"+token);//dingding token
            long day=400;
            for(int i=0;i<10;i++)
            {

                List<User> temp= dC.parseAll(token, day,1);
                usersDingding.addAll(temp);
                if(day>120)
                {  day = day-120;            System.out.println("大于120天，重置成120天");}
                else
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        List<User> usersDingding2 = new ArrayList<User>();
        List<User> usersDingding5 =new ArrayList<User>();
        List<User> usersDingding8 =new ArrayList<User>();
        //对不同情况的用户不同情况处理
        for(User u:usersDingding)
        {
            if(u.getStatus().equals("2"))
                usersDingding2.add(u);
            if(u.getStatus().equals("3"))
                usersDingding2.add(u);
            if(u.getStatus().equals("5"))
                usersDingding5.add(u);
            if(u.getStatus().equals("6"))//延长时间要求暂时和需要开通时间合并
                usersDingding2.add(u);
            if(u.getStatus().equals("8"))
                usersDingding8.add(u);
        }
        usersDingding=null;
       // System.out.println("还有"+ usersDingding2.size() +"个用户需要更新时间，有"+ usersDingding5.size() +"个用户需要最后确认时间!");
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> userDatebase = c.get_user(token);
        Iterator<User> it = userDatebase.listIterator();
        Iterator<User> it2 = usersDingding2.listIterator();

        String acc_formal_s=formal_user.accountString()+Tibei.accountString();
        String[] acc_formal=acc_formal_s.split(":");
        System.out.println("正式账户的个数："+acc_formal.length);
        for(String a:acc_formal) {
            //去掉数据库中包含的账户
            while (it.hasNext()) {
                User item = it.next();
                if(a.equals(item.getAccount()))
                    it.remove();
            }
            //去掉钉钉中的账户
            while (it2.hasNext()) {
                User item = it2.next();
                /*if(item.getUser_name().equals("湖南教育台"))
                {
                    System.out.println("test:该账户存在");
                }*/
                if(a.equals(item.getAccount()))
                    it2.remove();
                if(item.getAccountArray()==null)
                    continue;
                for(String aa:item.getAccountArray()) {
                    if(a.equals(aa)) {
                        it2.remove();break;
                    }
                }
            }
            it = userDatebase.listIterator();
            it2 = usersDingding2.listIterator();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String detail26="有问题状态既不是我要确认单，也不是延长时间单子，";
        List<String> modifiedDelay=new ArrayList<>();
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
                        if(ud.getStatus().equals("6"))
                            modifiedDelay.add(ud.getDingdingId());
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
                                if (ud.getDingding_expiryDate().compareTo(ua.getExpiryDate()) > 0) {
                                    boolean b = c.update_user(token, ua.getUserId(), ud.getDingding_expiryDate());
                                    updateNum++;
                                    if (!b) {
                                        successNum--;
                                    }
                                }
                            }else
                            {
                                ud.setResult(detail26+"更新的时间少于数据库失效时间，这种情况不合理（考虑续签的单子第一次时间）");
                            }
                        }
                    }
                }
                if (successNum == ud.getAccountArray().length) {
                    ud.setResult("全部成功,本次运行更新"+updateNum+"个客户！");
                    ud.setExpiryDate(ud.getDingding_expiryDate());
                    if(updateNum == 0)
                        modifiedDelay.add(ud.getDingdingId());
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
        if(usersDingding2.size()>0&&usersDingding2.size()!=modifiedDelay.size())
        System.out.println("更新账号");
        for (User ud : usersDingding2) {
            if(!modifiedDelay.contains(ud.getDingdingId()))
                System.out.print(ud.toString());
        }
        if(usersDingding5.size()>0)
        System.out.println("确定账号");
        for (User ud : usersDingding5)
            System.out.print(ud.toString());
        if(usersDingding8.size()>0)
        System.out.println("延长时间我还没有确认的");
        for (User ud : usersDingding8)
            System.out.print(ud.toString());
    }
    //汇总数据和对比数据库
    public static void allId() throws ParseException, ApiException {
        List<User> usersDingding = new ArrayList<User>();
        List<User> usersDingdingYichang = new ArrayList<User>();
        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            long day=300;
            for(int i=0;i<3;i++)
            {

                List<User> temp= dC.parseAll(token, day,2);
                usersDingding.addAll(temp);
                if(day>120)
                    day = day-120;
                else
                    break;
            }
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


        Iterator<User> it = userDatebase.listIterator();
        Iterator<User> it2 = usersDingding.listIterator();
        List<User> userfomal = new ArrayList<User>();
        String acc_formal_s=formal_user.accountString()+Tibei.accountString();
        String[] acc_formal=acc_formal_s.split(":");
        System.out.println("正式账户的个数："+acc_formal.length);
        for(String a:acc_formal) {
            //去掉数据库中包含的账户
            while (it.hasNext()) {
                User item = it.next();
                if(a.equals(item.getAccount()))
                {
                    it.remove();
                   // System.out.println("该账户formal(删除数据库)"+" "+item.getAccount());
                }

            }
            it=userDatebase.listIterator();
    /*        if(a.equals("gcxrmtzx@tv.com"))
            {
                System.out.println("test:该账户存在");
            }*/
            //去掉钉钉中的账户
            while (it2.hasNext()) {
                User item = it2.next();
                /*if(item.getUser_name().equals("刚察县融媒体中心"))
                {
                    System.out.println("test:该账户存在");
                }*/
                if(a.equals(item.getAccount()))
                {
                  //  System.out.println("该账户formal(删除钉钉测试)"+" "+item.getUser_name());
                    userfomal.add(item);
                    it2.remove();
                }
                if(item.getAccountArray()==null)
                    continue;
                for(String aa:item.getAccountArray()) {
                    if(a.equals(aa)) {
                //        System.out.println("该账户formal(删除钉钉测试)"+" "+item.getUser_name());
                        userfomal.add(item);
                        it2.remove();break;
                    }
                }
            }
            it2 = usersDingding.listIterator();
        }



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean addYi=true;
        //单个账号

            for (User ud : usersDingding) {
                for (User u : userDatebase) {
                    try {
                    if (ud.getAccount() == null && ud.getAccountArray().length == 0) {
                        ud.setResult("无法从钉钉获得账号");
                    } else if (ud.getAccount() != null && ud.getAccount().equals(u.getAccount())) {
                        ud.setUserId(u.getUserId());
                        ud.setExpiryDate(u.getExpiryDate());
                        ud.setJoinDate(u.getJoinDate());
                        // System.out.println(u.getAccount()+" "+sdf.format(u.getExpiryDate())+" "+sdf.format(u.getJoinDate()));
                        if (sdf.format(ud.getDingding_expiryDate()).equals(sdf.format(ud.getExpiryDate()))) {
                            ud.setResult("时间一致");
                            addYi = false;
                        } else {
                            ud.setResult("时间不一致");
                        }
                    }
                    }catch (Exception e)
                    {
                        System.out.println(ud.getUser_name());
                    }
                }
                if (addYi && ud.getAccountArray() == null)
                    usersDingdingYichang.add(ud);
                addYi = true;
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
        System.out.println("正式账户情况（前期是试用）：");
        for (User ud : userfomal)
        {
            System.out.println(ud.getUser_name() + " " + ud.accountToString() +
                    " " + sdf.format(ud.getDingding_expiryDate()) + " ");
        }
        System.out.println("试用账户情况：");
        for (User ud : usersDingding) {
            System.out.print(ud.getUser_name() + " " + ud.getUser_sell() + " "+ ud.accountToString() +
                    " " + sdf.format(ud.getDingding_expiryDate()) + " ");
            if(ud.getExpiryDate()!=null)
                System.out.print(sdf.format(ud.getExpiryDate()) + " " + ud.getResult() + "\n");
            else
            {
                System.out.println("************数据库没有失效时间（钉钉账号在数据库没有时间）********");
            }
        }
        System.out.println("异常账户情况：");
        for (User ud : usersDingdingYichang)
            System.out.print(ud.toString());
        System.out.println("*************************************");
        System.out.println("到今天已经到期的账号：");
        System.out.println("单位 账户 OA中时间 后台的数据时间");
        Date now = new Date();
        for (User ud : usersDingding)
            if(ud.getExpiryDate()!=null&&ud.getExpiryDate().compareTo(now)==-1) {
                System.out.print(ud.getUser_name() + " " + ud.accountToString() +
                        " " + sdf.format(ud.getDingding_expiryDate()) + " ");
                    System.out.print(sdf.format(ud.getExpiryDate()) + " " + ud.getResult() + "\n");
            }

       /* System.out.println("*************************************");
        System.out.println("不活跃的账户（通过分析D:login.log中的数据文件）：");
        String filePath = "D:\\login.log";
//      "res/";
        Map m = readTxtFile(filePath);
        Set<String> st = m.keySet();
        Iterator<String> is = st.iterator();
        while(is.hasNext())
        {
            String key = is.next();
            Date d = (Date) m.get(key);
            for (User ud : usersDingding){
                if(ud.accountEqual(key))
                    ud.setLastLogin(d);
            }
        }
        for (User ud : usersDingding)
            if(ud.getLastLogin()==null&&ud.getExpiryDate().compareTo(now)>-1)
                System.out.print(ud.getUser_name()+" "+ud.accountToString()+
                        " "+ sdf.format(ud.getDingding_expiryDate())+ " " + sdf.format(ud.getExpiryDate())+" "+"\n");
        System.out.println("*************************************");
        System.out.println("活跃的账户");
        for (User ud : usersDingding)
            if(ud.getExpiryDate()!=null&&ud.getLastLogin()!=null)
                System.out.print(ud.getUser_name()+" "+ud.accountToString()+
                        " "+ sdf.format(ud.getDingding_expiryDate())+ " " + sdf.format(ud.getExpiryDate())+" "+
                        sdf.format(ud.getLastLogin())+"\n");
        System.out.println("*************************************");*/

    }
    public static Map<Integer,String> get_user_info(int type,long day)
    {
        Map<Integer,String> user_id_str= new HashMap<Integer,String>();
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> userDatebase = c.get_join_day(token,day);//获得数据库在day天前后面注册账号，已加入时间为准

        List<User> usersDingding = new ArrayList<User>();//获得数钉钉在day天前后面注册账号，已加入时间为准
        long temp_day=day;
        try {
            dingClient dC = new dingClient();
            token = dC.get_access_token();
           // usersDingding = dC.parseAll(token, day,type);
            for(int i=0;i<10;i++)
            {

                List<User> temp= dC.parseAll(token, day, type);
                usersDingding.addAll(temp);
                if(day>120){
                    day = day-120;
                    System.out.println("大于120天，重置成120天");
                } else
                    break;
            }
           // System.out.println("dingding token:"+token);//dingding token
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        day=temp_day;
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
               //     u.setRemark(ud.getRemark());
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
                          //  u.setRemark(ud.getRemark());
                           // u.setAccount(account);
                           // u.setAccountArray(ud.getAccountArray());
                            u.setDingding_joinDate(ud.getDingding_joinDate());
                            u.setDingding_expiryDate(ud.getDingding_expiryDate());
                            u.setResult("钉钉账户和数据库数据一致且账号是多个");
                            oaSign.add(u);
                            addflag=true;
                            break;
                        }
                }
                if(addflag)break;
            }
            if(!addflag){
                noaSign.add(u);
            }

        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         System.out.println("在oA中有记录的账户：");
         Date date = new Date((new Date()).getTime()-day*24*60*60*1000L);
        for (int i =0 ;i< oaSign.size();i++)
        {
           // if(date.compareTo(ud.getJoinDate())<0)
           // System.out.print(ud.toString());
            User ud=oaSign.get(i);
            if(!ud.getResult().equals("print")) {
                System.out.print(ud.getUser_name() + "," + sdf.format(ud.getDingding_expiryDate()) + "," + ud.getAccount() + "," + sdf.format(ud.getExpiryDate()));
                for (int j = i + 1; j < oaSign.size(); j++) {
                    User ud1 = oaSign.get(j);
                    if (ud.getUser_name().equals(ud1.getUser_name())) {

                        System.out.print("," + ud1.getAccount() + "," + sdf.format(ud1.getExpiryDate()));
                        ud1.setResult("print");
                    }
                }
                System.out.print("\n");
            }
        }
        System.out.println("在oA中没记录的账户：");
        List<User> dayNoSign= new ArrayList<User>();
        for (User item:noaSign) {

            String dataacc=item.getAccount();

            boolean cb=false;
            //每个没有OA记录的账号中查找是否是特殊的账号
            for(String a:acc_special)
                if(a.equals(dataacc))
                    cb=true;
                //进一步处理 如果是正式账号 或者特殊账号则不处理 否则看注册时间是否在考虑的范围内
            if (cb) {
               continue;
               //r如果不是特殊账号则需要在看注册的时间是不是在最近考虑的范围内 如果在 则输出来 否则也不考虑处理
            }else if(date.compareTo(item.getJoinDate())<0) {
                dayNoSign.add(item);
                System.out.println(item.getUserId() + " " + item.getAccount()+" "+sdf.format(item.getJoinDate())+" "+sdf.format(item.getExpiryDate()));
            }
        }
         int[] deleteid=new int[dayNoSign.size()];
        for (int i=0;i<dayNoSign.size();i++) {
            deleteid[i] = dayNoSign.get(i).getUserId();
            System.out.print(deleteid[i] + "," );
        }
        System.out.println(" ");
        System.out.println("@qq.com");
        int[] deleteidqq=new int[dayNoSign.size()];
        for (int i=0;i<dayNoSign.size();i++) {
            deleteidqq[i] = dayNoSign.get(i).getUserId();
            if(dayNoSign.get(i).getAccount().contains("@qq.com")) {
                System.out.print(deleteidqq[i] + ",");
                user_id_str.put(Integer.valueOf(deleteid[i]),dayNoSign.get(i).getAccount());
            }

        }
        //考虑到部分账号虽然不含QQ 但是也要删除 但是这些账号需要手动添加
        String deleteManu = "shzrmtzx@tv.com,shzrmtzx2@tv.com";
        //string.indexOf(subString) == -1
        System.out.println("********************");
        System.out.print("手动指定删除账号:");
        for (int i=0;i<dayNoSign.size();i++) {
            deleteidqq[i] = dayNoSign.get(i).getUserId();
            if(deleteManu.indexOf(dayNoSign.get(i).getAccount())>-1) {
                System.out.print(deleteidqq[i] + ",");
                user_id_str.put(Integer.valueOf(deleteid[i]),dayNoSign.get(i).getAccount());
            }

        }
        System.out.println("********************");
        return user_id_str;
    }
    public static void delete_user(int[] useid)
    {
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        c.delete_user(token,useid);
    }

    public static void main(String[] args) throws ParseException, ApiException {

        args=new String[]{"d"};
        //args[0] ="u";
        if(args[0].equals("u")) {
            System.out.println("根据钉钉的记录时间，跟新数据库的时间");
            myprocess();
            System.out.println("跟新结束");
        }
        if(args[0].equals("d")){
            System.out.println("删除一些QQ的账户");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date beginTime = sdf.parse("2021-03-01");
            Date now =new Date();
            Map user=get_user_info(3,(now.getTime()-beginTime.getTime())/1000/3600/24);
            Set<Integer> id=user.keySet();
            //提取QQ账号的ID 用于删除
            int[] strid=new int[id.size()];
            int index = 0;
            Iterator it = id.iterator();
            while (it.hasNext()) {
                int idd=Integer.valueOf(it.next().toString());
                strid[index++]=idd;
                System.out.println(idd+":"+user.get(idd));
            }
            if(strid.length>0)
                delete_user(strid);
        }
        if(args[0].equals("a")) {
            System.out.println("输出所有账户的信息情况");
            allId();
        }
     //   get_user_info();
        //type = 2 是完成所有的流程的钉钉单 3是在运行中和完成成功的流程单 时间是只输出前day天的申请账号（数据库注册时间）//2021-6-1
   //get_user_info(3,20L);
   //delete_user(new int[]{722,721,718,717,716,714,713,712,711,710});
    }

}
