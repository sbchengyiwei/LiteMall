package org.linlinjava.litemall.wx.web;

import com.google.gson.Gson;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.storage.StorageService;
import org.linlinjava.litemall.core.util.CharUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallStorage;
import org.linlinjava.litemall.db.service.LitemallStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 对象存储服务
 */
@RestController
@RequestMapping("/wx/storage")
@Validated
public class WxStorageController {
    private final Log logger = LogFactory.getLog(WxStorageController.class);

    @Autowired
    private StorageService storageService;
    @Autowired
    private LitemallStorageService litemallStorageService;

    @Value("${litemall.wx.app-id}")
    private String appid;
    @Value("${litemall.wx.app-secret}")
    private String secret;

    private String generateKey(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        String suffix = originalFilename.substring(index);

        String key = null;
        LitemallStorage storageInfo = null;

        do {
            key = CharUtil.getRandomString(20) + suffix;
            storageInfo = litemallStorageService.findByKey(key);
        }
        while (storageInfo != null);

        return key;
    }

    @PostMapping("/upload")
    public Object upload(@RequestParam("file") MultipartFile file) throws IOException {
        File temp=File.createTempFile(UUID.randomUUID().toString(),"jpg");
        InputStream in = file.getInputStream();
        BufferedInputStream bin=new BufferedInputStream(in);
        FileOutputStream out=new FileOutputStream(temp);
        BufferedOutputStream bout=new BufferedOutputStream(out);
        int i=bin.read();
        while (i!=-1){
            bout.write(i);
            i=bin.read();
        }
        in.close();
        out.close();
        String param="grant_type=client_credential&secret="+secret+"&appid="+appid;
        HttpRequest request=HttpRequest.get("https://api.weixin.qq.com/cgi-bin/token?"+param);
        HttpResponse response=request.send();
        Gson gson=new Gson();
        HashMap map=gson.fromJson(response.body(), HashMap.class);
        String access_token=map.get("access_token").toString();
        request=HttpRequest.post("https://api.weixin.qq.com/wxa/img_sec_check?access_token="+access_token);
        request.contentType("multipart/form-data");
        request.charset("utf-8");
        request.form("media",temp);
        response=request.send();
        map=gson.fromJson(response.body(),HashMap.class);
        String errmsg=map.get("errmsg").toString();
        if (!"ok".equals(errmsg)){
            return ResponseUtil.badArgumentValue();
        }
        temp.delete();

        String originalFilename = file.getOriginalFilename();
        LitemallStorage litemallStorage = storageService.store(file.getInputStream(), file.getSize(), file.getContentType(), originalFilename);
        return ResponseUtil.ok(litemallStorage);
    }

    /**
     * 访问存储对象
     *
     * @param key 存储对象key
     * @return
     */
    @GetMapping("/fetch/{key:.+}")
    public ResponseEntity<Resource> fetch(@PathVariable String key) {
        LitemallStorage litemallStorage = litemallStorageService.findByKey(key);
        if (key == null) {
            return ResponseEntity.notFound().build();
        }
        if (key.contains("../")) {
            return ResponseEntity.badRequest().build();
        }
        String type = litemallStorage.getType();
        MediaType mediaType = MediaType.parseMediaType(type);

        Resource file = storageService.loadAsResource(key);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(mediaType).body(file);
    }

    /**
     * 访问存储对象
     *
     * @param key 存储对象key
     * @return
     */
    @GetMapping("/download/{key:.+}")
    public ResponseEntity<Resource> download(@PathVariable String key) {
        LitemallStorage litemallStorage = litemallStorageService.findByKey(key);
        if (key == null) {
            return ResponseEntity.notFound().build();
        }
        if (key.contains("../")) {
            return ResponseEntity.badRequest().build();
        }

        String type = litemallStorage.getType();
        MediaType mediaType = MediaType.parseMediaType(type);

        Resource file = storageService.loadAsResource(key);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(mediaType).header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

}
