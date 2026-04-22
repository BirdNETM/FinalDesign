<template>
  <div class="dc-container">
    <div class="header">
      <div class="logo">
        <i class="icon">📄</i>
        <h3>文档控制中心</h3>
      </div>
      <div class="upload-area">
        <button 
          class="upload-btn" 
          :disabled="uploadLoading" 
          @click="triggerFileInput"
        >
          {{ uploadLoading ? '正在解析文档...' : '上传新文档' }}
        </button>
        <input
          type="file"
          ref="fileInput"
          class="file-input"
          @change="handleFileUpload"
          accept=".doc,.docx,.pdf,.txt,.md"
        />
      </div>
    </div>

    <div class="main-content">
      <section class="panel folder-viewer">
        <div class="panel-header">
          <span>文件目录</span>
          <button class="add-tag-btn" @click="showAddTagModal = true">+ 添加Tag</button>
        </div>
        <div class="panel-body">
          <div class="search-section">
            <div class="search-box">
              <input
                v-model.trim="searchQuery"
                class="search-input"
                type="text"
                placeholder="Search documents"
                @keyup.enter="searchDocs"
              />
              <button
                class="search-btn"
                :disabled="searchLoading"
                @click="searchDocs"
              >
                {{ searchLoading ? 'Searching...' : 'Search' }}
              </button>
            </div>
            <div v-if="isSearchMode" class="search-toolbar">
              <span class="search-summary">
                {{ searchLoading ? 'Searching...' : `${searchResults.length} results` }}
              </span>
              <button class="clear-search-btn" @click="clearSearch">Clear</button>
            </div>
            <div v-if="searchError" class="search-error">{{ searchError }}</div>
          </div>

          <div v-if="isSearchMode" class="search-results">
            <div v-if="searchLoading" class="search-empty">Loading results...</div>
            <div v-else-if="searchResults.length === 0" class="search-empty">No documents found.</div>
            <ul v-else class="search-result-list">
              <li
                v-for="doc in searchResults"
                :key="doc.doc_id"
                class="search-result-item"
                @click="handleSearchResultClick(doc)"
              >
                <div class="search-result-title">
                  {{ doc.title || doc.doc_name || 'Untitled document' }}
                </div>
                <div v-if="doc.tag_name || doc.path" class="search-result-meta">
                  {{ doc.tag_name || doc.path }}
                </div>
                <div
                  v-if="doc.summary || doc.content || doc.doc_content"
                  class="search-result-desc"
                >
                  {{ doc.summary || doc.content || doc.doc_content }}
                </div>
              </li>
            </ul>
          </div>

          <FileTree
            v-else
            ref="fileTreeRef"
            @file-select="handleFileSelect"
          />
        </div>
      </section>

      <!-- 添加 Tag 弹窗 -->
      <div v-if="showAddTagModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>添加新标签</h3>
            <button class="close-btn" @click="closeAddTagModal">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-item">
              <label>标签名称：</label>
              <input v-model="newTag.tag_name" placeholder="请输入标签名称" />
            </div>
            <div class="form-item">
              <label>标签内容：</label>
              <textarea v-model="newTag.tag_content" placeholder="请输入标签内容"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="cancel-btn" @click="closeAddTagModal">取消</button>
            <button 
              class="submit-btn" 
              :disabled="!newTag.tag_name || tagLoading" 
              @click="submitTag"
            >
              {{ tagLoading ? '提交中...' : '提交' }}
            </button>
          </div>
        </div>
      </div>

      <section class="panel doc-viewer">
        <div class="panel-header">
          内容预览 {{ currentFile ? `- ${currentFile.name}` : '' }}
        </div>
        <div class="panel-body">
          <DocViewer
            v-if="currentFile"
            :file="currentFile"
          />
          <div v-else class="empty-state">
            <p>未选择文档</p>
            <span>从左侧目录选择一个文件进行查看</span>
          </div>
        </div>
      </section>

      <section class="panel note-viewer">
        <div class="panel-header">关联笔记</div>
        <div class="panel-body">
          <NoteViewer
            v-if="currentFile"
            :file="currentFile"
          />
          <div v-else class="empty-state">
            <p>笔记区</p>
            <span>选择文档后可编辑笔记</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import FileTree from './FileTree.vue'
import DocViewer from './DocViewer.vue'
import NoteViewer from './NoteViewer.vue'
import axios from 'axios'

