<template>
  <div class="note-container">
    <div v-if="loading" class="loading-state">
      <span class="spinner"></span> 加载笔记中...
    </div>
    
    <div v-else class="editor-wrapper">
      <textarea 
        v-model="noteContent" 
        placeholder="在此输入笔记内容..."
        class="note-area"
      ></textarea>
      
      <div class="footer-actions">
        <span class="word-count">字数: {{ noteContent.length }}</span>
        <button 
          @click="saveNote" 
          class="save-btn" 
          :disabled="saving"
        >
          {{ saving ? '正在保存...' : '保存笔记' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  props: ['file'],
  data() {
    return {
      noteContent: '',
      loading: false,
      saving: false
    }
  },
  watch: {
    'file.id': {
      handler(newId) {
        if (newId) this.fetchNote(newId);
      },
      immediate: true
    }
  },
  methods: {
    async fetchNote(docId) {
      this.loading = true;
      try {
        const res = await axios.get(`http://localhost:8080/docs/getNoteByDocId/${docId}`);
        // 注意：如果后端返回的是 String，直接赋值即可
        this.noteContent = res.data || '';
      } catch (e) {
        console.error("加载笔记失败", e);
        this.noteContent = '';
      } finally {
        this.loading = false;
      }
    },
    async saveNote() {
      if (!this.file || !this.file.id) return;
      
      this.saving = true;
      try {
        await axios.post('http://localhost:8080/docs/saveNote', {
          doc_id: this.file.id,
          note: this.noteContent
        });
        alert('笔记保存成功');
      } catch (e) {
        console.error("保存笔记失败", e);
        alert('保存失败，请检查后端服务');
      } finally {
        this.saving = false;
      }
    }
  }
}
</script>

<style scoped>
/* 撑满整个 Panel 空间 */
.note-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 核心：让 textarea 自动撑开 */
.note-area {
  flex: 1;             /* 占据剩余所有空间 */
  width: 100%;
  padding: 15px;
  border: none;        /* 去掉边框 */
  outline: none;       /* 去掉点击时的蓝框 */
  resize: none;        /* 禁止用户手动拉伸右下角，保持整洁 */
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  background-color: transparent;
}

/* 底部按钮栏样式 */
.footer-actions {
  padding: 10px 15px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f9f9f9;
}

.word-count {
  font-size: 12px;
  color: #999;
}

.save-btn {
  background-color: #52c41a; /* 绿色风格 */
  color: white;
  border: none;
  padding: 6px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.save-btn:hover {
  opacity: 0.8;
}

.save-btn:disabled {
  background-color: #bfbfbf;
  cursor: not-allowed;
}

.loading-state {
  text-align: center;
  padding-top: 50px;
  color: #999;
}
</style>