package com.yukibun.yukidoc.Service;

import com.yukibun.yukidoc.Entity.Doc;
import com.yukibun.yukidoc.Entity.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface DocManageService {

    String getAi_summary(Integer id);

    Doc getDocById(int id);

    List<Doc> getDocsByFatherId(Integer id);

    void createDoc(MultipartFile file);

    void deleteDoc(Integer id);

    void createFolder(String folderName);

    List<Doc> searchDoc(String query);

    List<Tag> getTags();

    List<Doc> getDocListByTagId(Integer tagId);

    String getNoteByDocId(Integer docId);

    void insertTag(Tag tag);

    void saveNote(Integer docId, String note);
}
