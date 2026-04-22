package com.yukibun.yukidoc.Controller;

import com.yukibun.yukidoc.Entity.Post;
import com.yukibun.yukidoc.Service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping("/getPost/{id}")
    public Post getPostById(@PathVariable Integer id) {
        return communityService.getPostById(id);
    }

    @GetMapping("/getPostList")
    public List<Post> getPostList() {
        return communityService.getPostList();
    }

    @PostMapping(value = "/createPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createPost(
            // 接收表单 JSON 字符串，自动转 Post 对象
            @RequestParam("post") String postJson,
            // 接收上传的文件
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        communityService.createPost(postJson, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam Integer post_id) {
        return communityService.downloadFile(post_id);
    }
}
