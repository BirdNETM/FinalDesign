package com.yukibun.yukidoc.Controller;

import com.yukibun.yukidoc.Entity.Doc;
import com.yukibun.yukidoc.Entity.Tag;
import com.yukibun.yukidoc.Service.DocManageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/docs")
@RequiredArgsConstructor
public class DocManageController {

    private final DocManageService docManageService;

    @GetMapping("/getDoc/{id}")
    public Doc getDocById(@PathVariable Integer id) {
        return docManageService.getDocById(id);
    }

    @GetMapping("/getDocList/{id}")
    public List<Doc> getDocsByFatherId(@PathVariable Integer id) {
        return docManageService.getDocsByFatherId(id);
    }

    @PostMapping(value = "/createDoc")
    public ResponseEntity<?> createDoc(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {

        System.out.println("Content-Type: " + request.getContentType());

        docManageService.createDoc(file);

        //return ResponseEntity.ok().build();
        return ResponseEntity.ok("录入成功");
    }

    @PostMapping("/createFolder/{FolderName}")
    public void createFolder(@PathVariable String FolderName) {
        docManageService.createFolder(FolderName);
    }

    @DeleteMapping("/deleteDoc/{id}")
    public void deleteDoc(@PathVariable Integer id) {
        docManageService.deleteDoc(id);
    }

    // 路径改成不带中文的，把查询词用 ?query=xxx 传递
    @GetMapping("/searchDoc")
    public List<Doc> searchDoc(@RequestParam String query) {
        return docManageService.searchDoc(query);
    }

    @GetMapping("/getAi_summary/{id}")
    public String getAi_summary(@PathVariable Integer id) {
        return docManageService.getAi_summary(id);

    }

    @GetMapping("/getTags")
    public List<Tag> getTags() {
        return docManageService.getTags();
    }

    @GetMapping("/getDocListByTagId/{tag_id}")
    public List<Doc> getDocListByTagId(@PathVariable Integer tag_id) {
        return docManageService.getDocListByTagId(tag_id);
    }

    @GetMapping("/getNoteByDocId/{doc_id}")
    public String getNoteByDocId(@PathVariable Integer doc_id) {
        return docManageService.getNoteByDocId(doc_id);
    }

    @PostMapping("/insertTag")
    public void insertTag(@RequestBody Tag tag) {
        docManageService.insertTag(tag);
    }

    @PostMapping("/saveNote")
    public void saveNote(@RequestBody Map<String, Object> params) {
        // 强制转换或 toString 转换
        Integer docId = Integer.valueOf((params.get("doc_id").toString()));
        String note = (String) params.get("note");

        docManageService.saveNote(docId, note);
    }

    @GetMapping("/reSetAI_summary/{doc_id}")
    public String reSetAI_summary(@PathVariable Integer doc_id) {
        return docManageService.reSetAI_summary(doc_id);
    }

}
