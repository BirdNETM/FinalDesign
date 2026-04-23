package com.yukibun.yukidoc.Mapper;

import com.yukibun.yukidoc.Entity.Doc;
import com.yukibun.yukidoc.Entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocManageMapper {

    @Select("SELECT d.*, v.text AS doc_content " +
            "FROM docs d " +
            "JOIN vector v ON d.doc_id = v.doc_id " +
            "WHERE d.doc_id = #{id}")
    Doc getDocById(int id);

    @Select("select * from docs where father_id = #{fatherId}")
    List<Doc> getDocsByFatherId(int fatherId);

    @Insert("INSERT INTO docs " +
            "(owner_id, connected_note_id, is_folder, father_id, doc_name, doc_url) " +
            "VALUES " +
            "(#{doc.owner_id}, #{doc.connected_note_id}, #{doc.is_folder}, #{doc.father_id}, #{doc.doc_name}, #{doc.doc_url})")
    @Options(useGeneratedKeys = true, keyProperty = "doc.doc_id")  // 👈 关键！加这一行
    void createDoc(@Param("doc") Doc doc);

    @Delete("delete from docs where doc_id = #{id}")
    void deleteDoc(Integer id);

    @Select("select summary from vector where doc_id = #{id}")
    String getAi_summary(Integer id);

    @Select("select * from tags")
    List<Tag> getTags();

    @Select("SELECT d.* FROM docs d " +
            "JOIN doc_tag dt ON d.doc_id = dt.doc_id " +
            "WHERE dt.tag_id = #{tagId}")
    List<Doc> getDocListByTagId(Integer tagId);

    @Select("SELECT v.text " +
            "FROM docs d " +
            "JOIN vector v ON d.connected_note_id = v.doc_id " +
            "WHERE d.doc_id = #{docId}")
    String getNoteByDocId(Integer docId);

    @Insert("INSERT INTO tags(tag_name, tag_content) VALUES(#{tag_name}, #{tag_content})")
    @Options(useGeneratedKeys = true, keyProperty = "tag_id")
    void insertTag(Tag tag);

    @Update("UPDATE vector SET text = #{note} WHERE doc_id = #{docId}")
    void updateContentByDocId(@Param("docId") Integer docId, @Param("note") String note);

    @Insert("INSERT INTO vector(doc_id) values (#{newDocId})")
    void createDocVector(int newDocId);

}
