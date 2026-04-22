package com.yukibun.yukidoc.Utils;

import com.yukibun.yukidoc.Entity.Doc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class TagUtils {


    // RestTemplate 发送请求（静态内部创建，不用依赖注入）
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    // Python 统一服务地址
    private static final String PYTHON_OCR_URL = "http://localhost:8000/api";
    private static final String PYTHON_BERT_URL = "http://localhost:8001/api";

    public static void callPythonSetDocTag(int newDocId) {
        try {
            String url = PYTHON_BERT_URL + "/set_doc_tag?doc_id=" + newDocId;
            Map<String, Object> result = REST_TEMPLATE.postForObject(url, null, Map.class);
            System.out.println("✅ BERT 调用成功：" + result);
        } catch (Exception e) {
            System.err.println("❌ BERT 调用失败：" + e.getMessage());
        }
    }

    public static void callPythonUpdateTagVector(int tag_id) {
        try {
            String url = PYTHON_BERT_URL + "/update_tag_vector?tag_id=" + tag_id;
            Map<String, Object> result = REST_TEMPLATE.postForObject(url, null, Map.class);
            System.out.println("✅ BERT 调用成功：" + result);
        } catch (Exception e) {
            System.err.println("❌ BERT 调用失败：" + e.getMessage());
        }
    }


}