export default {
  name: 'DocumentCenter',
  components: {
    FileTree,
    DocViewer,
    NoteViewer
  },
  data() {
    return {
      currentFile: null,
      uploadLoading: false,
      searchQuery: '',
      searchLoading: false,
      searchResults: [],
      searchError: '',
      isSearchMode: false,
      showAddTagModal: false,
      tagLoading: false,
      newTag: {
        tag_name: '',
        tag_content: ''
      }
    }
  },
  methods: {
    buildFileNode(doc) {
      return {
        id: doc.doc_id,
        name: doc.title || doc.doc_name || 'Untitled document',
        type: 'file',
        path: doc.path
      };
    },

    async searchDocs() {
      const query = this.searchQuery.trim();

      if (!query) {
        this.clearSearch();
        return;
      }

      try {
        this.searchLoading = true;
        this.searchError = '';
        this.isSearchMode = true;

        const response = await axios.get(
          `http://localhost:8080/docs/serachDoc/${encodeURIComponent(query)}`
        );

        this.searchResults = Array.isArray(response.data) ? response.data : [];
      } catch (error) {
        console.error('Search docs failed:', error);
        this.searchResults = [];
        this.searchError = 'Search failed. Please check the backend service.';
      } finally {
        this.searchLoading = false;
      }
    },

    clearSearch() {
      this.searchQuery = '';
      this.searchLoading = false;
      this.searchResults = [];
      this.searchError = '';
      this.isSearchMode = false;
    },

    handleSearchResultClick(doc) {
      this.handleFileSelect(this.buildFileNode(doc));
    },

    closeAddTagModal() {
      this.showAddTagModal = false;
      this.newTag = { tag_name: '', tag_content: '' };
    },

    async submitTag() {
      if (!this.newTag.tag_name) return;

      try {
        this.tagLoading = true;
        const response = await axios.post(
          'http://localhost:8080/docs/insertTag',
          this.newTag
        );

        if (response.status === 200) {
          alert('标签添加成功！');
          this.closeAddTagModal();
          // 刷新文件树
          if (this.$refs.fileTreeRef) {
            await this.$refs.fileTreeRef.getTags();
          }
        }
      } catch (error) {
        console.error('添加标签失败:', error);
        alert('添加标签失败，请检查网络或后端服务');
      } finally {
        this.tagLoading = false;
      }
    },

    handleFileSelect(node) {
      if (node.type !== 'file') return;
      this.currentFile = { ...node };
    },

    triggerFileInput() {
      this.$refs.fileInput.click();
    },

    async handleFileUpload(e) {
      const file = e.target.files[0];
      if (!file) return;

      const formData = new FormData();
      formData.append('file', file);

      try {
        this.uploadLoading = true;
        
        // 1. 发起上传请求。因为后端处理完才返回，所以程序会在这里“等待”解析完成
        const response = await axios.post(
          'http://localhost:8080/docs/createDoc',
          formData,
          { 
            headers: { 'Content-Type': 'multipart/form-data' },
            // 如果解析大文件时间较长，建议增加超时限制（单位ms，这里设为5分钟）
            timeout: 300000 
          }
        );

        // 2. 收到 200 返回，说明数据库已经写入了新文档
        if (response.status === 200) {
          console.log('解析完成，正在刷新目录...');
          
          // 3. 调用 FileTree 的刷新方法
          if (this.$refs.fileTreeRef) {
            await this.$refs.fileTreeRef.getTags(); 
          }
          
          alert('上传并解析成功！');
        }

      } catch (error) {
        console.error('流程失败：', error);
        const errorMsg = error.response?.data?.message || '服务器解析超时或异常';
        alert(`上传失败: ${errorMsg}`);
      } finally {
        this.uploadLoading = false;
        this.$refs.fileInput.value = ''; 
      }
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.dc-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
  overflow: hidden;
}
.header {
  height: 64px;
  background-color: #001529;
  color: #fff;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  z-index: 10;
}
.main-content {
  flex: 1;
  display: flex;
  padding: 12px;
  gap: 12px;
  overflow: hidden;
}
.panel {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  overflow: hidden;
}
.panel-header {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
  font-weight: bold;
  color: #333;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-tag-btn {
  padding: 4px 10px;
  font-size: 12px;
  color: #1890ff;
  border: 1px solid #1890ff;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.add-tag-btn:hover {
  background: #1890ff;
  color: #fff;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  width: 400px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.modal-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-item textarea {
  height: 100px;
  resize: none;
}

.modal-footer {
  padding: 16px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:disabled {
  background: #bfbfbf;
  cursor: not-allowed;
}

.panel-body {
  flex: 1;
  overflow: auto;
  padding: 10px;
}
.search-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}
.search-box {
  display: flex;
  gap: 8px;
}
.search-input {
  flex: 1;
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
}
.search-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.12);
}
.search-btn,
.clear-search-btn {
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.search-btn {
  padding: 8px 14px;
  background: #1890ff;
  color: #fff;
  white-space: nowrap;
}
.search-btn:disabled {
  background: #bfbfbf;
  cursor: not-allowed;
}
.clear-search-btn {
  padding: 4px 10px;
  background: #f5f5f5;
  color: #666;
}
.clear-search-btn:hover {
  background: #e8e8e8;
}
.search-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}
.search-error {
  padding: 8px 10px;
  border-radius: 4px;
  background: #fff2f0;
  color: #cf1322;
  font-size: 12px;
}
.search-results {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.search-result-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.search-result-item {
  padding: 10px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.search-result-item:hover {
  border-color: #91d5ff;
  background: #f6ffed;
}
.search-result-title {
  font-size: 13px;
  font-weight: 600;
  color: #262626;
  line-height: 1.5;
}
.search-result-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
  word-break: break-all;
}
.search-result-desc {
  margin-top: 6px;
  font-size: 12px;
  color: #595959;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}
.search-empty {
  padding: 16px 0;
  text-align: center;
  color: #999;
  font-size: 13px;
}
.folder-viewer { flex: 0 0 240px; }
.doc-viewer    { flex: 1; min-width: 400px; }
.note-viewer   { flex: 0 0 350px; }

.upload-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}
.upload-btn:disabled {
  background-color: #bfbfbf;
  cursor: not-allowed;
}
.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
  text-align: center;
}
.file-input { display: none; }
</style>
