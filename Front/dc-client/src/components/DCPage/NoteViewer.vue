<template>
  <div class="note-container">
    <div v-if="loading" class="loading-state">
      <span class="spinner"></span> 加载笔记中...
    </div>
    
    <div v-else class="editor-wrapper">
      <!-- 笔记编辑区 -->
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

      <!-- AI 总结区 -->
      <div class="ai-summary-section">
        <div class="ai-header">
          <span>📝 AI 总结</span>
          <span v-if="aiSummaryLoading" class="loading-text">加载中...</span>
        </div>
        <div class="ai-content">
          <div v-if="aiSummaryLoading" class="ai-loading">正在获取总结...</div>
          <div v-else-if="aiSummary" class="ai-text">{{ aiSummary }}</div>
          <div v-else class="ai-empty">暂无 AI 总结</div>
        </div>
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
      aiSummary: '',       // AI 总结内容
      loading: false,      // 笔记加载状态
      aiSummaryLoading: false, // AI 总结加载状态
      saving: false
    }
  },
  watch: {
    'file.id': {
      handler(newId) {
        if (newId) {
          this.fetchNote(newId);       // 加载笔记
          this.fetchAiSummary(newId); // 同时加载 AI 总结
        }
      },
      immediate: true
    }
  },
  methods: {
    // 获取笔记
    async fetchNote(docId) {
      this.loading = true;
      try {
        const res = await axios.get(`http://localhost:8080/docs/getNoteByDocId/${docId}`);
        this.noteContent = res.data || '';
      } catch (e) {
        console.error("加载笔记失败", e);
        this.noteContent = '';
      } finally {
        this.loading = false;
      }
    },

    // 获取 AI 总结
    async fetchAiSummary(docId) {
      this.aiSummaryLoading = true;
      this.aiSummary = '';
      try {
        const res = await axios.get(`http://localhost:8080/docs/getAi_summary/${docId}`);
        // 根据后端返回格式调整，默认取 res.data
        this.aiSummary = res.data || '';
      } catch (e) {
        console.error("加载 AI 总结失败", e);
        this.aiSummary = '获取总结失败';
      } finally {
        this.aiSummaryLoading = false;
      }
    },

    // 保存笔记
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

/* ========== AI 总结区样式 ========== */
.ai-summary-section {
  border-top: 2px solid #f0f0f0;
  padding: 12px 15px;
  background-color: #fdfefe;
  flex-shrink: 0;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.loading-text {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.ai-content {
  min-height: 60px;
  padding: 10px;
  background: #f7f9fa;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #444;
}

.ai-loading {
  color: #999;
  text-align: center;
  padding: 10px 0;
}

.ai-empty {
  color: #bbb;
  text-align: center;
  padding: 10px 0;
}

.ai-text {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>