package org.linlinjava.litemall.wx.web;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.wx.service.UserTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/wx/test")
public class TestController {
    @Autowired
    private WxMaService wxMaService;
    @PostMapping("login")
    public Object login(String code,String nickName,String gender,String avatarUrl){
        try{
            WxMaJscode2SessionResult result= wxMaService.getUserService().getSessionInfo(code);
            String openid=result.getOpenid();
            //保存数据记录
            int id=160;
            String token= UserTokenManager.generateToken(id);
            HashMap map=new HashMap();
            map.put("token",token);
            return ResponseUtil.ok(map);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseUtil.badArgument();
        }

    }
}
