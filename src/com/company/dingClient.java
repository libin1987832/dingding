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
    public String get_listId(String token,long curse,int day) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode("PROC-30076949-A6F9-401B-A04C-AFF305AC4EE0");
        Date d = new Date();

        long startT=getTimestamp(d)-day*timeD;
        long endT=getTimestamp(d);
        //System.out.println(Long.toString(startT)+" "+Long.toString(endT));//查询开始时间和结束时间
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
                //  System.out.println("客户单位名称"+j.getString("value"));
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
                //如果已经提交成功，就从最后评论中取得账号
                int sum=operational.size();
                if(sum>2) {
                    String myremark = operational.get(sum-1).getString("remark");
                    if(myremark!=null&&myremark.contains("@tv.com"))
                        remark = "最后" + myremark;
                }
                //如果已经中途，就从我的用户中
                if(remark==null||!remark.contains("@tv.com")) {
                    for(JSONObject jo:operational)
                    {
                        if("0466311823845822".equals(jo.getString("userid")))
                        {
                            String myremark = jo.getString("remark");
                            if(myremark!=null&&myremark.contains("@tv.com"))
                                remark = "我的" + myremark;
                        }
                    }
                }
                //从玉洁中取得
                if(remark==null||!remark.contains("@tv.com")) {
                        if(operational.size()>1) {
                            JSONObject opera = operational.get(1);
                            remark = "第二个"+ opera.getString("remark");
                        }
                }
                if(remark!=null){
                    remark=remark.replaceAll("\n", " ").trim();
                    user.setRemark(remark);
                }
            }
        }

        return user;
    }

    public List<User> parseAllid(String token,String jsonString) throws ApiException, ParseException{
        List<String> listS = JSON.parseArray(jsonString,String.class);
        DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
        OapiProcessinstanceGetRequest req2 = new OapiProcessinstanceGetRequest();
        List<User> users = new ArrayList<User>();
        System.out.println("钉钉中有用户:"+Integer.toString(listS.size()));
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
        }
        System.out.println("还有"+Integer.toString(index_processNe3)+"个单我处理");
        return users;
    }
    public List<User> parse(String token,String jsonString) throws ApiException, ParseException {
     //   JSONObject object = JSONObject.parseObject(jsonString);
   //     JSONObject objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
    //    List<String> listS = JSON.parseArray(objsub.getJSONArray("list").toJSONString(),String.class);
        List<String> listS = JSON.parseArray(jsonString,String.class);
        DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
        OapiProcessinstanceGetRequest req2 = new OapiProcessinstanceGetRequest();
        List<User> users = new ArrayList<User>();
        System.out.println("钉钉中有用户:"+Integer.toString(listS.size()));
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
            //如果处理流程不等于3 则意味这个单子没有到我这里来 不处理
            if(2==operational.size()&&operational.get(1).getString("operation_result").equals("AGREE"))
            {
                index_processNe3++;
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("2");
                user.setResult("未在数据库找到（钉钉）");
                users.add(user);
            }
            else if(5==operational.size()&&operational.get(4).getString("operation_result").equals("AGREE"))
            {
                User user=gerenateUser(objsublist,operational);
                user.setProcessId(l);
                user.setDingdingId(businessId);
                user.setStatus("5");
                user.setResult("未在数据库找到（钉钉）");
                users.add(user);
            }
        }
        System.out.println("还有"+Integer.toString(index_processNe3)+"个单我处理");
        return users;
    }
    public static Long getTimestamp(Date date){
        if (null == date) {
            return (long) 0;
        }
        String timestamp = String.valueOf(date.getTime());
        return Long.valueOf(timestamp);
    }
    public List<User> parseAll(String token,int day) throws ApiException, ParseException {
        long curse = 0L;
        String sizelist = get_listId(token,curse,day);
        JSONObject object = JSONObject.parseObject(sizelist);
        JSONObject objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
      //  List<User> listS = parse(token,objsub.getJSONArray("list").toJSONString());
        List<User> listS = parseAllid(token,objsub.getJSONArray("list").toJSONString());
        while(objsub.containsKey("next_cursor")) {
            curse = objsub.getLongValue("next_cursor");
            sizelist = get_listId(token,curse,day);
            object = JSONObject.parseObject(sizelist);
            objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
        //    List<User> listT = parse(token,objsub.getJSONArray("list").toJSONString());
            List<User> listT = parseAllid(token,objsub.getJSONArray("list").toJSONString());
            listS.addAll(listT);
        }
        return listS;
    }
    public static void main(String[] args) {

        try {
            dingClient dC = new dingClient();
            String token = dC.get_access_token();
            List<User> users = dC.parseAll(token,2);
            users.forEach(System.out::println);
        } catch (ApiException | ParseException e) {
            e.printStackTrace();
        }
    }
}
