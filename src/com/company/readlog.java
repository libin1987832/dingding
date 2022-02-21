package com.company;
import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class readlog {
    private static Object HashMap;

    /**
     * 功能：Java读取txt文件的内容
     * 步骤：1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     * 4：一行一行的输出。readline()。
     * 备注：需要考虑的是异常情况
     * @param filePath
     */
    public static Map readTxtFile(String filePath){
        Map emailTime =new HashMap<String, Date>();
        try {
            String encoding="UTF-8";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String time = lineTxt.substring(lineTxt.indexOf("product-service-sign-lang-ws.")+29, lineTxt.indexOf(".log:")-2);
                    if(lineTxt.indexOf("email\":\"")>0){
                        String email = lineTxt.substring(lineTxt.indexOf("email\":\"")+8, lineTxt.indexOf("\",\"funcRemTime\":"));
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date d=df.parse(time);
                        if(!emailTime.containsKey(email))
                            emailTime.put(email,d);
                        else{
                            Date old = (Date) emailTime.get(email);
                            if(old.compareTo(d)==-1)
                            {
                                emailTime.remove(email);
                                emailTime.put(email,d);
                            }
                        }
                    }
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return emailTime;
    }

    public static void main(String argv[]){
        String filePath = "D:\\login.log";
//      "res/";
        Map m = readTxtFile(filePath);
        Set<String> st = m.keySet();
        Iterator<String> is = st.iterator();
        while(is.hasNext())
        {
            String key = is.next();
            Date d = (Date) m.get(key);
            System.out.println(key+" "+ d.toString());
        }
    }

}