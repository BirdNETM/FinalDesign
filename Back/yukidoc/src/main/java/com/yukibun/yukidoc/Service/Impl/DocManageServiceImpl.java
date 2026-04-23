package com.yukibun.yukidoc.Service.Impl;

import com.yukibun.yukidoc.Entity.Doc;
import com.yukibun.yukidoc.Entity.Tag;
import com.yukibun.yukidoc.Mapper.DocManageMapper;
import com.yukibun.yukidoc.Service.DocManageService;
import com.yukibun.yukidoc.Utils.FileUtils;
import com.yukibun.yukidoc.Utils.TagUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocManageServiceImpl implements DocManageService {

    private final DocManageMapper docMapper;

    @Override
    public String getAi_summary(Integer id) {
        String summary = docMapper.getAi_summary(id);
        if(summary == null){
            FileUtils.callPythonAISummary(id);
            return getAi_summary(id);
        }else{
            return summary;
        }

    }

    @Override
    public Doc getDocById(int id) {
        return docMapper.getDocById(id);
    }

    @Override
    public List<Doc> getDocsByFatherId(Integer id) {
        return docMapper.getDocsByFatherId(id);
    }

    @Override
    public void createDoc(MultipartFile file) {
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
            String rootPath = "D:/FinalDesign/Date/privatefiles/1";
            Path filePath = Paths.get(rootPath, uniqueName);
            Files.createDirectories(filePath.getParent());

            // 4️⃣ 保存文件到磁盘
            file.transferTo(filePath.toFile());

            // 5️⃣ 存数据库
            Doc doc = new Doc();
            doc.setDoc_name(originalName);  // 保存原始文件名
            doc.setIs_folder(0);
            doc.setOwner_id(1);     // 用户部分
            doc.setFather_id(0);   // 分配端
            doc.setDoc_url(String.valueOf(filePath));

            docMapper.createDoc(doc);
            int newDocId = doc.getDoc_id();
            docMapper.createDocVector(newDocId);
            String doc_content = FileUtils.callPythonOCR(newDocId);
            doc.setDoc_content(doc_content);
            docMapper.updateContentByDocId(newDocId, doc_content);
            System.out.println("doc content is " + docMapper.getDocById(newDocId).getDoc_content());
            FileUtils.callPythonBertStore(newDocId);
            FileUtils.callPythonAISummary(newDocId);
            TagUtils.callPythonSetDocTag(newDocId);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    @Override
    public void deleteDoc(Integer id) {
        // 1️⃣ 查询文档信息
        Doc doc = docMapper.getDocById(id);
        if (doc == null) {
            throw new RuntimeException("文档不存在，id=" + id);
        }

        String url = doc.getDoc_url();
        if (url != null && !url.isEmpty()) {
            try {
                Files.deleteIfExists(Path.of(url)); // 文件不存在也不会抛异常
            } catch (IOException e) {
                throw new RuntimeException("删除文件失败: " + url, e);
            }
        }
        // 3️⃣ 删除数据库记录
        docMapper.deleteDoc(id);
    }

    @Override
    public void createFolder(String folderName) {
        Doc doc = new Doc();
        doc.setDoc_name(folderName);  // 保存原始文件名
        doc.setIs_folder(1);

        docMapper.createDoc(doc);
    }

    @Override
    public List<Doc> searchDoc(String query) {
        List<Integer> docIdList = FileUtils.callPythonBertQuery(query);
        List<Doc> docList = new ArrayList<>();
        for (Integer doc_id : docIdList) {
            Doc doc = docMapper.getDocById(doc_id);
            docList.add(doc);
        }
        return docList;
    }

    @Override
    public List<Tag> getTags() {
        return docMapper.getTags();
    }

    @Override
    public List<Doc> getDocListByTagId(Integer tagId) {
        return docMapper.getDocListByTagId(tagId);
    }

    @Override
    public String getNoteByDocId(Integer docId) {
        return docMapper.getNoteByDocId(docId);
    }

    @Override
    public void insertTag(Tag tag) {
        docMapper.insertTag(tag);
        int tagId = tag.getTag_id();
        TagUtils.callPythonUpdateTagVector(tagId);
    }

    private void updateFileContent(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (IOException e) {
            throw new RuntimeException("文件系统写入失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及多表操作，必须开启事务
    public void saveNote(Integer docId, String note) {
        // 1. 在 docs 表里根据 docId 查找是否存在 connected_note_id
        // 假设你的 Doc 实体类中有这个字段
        Doc currentDoc = docMapper.getDocById(docId);

        if (currentDoc != null && currentDoc.getConnected_note_id() != null) {
            // --- 情况 A: 存在关联笔记，执行更新 ---
            Doc noteDoc = docMapper.getDocById(currentDoc.getConnected_note_id());
            // 1.1 找到关联笔记的 doc_url (假设就是当前 doc 的 url 或者关联 doc 的 url)
            String docUrl = noteDoc.getDoc_url();

            // 1.2 修改物理文件内容
            updateFileContent(docUrl, note);

            // 1.3 修改 vector 表里的 doc_content (假设 vector 表主键或关联键是 doc_id)
            docMapper.updateContentByDocId(docId, note);

            // 1.4 (可选) 如果你希望更新向量，这里需要调用 Python 或内部算法重新计算 embedding
            FileUtils.callPythonBertStore(docId);

        } else {
            // --- 情况 B: 不存在关联笔记，执行新建 ---

            // 由于 createDoc 接收 MultipartFile，我们需要把 String 封装一下
            // 或者直接调用你内部的逻辑。这里演示封装成 MockMultipartFile
            MultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "note_" + docId + ".txt",
                    "text/plain",
                    note.getBytes(StandardCharsets.UTF_8)
            );

            this.createDoc(mockFile);
        }
    }


}
