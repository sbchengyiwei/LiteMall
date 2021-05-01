package org.linlinjava.litemall.wx;

import com.google.gson.Gson;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

import java.io.File;
import java.util.HashMap;

public class Test_1 {
    public static void main(String[] args) {
        String appid="###";
        String secret="###";
        String param="grant_type=client_credential&secret="+secret+"&appid="+appid;
        HttpRequest request=HttpRequest.get("https://api.weixin.qq.com/cgi-bin/token?"+param);
        HttpResponse response=request.send();
        Gson gson=new Gson();
        HashMap map=gson.fromJson(response.body(), HashMap.class);
        String access_token=map.get("access_token").toString();
        System.out.println(access_token);

        request=HttpRequest.post("https://api.weixin.qq.com/wxa/msg_sec_check?access_token="+access_token);
        request.contentType("application/json");
        request.charset("utf-8");
        map=new HashMap();
        map.put("content","HelloWorld");
        String json=gson.toJson(map).toString();
        request.body(json);
        response=request.send();
        map=gson.fromJson(response.body(),HashMap.class);
        String errmsg=map.get("errmsg").toString();
        System.out.println(errmsg);

        request=HttpRequest.post("https://api.weixin.qq.com/wxa/img_sec_check?access_token="+access_token);
        request.contentType("multipart/form-data");
        request.charset("utf-8");
        request.form("media",new File("D:/1.jpg"));
        response=request.send();
        map=gson.fromJson(response.body(),HashMap.class);
        errmsg=map.get("errmsg").toString();
        System.out.println(errmsg);

    }
}
