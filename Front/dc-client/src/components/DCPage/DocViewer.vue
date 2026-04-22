<template>
  <div class="doc-content-container">
    <div v-if="loading" class="loading">正在加载文档内容...</div>
    <div v-else>
      <h2 class="doc-title">{{ file.name }}</h2>
      <hr />
      <div class="content-body" v-html="docContent"></div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'DocViewer',
  props: {
    // 接收父组件传来的 file 对象
    file: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      docContent: '',
      name:'',
      loading: false
    }
  },
  // 关键：监听 file 属性，一旦变化（选择了新文档），就去后台查内容
  watch: {
    file: {
      handler(newFile) {
        if (newFile && newFile.id) {
          this.fetchDocContent(newFile.id);
        }
      },
      immediate: true // 组件初次挂载时如果已有 file，也立即执行一次
    }
  },
  methods: {
    async fetchDocContent(docId) {
      try {
        this.loading = true;
        // 调用你的后端接口 /getDoc/{id}
        const response = await axios.get(`http://localhost:8080/docs/getDoc/${docId}`);
        
        // 假设后端返回的对象结构是 { doc_content: "..." }
        // 注意：根据你之前的下划线风格，这里大概率是 doc_content
        this.docContent = response.data.doc_content || response.data.content || "该文档无内容";
        this.name = response.data.doc_name;
        
      } catch (error) {
        console.error('获取文档内容失败:', error);
        this.docContent = '<span style="color:red">内容加载失败，请重试。</span>';
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.doc-content-container {
  padding: 20px;
  line-height: 1.6;
  color: #2c3e50;
}
.doc-title {
  margin-bottom: 10px;
  color: #333;
}
.content-body {
  white-space: pre-wrap; /* 保持换行格式 */
  font-family: sans-serif;
}
.loading {
  color: #999;
  text-align: center;
  margin-top: 50px;
}
</style>