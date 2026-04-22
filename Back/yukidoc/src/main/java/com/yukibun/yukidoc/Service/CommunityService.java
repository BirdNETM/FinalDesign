package com.yukibun.yukidoc.Service;

import com.yukibun.yukidoc.Entity.Post;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface CommunityService {

    List<Post> getPostList();

    Post getPostById(Integer id);

    void createPost(String postJson, MultipartFile file);

    ResponseEntity<Resource> downloadFile(Integer postId);
}
