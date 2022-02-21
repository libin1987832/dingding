package com.company;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Tibei {
    final static String accountTibei="gonggarmtzx@tv.com," +
            "qiongjxgbdst@tv.com," +
            "langkzrmtzx@tv.com," +
            "qiongjxgbdst@tv.com," +
            "nqdst@tv.com," +
            "biruxrmtzx@tv.com," +
            "jialixgbdst@tv.com," +
            "suoxrmtzx@tv.com," +
            "baqingxgbdst@tv.com," +
            "nimaxrmtzx@tv.com," +
            "seniqgbdst@tv.com," +
            "shuanghuxgbdst@tv.com," +
            "nlamurmtzx@tv.com," +
            "nanmulinrmtzx@tv.com," +
            "dingjiexrmtzx@tv.com," +
            "gangbaxrmtzx@tv.com," +
            "zbxrmtzx@tv.com," +
            "jilongxrmtzx@tv.com," +
            "jiangzixrmtzx@tv.com," +
            "dingrixrmtzx@tv.com," +
            "changdusgbdst@tv.com," +
            "jiangdaxrmtzx@tv.com," +
            "leiwuqixgbdst@tv.com," +
            "basuxrmtzx@tv.com," +
            "zuogongxrmtzx@tv.com," +
            "kmxgbdst@tv.com," +
           // "uolongxrmtzx@tv.com," +
            "luolonxrmtzx@tv.com,"+
            "alidst@tv.com," +
            "zhadaxrmtzx@tv.com," +
            "gbdjrmtzx@tv.com," +
            "mlxrmtzx@tv.com," +
            "bomixrmtzx@tv.com," +
            "nimuxrmtzx@tv.com," +
            "qushuixrmtzx@tv.com," +
           // "dazigbdst@tv.com," +
            "dazigbdst@tv.comc,"+
            "jcrmtzx@tv.com," +
            "xiaojinxrmtzx@tv.com," +
            "abxrmtzx@tv.com," +
            "ganzzrmtzx@tv.com," +
            "kangdingxrmtzx@tv.com," +
            "ludingxrmtzx@tv.com," +
            "daofuxrmtzx@tv.com," +
            "sedaxrmtzx@tv.com," +
            "dgxrmtzx@tv.com," +
            "shiquxrmtzx@tv.com," +
            "ltxrmtzx@tv.com," +
            "btxrmtzx@tv.com," +
            "xlmrtzx@tv.com," +
            "derongxrmtzx@tv.com," +
            "lszrmtzx@tv.com," +
            "mulixrmtzx@tv.com," +
            "diqingzhougbdst@tv.com," +
            "wxrmtzx1@tv.com," +
            "dqxrmtzx@tv.com," +
            "xgllsgbdst@tv.com," +
            "menyuanrmtzx@tv.com," +
            "qilianrmtzx@tv.com," +
            "hyrmtzx@tv.com," +
            "gcxrmtzx@tv.com," +
            "jianzhaxrmtzx@tv.com," +
            "gdxrmtzx2@tv.com," +
            "chenxianrmtzx@tv.com," +
            "huixrmtzx@tv.com," +
            "tcrm@tv.com," +
            "wenxrmtzx@tv.com," +
            "kangxianrmtzx@tv.com," +
            "xhrmtzx@tv.com," +
            "lixrmtzx@tv.com," +
            "liangdangxrmtzx@tv.com," +
            "wudurmtzx@tv.com," +
            "dieburmtzx@tv.com," +
            "zhuonixrmtzx@tv.com," +
            "maquxrmtzx@tv.com," +
            "xiahermtzx@tv.com," +
            "lintanxrmtzx@tv.com," +
            "zhouqurmtzx@tv.com," +
            "luquxrmtzx@tv.com," +
            "tianzhurmtzx@tv.com";
    final static String accountTibei2="longzixgbdst@tv.com,"+//1
            "bangexrmtzx@tv.com,"+//2
            "anduoxrmtzx@tv.com,"+//3
            "nierongxgbdst@tv.com,"+//4
            "shenzhaxrmtzx@tv.com,"+//5
            "angrenxrmtzx@tv.com,"+//6
            "renbuxrmtzx@tv.com,"+//7
            "bailangxrmtzx@tv.com,"+//8
            "yadonrmtzx@tv.com,"+//9
            "lazixgbdst@tv.com,"+//10
            "xietongxgbdst@tv.com,"+//11
            "chayarmtzx@tv.com,"+//12
            "linzhidst@tv.com,"+//13
            "motuoxrmtzx@tv.com,"+//14
            "langxrmtzx@tv.com,"+//15
            "ruoergxgbdst@tv.com,"+//16
            "hongyuanrmtzx@tv.com,"+//17
            "lixianrmtzx@tv.com,"+//18
            "maoxianrmtzx@tv.com,"+//19
            "jiuzhaigourmtzx@tv.com,"+//20
            "danbaxrmtzx@tv.com,"+//21
            "jiulongxrmtzx@tv.com,"+//22
            "luhuormtzx@tv.com,"+//23
            "baiyunxrmtzx@tv.com,"+//24
            "qincuormtzx@tv.com";//25



    public static String accountString()
    {
        return accountTibei.replace(',',':')+":"+accountTibei2.replace(',',':');
    }
    public static boolean isExistence(String[] accountT,String account){
        for(int i =0 ;i<accountT.length;i++)
            if(accountT[i].equals(account))
                return true;
       return false;
    }
    public static void main(String[] args) throws ParseException {
        String[] accountT = accountString().split(":");
        boolean[] joinT= new boolean[accountT.length];
        JavaNetURLRESTFulClient c = new JavaNetURLRESTFulClient();
        String token = c.get_token();
        List<User> userList=c.get_user(token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date updateTime = sdf.parse("2022-12-31");
        boolean exist = false;
        int index =1;
        for (User u:userList) {
            if(isExistence(accountT,u.getAccount()))
            {
                if(u.getExpiryDate().compareTo(updateTime)!=0) {
                    exist = true;
                    if (!c.update_user(token, u.getUserId(), updateTime))
                        System.out.println(u.getAccount() + ",跟新未成功!!!"+Integer.toString(index));
                    else
                        System.out.println(u.getAccount() + ",账户成功!!!"+Integer.toString(index));
                }
                index++;
            }
        }
        if(!exist)
            System.out.println("注册了账户时间都是2022-12-31");
        for(int i =0;i<accountT.length;i++)
        {
            boolean isExist = false;
            for (User u:userList) {
                if(accountT[i].equals(u.getAccount()))
                {
                    isExist = true;
                   System.out.println("西藏省账号 "+accountT[i]+" "+sdf.format(updateTime)+" "+sdf.format(u.getExpiryDate())+" "+Integer.toString(i+1));
                   break;
                }
            }
            if(!isExist)
                System.out.println(accountT[i]+":*账号还没有注册*:"+Integer.toString(i+1));
        }
    }
}
