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

import javax.swing.text.html.HTMLDocument;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class formal_user {
    private  static final long  timeD= 24*60*60*1000;
    public static Long getTimestamp(Date date){
        if (null == date) {
            return (long) 0;
        }
        String timestamp = String.valueOf(date.getTime());
        return Long.valueOf(timestamp);
    }
    public static String get_access_token() throws ApiException {
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
    public static String get_listId(String token, long curse, long day) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode("PROC-BF17FFB9-699A-4308-92F4-16A1F79CA5DF");//这个code通过https://open-dev.dingtalk.com/apiExplorer?spm=ding_open_doc.document.0.0.72b9722fgwSt18#/?devType=isv&api=dingtalk.oapi.process.get_by_name 这个接口获得

        Date d = new Date();

        long startT=getTimestamp(d)-day*timeD;
        long endT=getTimestamp(d);
        if(day>120)
        {
            endT = startT + 120*timeD;
        }
        req.setStartTime(startT);
        req.setEndTime(endT);
        req.setSize(10L);
        req.setCursor(curse);
        OapiProcessinstanceListidsResponse rsp = client.execute(req, token);
        String jsonString =rsp.getBody();
        return jsonString;
    }
    public static User modified_time(User u, String time) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        u.setDingding_expiryDate(df.parse(time));
        return u;
    }
    public static List<User> parse(String token, String jsonString) throws ApiException, ParseException {
        //   JSONObject object = JSONObject.parseObject(jsonString);
        //     JSONObject objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
        //    List<String> listS = JSON.parseArray(objsub.getJSONArray("list").toJSONString(),String.class);
        List<String> listS = JSON.parseArray(jsonString,String.class);
         DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
        OapiProcessinstanceGetRequest req2 = new OapiProcessinstanceGetRequest();
        List<User> users = new ArrayList<User>();
        //System.out.println("钉钉中有用户:"+Integer.toString(listS.size()));
        int index_processNe3=0;
        for(String l:listS) {
            // System.out.println(l);//实例的id 可以提供给APIexplorer
            req2.setProcessInstanceId(l);
            OapiProcessinstanceGetResponse rsp2 = client2.execute(req2, token);
            String jsonString2 = rsp2.getBody();
            JSONObject object = JSONObject.parseObject(jsonString2);
            // System.out.println(jsonString);
            JSONObject objsub = JSON.parseObject(object.getJSONObject("process_instance").toJSONString());
            String businessId = objsub.getString("business_id");
            List<JSONObject> objsublist = JSON.parseArray(objsub.getJSONArray("form_component_values").toJSONString(), JSONObject.class);
            List<JSONObject> operational = JSON.parseArray(objsub.getJSONArray("operation_records").toJSONString(), JSONObject.class);
            int sumoper = operational.size();
            String statusOperlast = objsub.getString("status");
            if(statusOperlast.equals("TERMINATED"))
                continue;
            String useridOperlast = operational.get(sumoper - 1).getString("userid");
            String remarkOperlast = operational.get(sumoper - 1).getString("operation_type");
            String resultOperlast = operational.get(sumoper - 1).getString("operation_result");
            if(resultOperlast.equals("REFUSE")||sumoper <8)
                continue;
            User u=new User();
            for(JSONObject j:objsublist) {

                String v = j.getString("name");
                if (v.equals("客户单位名称")) {
                    //  System.out.println("客户单位名称:"+j.getString("value"));
                    u.setUser_name(j.getString("value"));
                 //  if(u.getUser_name().equals("湛江市广播电视台"))
                 //       System.out.println("客户单位名称:"+j.getString("value"));
                } else if (v.equals("业务负责人")) {
                    //  System.out.println("业务负责人"+j.getString("value"));
                    u.setUser_sell(j.getString("value"));
                } else if (v.equals("[\"权限开始时间\",\"权限结束时间\"]")) {
                    //    System.out.println("测试结束时间"+j.getString("value"));
                    String beginT = j.getString("value").substring(2, 12);
                    String endT = j.getString("value").substring(15, 25);
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    u.setDingding_expiryDate(df.parse(endT));
                    u.setDingding_joinDate(df.parse(beginT));
                } else if (v.equals("备注")) {
                    String remark = j.getString("value");
                    u.setRemark(remark);
                }
            }
            String remarkdetails = "";
            if (sumoper > 8 && "0466311823845822".equals(useridOperlast))
            {
                int sum=operational.size();
                if(sum>2) {
                    final String[] chinese={"倒数一","倒数二","倒数三"};
                    //从最后的3条评论中找账号 后面的优先
                    for(int i=1;i<3;i++) {
                        String myremark = operational.get(sum - i).getString("remark");
                        String userid = operational.get(sum - i).getString("userid");
                        if (myremark != null && myremark.contains("@tv.com") && "0466311823845822".equals(userid))
                        {
                            String remark = "最后评论中第"+ chinese[i] + myremark;
                            u.setRemark(remark);
                        }
                        if (myremark != null && myremark.contains("extend:") && "0466311823845822".equals(userid))
                        {
                            u=modified_time(u,myremark.substring(7).trim());
                        }

                        if (myremark != null && myremark.contains("time:") && "0466311823845822".equals(userid))
                        {
                            u.setCounnt_time(myremark.substring(5).trim());
                        }
                        if (myremark != null && myremark.contains("htime:") && "0466311823845822".equals(userid))
                        {
                            int count = Integer.valueOf(myremark.substring(6).trim())*3600;
                            u.setCounnt_time(Integer.toString(count));
                        }
                    }
                }
                u.setStatus("1");
            } else{
                u.setStatus("0");
                u.setRemark("最后评论不是我");
        }
            users.add(u);
        }
        return users;
    }
    public static List<User> parseall() throws ApiException, ParseException{
        List<User> usersDingding = new ArrayList<User>();
        String token = get_access_token();
        int day =300;
        for(int i=0;i<10;i++)
        {

            String sizelist = get_listId(token,0,day);
            JSONObject object = JSONObject.parseObject(sizelist);
            JSONObject objsub = JSON.parseObject(object.getJSONObject("result").toJSONString());
            List<User> listT = parse(token, objsub.getJSONArray("list").toJSONString());
            usersDingding.addAll(listT);
            if(day>120)
            {  day = day-120;            System.out.println("大于120天，重置成120天");}
            else
                break;
        }
        Iterator<User> it = usersDingding.listIterator();
        while (it.hasNext()) {
            User item = it.next();
            if (item.getUser_name().equals("详见附件表格"))
                it.remove();
        }
        return usersDingding;
    }
    public static String accountString() throws ParseException, ApiException {
        List<User> usersDingding = parseall();
        String str="";
        for (User u:usersDingding
             ) {
            str=str+u.accountToString();
            str=str+":";
        }
        return  str;
    }
    public static void main(String[] args) throws ParseException, ApiException {



        //System.out.println(accountString());

        List<User> usersDingding = parseall();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String tokenC = c.get_token();
        List<User> userList=c.get_user(tokenC);

        for(int i =0;i<usersDingding.size();i++)
        {
            if(usersDingding.get(i).getUser_name().equals("详见附件表格"))
                continue;
            int index = 0;
            for (User u:userList) {
                if(usersDingding.get(i).getAccount()!=null&&usersDingding.get(i).getAccount().equals(u.getAccount()))
                {

                    index = 1;
                    usersDingding.get(i).setExpiryDate(u.getExpiryDate());
                    System.out.println(u.getAccount()+":"+sdf.format(usersDingding.get(i).getDingding_expiryDate())+":"+sdf.format(u.getExpiryDate())+":"+usersDingding.get(i).getCounnt_time()+"秒:"+u.getCounnt_time()+"秒");
                    if(usersDingding.get(i).getDingding_expiryDate().compareTo(u.getExpiryDate())!=0) {
                        System.out.println("********************************");
                        System.out.println("时间不一致");
                        System.out.println("********************************");
                    }
                    break;
                }
                else if(usersDingding.get(i).getAccountArray()!=null)
                {

                    for(int j=0;j<usersDingding.get(i).getAccountArray().length;j++)
                    {
                        if(usersDingding.get(i).getAccountArray()[j].equals(u.getAccount()))
                        {
                            usersDingding.get(i).setExpiryDate(u.getExpiryDate());
                            System.out.println(usersDingding.get(i).getAccountArray()[j]+":"+sdf.format(usersDingding.get(i).getDingding_expiryDate())+":"+sdf.format(u.getExpiryDate())+":"+usersDingding.get(i).getCounnt_time()+"秒:"+u.getCounnt_time()+"秒");
                            index++;
                            if(usersDingding.get(i).getDingding_expiryDate().compareTo(u.getExpiryDate())!=0) {
                                System.out.println("********************************");
                                System.out.println("时间不一致");
                                System.out.println("********************************");
                            }
                        }
                    }
                }

            }
            if(index>0) {
                System.out.println(usersDingding.get(i).getUser_name() + ":" + usersDingding.get(i).getRemark() + ":" + Integer.toString(index) + ":" + Integer.toString(i + 1));

            }
            else {
                System.out.println("********************************");
                System.out.println(usersDingding.get(i).getUser_name() + ":" + usersDingding.get(i).accountToString() + ":" + usersDingding.get(i).getRemark());
                System.out.println("********************************");
            }
            System.out.println("");
        }
        for(int i =0;i<usersDingding.size();i++)
        {
            System.out.println(usersDingding.get(i).getUser_name() + " " + usersDingding.get(i).accountToString()+" "+sdf.format(usersDingding.get(i).getDingding_expiryDate())+" "+sdf.format(usersDingding.get(i).getExpiryDate()));
        }
    }

}
