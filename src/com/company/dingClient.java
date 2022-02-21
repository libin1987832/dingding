package com.company;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiProcessinstanceGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.sun.jna.WString;
import com.taobao.api.ApiException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class dingClient {
    private final long  timeD= 24*60*60*1000;
    public String get_access_token() throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey("dinglanffnj8to34oxkh");
        request.setAppsecret("Mx3JxmrVTWgCN1eqA6IvvTcN9vU8WzTq6_lEfhStmk8VY4-NgK3lIgCmHCrn6eSw");
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);
      //  System.out.println(response.getBody());
        String output = response.getBody();
        JSONObject object = JSONObject.parseObject(output);
        String token=object.getString("access_token");
        return token;
    }
    public String get_listId(String token,long curse,long day) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode("PROC-30076949-A6F9-401B-A04C-AFF305AC4EE0");
        Date d = new Date();

        long startT=getTimestamp(d)-day*timeD;
        long endT=getTimestamp(d);
        if(day>120)
        {
            endT = startT + 120*timeD;

        }
    //    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
     //   System.out.println("从钉钉获取从"+df.format(new Date(startT))+"到"+df.format(endT));
   //     System.out.println(Long.toString(startT)+" "+Long.toString(endT));//查询开始时间和结束时间
        req.setStartTime(startT);
        req.setEndTime(endT);
        req.setSize(10L);
        req.setCursor(curse);
        OapiProcessinstanceListidsResponse rsp = client.execute(req, token);
        String jsonString =rsp.getBody();
        return jsonString;
    }
    public User gerenateUser(List<JSONObject> objsublist,List<JSONObject> operational) throws ParseException {
        User user = new User();
       // user.setProcessId(pocessId);
       // user.setResult("数据库未查到（钉钉）");
       // user.setDingdingId(businessId);
        for(JSONObject j:objsublist)
        {

            String v=j.getString("name");
            if(v.equals("客户单位名称"))
            {
                //  System.out.println("客户单位名称:"+j.getString("value"));
                if(j.getString("value").indexOf("清远市广播电视台")>-1)
                    System.out.println("客户单位名称:"+j.getString("value"));
                user.setUser_name(j.getString("value"));
            }
            else if(v.equals("业务负责人"))
            {
                //  System.out.println("业务负责人"+j.getString("value"));
                user.setUser_sell(j.getString("value"));
            }
            else if(v.equals("[\"测试开始时间\",\"测试结束时间\"]"))
            {
                //    System.out.println("测试结束时间"+j.getString("value"));
                String beginT=j.getString("value").substring(2,12);
                String endT=j.getString("value").substring(15,25);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                user.setDingding_expiryDate(df.parse(endT));
                user.setDingding_joinDate(df.parse(beginT));
            }
            else if(v.equals("备注"))
            {
                //    System.out.println("备注"+j.getString("value"));
                String remark = j.getString("value");

//               if(remark.equals("hdrmtzx@tv.com"))
//                {
//                    System.out.println("debug");
//                }

                if(remark!=null&&remark.equals("null"))
                    remark=null;
                //如果已经提交成功，就从最后评论中取得账号  或者remark 是错误的
                int sum=operational.size();
                if(sum>2) {
                    final String[] chinese={"倒数一","倒数二","倒数三"};

                    //从最后的3条评论中找账号 后面的优先
                    for(int i=1;i<3;i++) {
                        String myremark = operational.get(sum - i).getString("remark");
                        String userid = operational.get(sum - i).getString("userid");
                        if (myremark != null && myremark.contains("@tv.com") && "0466311823845822".equals(userid))
                        { remark = "最后评论中第"+ chinese[i] + myremark;break;}
                    }
                }
                //如果已经中途，就从我的用户中  (如果不在最后就从头开始找我的评论)
                if(remark==null||!remark.contains("@tv.com")) {
                    int index=0;
                    final String[] chinese2={"一","二","三","四","五","六","七","八","九"};
                    for(JSONObject jo:operational)
                    {
                        index++;
                        if("0466311823845822".equals(jo.getString("userid")))
                        {
                            String myremark = jo.getString("remark");
                            if(myremark!=null&&myremark.contains("@tv.com"))
                                remark = "我的" +chinese2[index%9]+ myremark;
         //                 if(myremark!=null&&myremark.contains("@qq.com"))
               //                 user.setAccount(myremark);
                            if(myremark!=null&&myremark.contains("@TV.COM"))
                                user.setAccount(myremark);
                        }
                    }
                }
                //从玉洁中取得
                if(remark==null||!remark.contains("@tv.com")) {
                        if(operational.size()>1) {
                            JSONObject opera = operational.get(1);
                            String myremark=opera.getString("remark");
                            if(myremark!=null&&myremark.contains("@tv.com"))
                                remark = "第二个位置"+myremark;
                            // 在评论中
                           if(remark == null&&operational.size()>2){
                                opera = operational.get(2);
                                myremark=opera.getString("remark");
                               if(myremark!=null&&myremark.contains("@tv.com"))
                                   remark = "第三个位置"+myremark;
                            }
                        }
                }

                if(user.getUser_name().equals("广安区融媒体中心"))
                {
                    user.setAccount("4001401298@qq.com");
                }
                if(user.getUser_name().equals("仙桃广播电视台"))
                {
                    user.setAccount("xiantao@qq.com");
                }
                if(remark!=null){
                    remark=remark.replaceAll("\n", " ").trim();
                    remark=remark.replaceAll("，", " ").trim();

                    user.setRemark(remark);
                }else
                    user.setRemark("没有取到有效的值");
            }
        }

        return user;
    }

    public List<User> parseAllRunning(String token,String jsonString) throws ApiException, ParseException{
        List<String> listS = JSON.parseArray(jsonString,String.class);
        DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
        OapiProcessinstanceGetRequest req2 = new OapiProcessinstanceGetRequest();
        List<User> users = new ArrayList<User>();
       // System.out.println("钉钉中有用户:"+Integer.toString(listS.size()));
        int index_processNe3=0;
        for(String l:listS)
        {
            // System.out.println(l);//实例的id 可以提供给APIexplorer
            req2.setProcessInstanceId(l);
            OapiProcessinstanceGetResponse rsp2 = client2.execute(req2, token);
            String jsonString2 =rsp2.getBody();
            JSONObject object = JSONObject.parseObject(jsonString2);
            // System.out.println(jsonString);
            JSONObject objsub = JSON.parseObject(object.getJSONObject("process_instance").toJSONString());
            String businessId = objsub.getString("business_id");
            List<JSONObject> objsublist = JSON.parseArray(objsub.getJSONArray("form_component_values").toJSONString(),JSONObject.class);
            List<JSONObject> operational = JSON.parseArray(objsub.getJSONArray("operation_records").toJSONString(),JSONObject.class);
            String status=objsub.getString("status");
            //如果处理流程不等于3 则意味这个单子没有到我这里来 不处理
            if(6<operational.size()&&status.equals("COMPLETED"))
            {
                index_processNe3++;
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("2");
                user.setResult("未在数据库找到（钉钉）");
                users.add(user);
            }
            if(2<operational.size()&&status.equals("RUNNING"))
            {
                index_processNe3++;
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("3");
                user.setResult("未在数据库找到（钉钉）");
                users.add(user);
            }
        }
     //   System.out.println("还有"+Integer.toString(index_processNe3)+"个单我处理");
        return users;
    }

    public List<User> parseAllid(String token,String jsonString) throws ApiException, ParseException{
        List<String> listS = JSON.parseArray(jsonString,String.class);
        DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
        OapiProcessinstanceGetRequest req2 = new OapiProcessinstanceGetRequest();
        List<User> users = new ArrayList<User>();
     //   System.out.println("钉钉中有用户:"+Integer.toString(listS.size()));
        int index_processNe3=0;
        for(String l:listS)
        {
            // System.out.println(l);//实例的id 可以提供给APIexplorer
            req2.setProcessInstanceId(l);
            OapiProcessinstanceGetResponse rsp2 = client2.execute(req2, token);
            String jsonString2 =rsp2.getBody();
            JSONObject object = JSONObject.parseObject(jsonString2);
            // System.out.println(jsonString);
            JSONObject objsub = JSON.parseObject(object.getJSONObject("process_instance").toJSONString());
            String businessId = objsub.getString("business_id");
            List<JSONObject> objsublist = JSON.parseArray(objsub.getJSONArray("form_component_values").toJSONString(),JSONObject.class);
            List<JSONObject> operational = JSON.parseArray(objsub.getJSONArray("operation_records").toJSONString(),JSONObject.class);
            String status=objsub.getString("status");
            int sumoper = operational.size();
            boolean addflag =true;
            for(int i=sumoper-1;i>-1;i--)
            {
                if(operational.get(i).getString("operation_result").equals("REFUSE"))
                    addflag=false;
            }
            //延长时间
            String remarkdetails="";
            String userid = operational.get(sumoper-1).getString("userid");
            String remark = operational.get(sumoper-1).getString("operation_type");
            boolean type =false;
            if (sumoper>1&&"0466311823845822".equals(userid)&&"ADD_REMARK".equals(remark))
            {
                    remarkdetails = operational.get(sumoper - 1).getString("remark");
                    if (remarkdetails.contains("extend:"))
                        type = true;//需要延长的
                    else if (operational.get(sumoper - 2).getString("remark")!=null&&operational.get(sumoper - 2).getString("remark").contains("extend:")) {
                        remarkdetails = operational.get(sumoper - 2).getString("remark");
                        type = true;//需要延长的
                        //最后一个可能是账号因此不能用 还存在两个extend 因为前面一个是错误的
                    }
            }

            //如果处理流程不等于3 则意味这个单子没有到我这里来 不处理 还有拒绝的要丢弃 finish 关闭单子
            if(6<operational.size()&&status.equals("COMPLETED")&&addflag)
            {
                index_processNe3++;
                User user=gerenateUser(objsublist,operational);
                if(type)
                    user=modified_time(user,remarkdetails.substring(7).trim());
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("2");
                user.setResult("未在数据库找到（钉钉）");
                user.setLast_remark(remarkdetails);
                users.add(user);

            }
        }
        //System.out.println("还有"+Integer.toString(index_processNe3)+"个单我处理");
        return users;
    }
    public User modified_time(User u,String time) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        u.setDingding_expiryDate(df.parse(time));
        return u;
    }
    public List<User> parse(String token,String jsonString) throws ApiException, ParseException {
     //   JSONObject object = JSONObject.parseObject(jsonString);
   //     JSONObject objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
    //    List<String> listS = JSON.parseArray(objsub.getJSONArray("list").toJSONString(),String.class);
        List<String> listS = JSON.parseArray(jsonString,String.class);
        DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
        OapiProcessinstanceGetRequest req2 = new OapiProcessinstanceGetRequest();
        List<User> users = new ArrayList<User>();
        //System.out.println("钉钉中有用户:"+Integer.toString(listS.size()));
        int index_processNe3=0;
        for(String l:listS)
        {
           // System.out.println(l);//实例的id 可以提供给APIexplorer
            req2.setProcessInstanceId(l);
            OapiProcessinstanceGetResponse rsp2 = client2.execute(req2, token);
            String jsonString2 =rsp2.getBody();
            JSONObject object = JSONObject.parseObject(jsonString2);
            // System.out.println(jsonString);
            JSONObject objsub = JSON.parseObject(object.getJSONObject("process_instance").toJSONString());
            String businessId = objsub.getString("business_id");
            List<JSONObject> objsublist = JSON.parseArray(objsub.getJSONArray("form_component_values").toJSONString(),JSONObject.class);
            List<JSONObject> operational = JSON.parseArray(objsub.getJSONArray("operation_records").toJSONString(),JSONObject.class);
            int sumoper = operational.size();
            String status = objsub.getString("status");
            String userid = operational.get(sumoper-1).getString("userid");
            String remark = operational.get(sumoper-1).getString("operation_type");
            String result = null;
            if(sumoper>1)
                result = operational.get(1).getString("operation_result");//.equals("AGREE"); 玉洁可能添加评论 导致同意的单子会在2个位置
            int type = 0;
            //单子玉洁处理完成
            if (sumoper>1&&"195135213224684158".equals(userid)&&operational.get(sumoper-1).getString("operation_result").equals("AGREE")&&"RUNNING".equals(status))
                type = 1;
            //由于账号从备注没找到 我评论才找到  但是我还没同意
            if (sumoper>1&&"0466311823845822".equals(userid)&&"RUNNING".equals(status))
                type = 2;
            //我已经同意了 但是这个单子还没给别人审批 最后不能是评论 最后是评论很有可能是我为了加入账号  这种应该是要处理的
            if (sumoper>1&&"0466311823845822".equals(userid)&&"RUNNING".equals(status)&&result.equals("AGREE")&&!"ADD_REMARK".equals(remark))
                type = 0;
            //文总审批了 我最后确认时间
            if (sumoper>4&&"1542001861835468".equals(userid)&&"RUNNING".equals(status))
                type = 5;
            //部分客户可能会需要延长时间，业务员直接在单子给出来
            String remarkdetails = "";
            if (sumoper>4&&"0466311823845822".equals(userid))
            {
                if("ADD_REMARK".equals(remark)) {
                    remarkdetails = operational.get(sumoper - 1).getString("remark");

                    if (remarkdetails.contains("extend:"))
                        type = 6;//需要延长的
                    else if (operational.get(sumoper - 2).getString("remark")!=null&&operational.get(sumoper - 2).getString("remark").contains("extend:")) {
                        remarkdetails = operational.get(sumoper - 2).getString("remark");
                        type = 6;//需要延长的
                        //最后一个可能是账号因此不能用 还存在两个extend 因为前面一个是错误的
                    } else if(remarkdetails.contains("refuse"))
                        type = 7;
                    else if("RUNNING".equals(status))
                        type = 2;//需要注意最后一个评论的是不是账号问题 不含extend
                    else
                        type = 11;
                }
                else {
                    type=9;//是我评论的 但是内容不是增加 而是其它东西
                }
            }else if (sumoper>4&&!"PROCESS_CC".equals(remark)&&"COMPLETED".equals(status))//最后一个不是文总评论也不是我评论的就需要单独拉出来注意一下
            {
                //流程大于4个（不是中间拒绝的） 并且 最后评论不是我的都要注意一下
          //      if("195135213224684158".equals(userid)&&operational.get(sumoper-1).getString("operation_result").equals("REFUSE"))
          //          type = 10;
          //      else
                    type = 8;
            }
            //如果处理流程不等于3 则意味这个单子没有到我这里来 不处理
            if(type == 1||type ==2)
            {
                index_processNe3++;
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("2");
                user.setResult("未在数据库找到（钉钉）");
                users.add(user);
            }
            else if(type == 5)
            {
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("5");
                user.setResult("未在数据库找到（钉钉）");
                users.add(user);
            }
            else if(type==6)
            {
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("6");
                user.setResult("有延长时间要求");
                user=modified_time(user,remarkdetails.substring(7).trim());
                users.add(user);
            }
            else if(type==8)
            {
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("8");
                user.setResult("最后评论的人不是我（主要是看是不是有延长时间需求）评论内容："+remarkdetails);
                users.add(user);
            }
        }
      //  System.out.println("还有"+Integer.toString(index_processNe3)+"个单我处理");
        return users;
    }
    public static Long getTimestamp(Date date){
        if (null == date) {
            return (long) 0;
        }
        String timestamp = String.valueOf(date.getTime());
        return Long.valueOf(timestamp);
    }
    //1 为更新 2其它为查询
    public List<User> parseAll(String token,long day,int type) throws ApiException, ParseException {
        long curse = 0L;
        Date d = new Date();
        long startT=getTimestamp(d)-day*timeD;
        long  endT = getTimestamp(d);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if(day>120)
        {
            endT = startT + 120*timeD;
        }
        System.out.println("从钉钉获取从"+df.format(new Date(startT))+"到"+df.format(endT));
        String sizelist = get_listId(token,curse,day);
        JSONObject object = JSONObject.parseObject(sizelist);
        JSONObject objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
        List<User> listS =null;
        switch(type){
            case 1:listS = parse(token,objsub.getJSONArray("list").toJSONString());break;
            case 2:listS = parseAllid(token,objsub.getJSONArray("list").toJSONString());break;
            case 3:listS = parseAllRunning(token,objsub.getJSONArray("list").toJSONString());break;
        }


        while(objsub.containsKey("next_cursor")) {
            curse = objsub.getLongValue("next_cursor");
            sizelist = get_listId(token,curse,day);
            object = JSONObject.parseObject(sizelist);
            objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
            List<User> listT = null;
            if(type == 1)
            listT = parse(token,objsub.getJSONArray("list").toJSONString());
            else
             listT = parseAllid(token,objsub.getJSONArray("list").toJSONString());
            listS.addAll(listT);
        }
        return listS;
    }
    public static void main(String[] args) {

        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            List<User> users = dC.parseAll(token,10,1);
            users.forEach(System.out::println);
        } catch (ApiException | ParseException e) {
            e.printStackTrace();
        }
    }
}
