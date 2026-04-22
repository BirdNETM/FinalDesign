package com.yukibun.yukidoc.Mapper;

import com.yukibun.yukidoc.Entity.Post;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommunityMapper {
    @Select("select * from posts")
    List<Post> getPostList();

    @Select("select * from posts where post_id = #{id}")
    Post getPostById(Integer id);

    @Insert("INSERT INTO posts(owner_id, owner_name, post_title, post_content,  file_name, file_url) " +
            "VALUES(#{owner_id}, #{owner_name}, #{post_title}, #{post_content},  #{file_name}, #{file_url})")
    void createPost(Post post);
}
