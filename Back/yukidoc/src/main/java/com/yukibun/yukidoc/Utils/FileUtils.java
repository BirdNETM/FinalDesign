package com.yukibun.yukidoc.Utils;

import com.yukibun.yukidoc.Entity.Doc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.yukibun.yukidoc.Service.DocManageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class FileUtils {


    // RestTemplate 发送请求（静态内部创建，不用依赖注入）
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    // Python 统一服务地址
    private static final String PYTHON_OCR_URL = "http://localhost:8000/api";
    private static final String PYTHON_BERT_URL = "http://localhost:8001/api";
    // ========================
    // 调用 Python OCR
    // ========================
    public static String callPythonOCR(int doc_id) {
        try {
            String url = PYTHON_OCR_URL + "/ocr?doc_id=" + doc_id;
            Map<String, Object> result = REST_TEMPLATE.postForObject(url, null, Map.class);
            String doc_content = (String) result.get("data");
            System.out.println("✅ OCR 调用成功：" + result);
            return doc_content;
        } catch (Exception e) {
            System.err.println("❌ OCR 调用失败：" + e.getMessage());
        }
        return null;
    }

    // ========================
    // 调用 Python BERT
    // ========================
    public static void callPythonBertStore(int doc_id) {
        try {
            String url = PYTHON_BERT_URL + "/bert_store?doc_id=" + doc_id;
            Map<String, Object> result = REST_TEMPLATE.postForObject(url, null, Map.class);
            System.out.println("✅ BERT 调用成功：" + result);
        } catch (Exception e) {
            System.err.println("❌ BERT 调用失败：" + e.getMessage());
        }
    }

    public static List<Integer> callPythonBertQuery(String query) {
        try {
            String url = PYTHON_BERT_URL + "/bert_query?query=" + query;

            // 1. 接收 Python 返回的完整 JSON
            Map<String, Object> result = REST_TEMPLATE.postForObject(url, null, Map.class);

            // 2. 获取 data 列表
            List<Object> dataList = (List<Object>) result.get("data");
            if (dataList == null || dataList.isEmpty()) {
                System.out.println("⚠️ BERT 返回空数据");
                return new ArrayList<>();
            }

            // 3. 遍历解析：每一项都是 [int, String, double] 数组！
            List<Integer> docIdList = new ArrayList<>();
            for (Object item : dataList) {
                // 把每一项转成 List（因为 Python 返回的是列表）
                List<Object> info = (List<Object>) item;
                int dic_id = ((Number) info.get(0)).intValue();

                docIdList.add(dic_id);
            }

            System.out.println("✅ BERT 调用成功，返回 " + docIdList.size() + " 条文档");
            return docIdList;

        } catch (Exception e) {
            System.err.println("❌ BERT 调用失败：");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    // ========================
    // 调用 Python AI_SUMMARY
    // ========================
    public static void callPythonAISummary(int doc_id) {
        try {
            String url = PYTHON_OCR_URL + "/ai_summary?doc_id=" + doc_id;
            Map<String, Object> result = REST_TEMPLATE.postForObject(url, null, Map.class);
            System.out.println("✅ ai 调用成功：" + result);
        } catch (Exception e) {
            System.err.println("❌ ai 调用失败：" + e.getMessage());
        }
    }




    public static String uploadFile(MultipartFile file, String rootPath) {
        try {
            // 1️⃣ 获取原文件名
            String originalName = file.getOriginalFilename();

            // 2️⃣ 生成唯一文件名（UUID + 后缀）
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String uniqueName = UUID.randomUUID() + ext;

            // 3️⃣ 文件存储路径（可加分桶目录）

            Path filePath = Paths.get(rootPath, uniqueName);
            Files.createDirectories(filePath.getParent());

            // 4️⃣ 保存文件到磁盘
            file.transferTo(filePath.toFile());

            return String.valueOf(filePath);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    // 重载方法：不传路径就用默认路径
    public static String uploadFile(MultipartFile file) {
        return uploadFile(file, "D:/FinalDesign/Date/publicfiles/1");
    }

    public static void callPythonSetDocTag(int newDocId) {
        try {
            String url = PYTHON_BERT_URL + "/set_doc_tag?doc_id=" + newDocId;
            Map<String, Object> result = REST_TEMPLATE.postForObject(url, null, Map.class);
            System.out.println("✅ BERT 调用成功：" + result);
        } catch (Exception e) {
            System.err.println("❌ BERT 调用失败：" + e.getMessage());
        }
    }


}