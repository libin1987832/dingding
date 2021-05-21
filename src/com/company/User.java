package com.company;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {
    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_sell() {
        return user_sell;
    }

    public void setUser_sell(String user_sell) {
        this.user_sell = user_sell;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    private String user_name;
    private String user_sell;
    private Date expiryDate;

    public String getRemark() {
        return remark;
    }
    private static String REGEX_CHINESE = "[\u4e00-\u9fa5]";// 中文正则
    public void setRemark(String remark) {
        this.remark = remark;
        String stringremark = remark.replaceAll(REGEX_CHINESE, " ").trim();
        stringremark=stringremark.replace(",","");
        stringremark=stringremark.replace("，","");
        stringremark=stringremark.replace(":","");
        stringremark=stringremark.replace(":","");
        String[] remarkArray=stringremark.split(" ");
        List<String> accountList = new ArrayList<String>();
        for(String re:remarkArray) {
            String pattern = "^([a-z0-9A-Z]+[-|\\\\.]?)+[a-z0-9A-Z]@tv.com$";

            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);

            // 现在创建 matcher 对象
            Matcher m = r.matcher(re);
            if (m.find())
                accountList.add(re.trim());
        }
        if(accountList.size() > 1) {
            this.accountArray = new String[accountList.size()];
            for(int i=0;i<accountList.size();i++)
                this.accountArray[i]=accountList.get(i);
            this.account = null;
        }else if(accountList.size() == 1){
            this.account = accountList.get(0);
            this.accountArray=null;
        }

    }

    private String remark;




    public Date getDingding_expiryDate() {
        return dingding_expiryDate;
    }

    public void setDingding_expiryDate(Date dingding_expiryDate) {
        this.dingding_expiryDate = dingding_expiryDate;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getDingding_joinDate() {
        return dingding_joinDate;
    }

    public void setDingding_joinDate(Date dingding_joinDate) {
        this.dingding_joinDate = dingding_joinDate;
    }

    private Date dingding_joinDate;
    private Date dingding_expiryDate;
    private Date joinDate;
    private String account;

    public String[] getAccountArray() {
        return accountArray;
    }

    public void setAccountArray(String[] accountArray) {
        this.accountArray = accountArray;
    }

    private String[] accountArray;
    private int userId;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    private String result;

    public String getDingdingId() {
        return dingdingId;
    }

    public void setDingdingId(String dingdingId) {
        this.dingdingId = dingdingId;
    }

    private String dingdingId;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    private String processId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;
    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String outAccout = account;
        /*if(account==null&&accountArray==null)
        {
            outAccout="只有广安的由于结尾不是@tv.ocm导致";
        }else */
        if(account==null&&accountArray.length>1)
        {
            outAccout="";
            for(String s:accountArray)
                outAccout=outAccout+" "+s;
        }
        if(dingding_expiryDate!=null&&expiryDate!=null)
        {
            return  user_name +
                    "," + user_sell +
                    "," + remark+
                    "," + df.format(dingding_joinDate) +
                    "," + df.format(dingding_expiryDate) +
                    "," + df.format(joinDate) +
                    "," +  df.format(expiryDate) +
                    "," + outAccout +
                    "," + userId +
                    "," + result +","+dingdingId +","+processId +"\n";
        }
       else if(dingding_expiryDate!=null){
            return  user_name +
                    "," + user_sell +
                    "," + remark +
                    "," + df.format(dingding_expiryDate) +
                    "," + expiryDate +
                    "," + outAccout +
                   "," + userId +
                    "," + result +","+dingdingId +","+processId +"\n";
        }
       else if(expiryDate!=null)
        {
            return  user_name +
                    "," + user_sell +
                    "," + remark +
                    "," + dingding_joinDate +
                    "," + dingding_expiryDate +
                    "," + df.format(joinDate)+
                    "," +  df.format(expiryDate) +
                    "," + outAccout +
                        "," + userId +
                    "," + result +","+dingdingId +","+processId +"\n";
        }else return "";
    }
}
