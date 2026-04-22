package com.yukibun.yukidoc.Service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukibun.yukidoc.Entity.Post;
import com.yukibun.yukidoc.Mapper.CommunityMapper;
import com.yukibun.yukidoc.Service.CommunityService;
import com.yukibun.yukidoc.Utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;

    @Override
    public List<Post> getPostList() {
        return communityMapper.getPostList();
    }

    @Override
    public Post getPostById(Integer id) {
        return communityMapper.getPostById(id);
    }


    @Override
    public void createPost(String postJson, MultipartFile file) {
        try {
            // 1. JSON 转 Post 对象
            ObjectMapper objectMapper = new ObjectMapper();
            Post post = objectMapper.readValue(postJson, Post.class);

            // 3. 上传文件（有文件才处理）
            if (file != null && !file.isEmpty()) {
                String url = FileUtils.uploadFile(file); // 文件上传返回URL
                String name = file.getOriginalFilename();
                post.setFile_url(url);
                post.setFile_name(name);
            }

            System.out.println(post);
            // 4. 插入数据库
            communityMapper.createPost(post);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("发布帖子失败");
        }
    }

    @Override
    public ResponseEntity<Resource> downloadFile(Integer postId) {
        // 1. 根据 post_id 查询帖子信息，拿到 file_url 和 file_name
        Post post = communityMapper.getPostById(postId);
        String filePath = post.getFile_url();

        // 2. 把路径转成 Spring Resource
        File file = new File(filePath);
        Resource resource = new FileSystemResource(file);

        String contentType = "application/octet-stream";
        String encodedFileName = URLEncoder.encode(post.getFile_name(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }
}
