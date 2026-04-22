<template>
  <div class="file-tree-container">
    <div v-if="loading" class="loading">加载中...</div>
    
    <ul class="tag-list">
      <li v-for="tag in tags" :key="tag.tag_id" class="tag-item">
        <div 
          class="tag-header" 
          :class="{ 'is-active': expandedTags.includes(tag.tag_id) }"
          @click="handleTagClick(tag)"
        >
          <span class="icon">{{ expandedTags.includes(tag.tag_id) ? '📂' : '📁' }}</span>
          <span class="tag-name">{{ tag.tag_name }}</span>
        </div>

        <transition name="fade">
          <ul v-if="expandedTags.includes(tag.tag_id)" class="doc-list">
            <li v-if="tagDocs[tag.tag_id] && tagDocs[tag.tag_id].length === 0" class="empty-tip">
              暂无文档
            </li>
            <li 
              v-for="doc in tagDocs[tag.tag_id]" 
              :key="doc.doc_id" 
              class="doc-item"
              @click="handleDocClick(doc)"
            >
              <span class="icon">📄</span>
              <span class="doc-name">{{ doc.title || doc.doc_name || '未命名文档' }}</span>
            </li>
          </ul>
        </transition>
      </li>
    </ul>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'FileTree',
  data() {
    return {
      tags: [],           // 标签列表
      tagDocs: {},        // 存储每个tag对应的文档列表，格式: { 1: [doc1, doc2] }
      expandedTags: [],   // 当前展开的标签ID数组
      loading: false
    }
  },
  mounted() {
    this.getTags()
  },
  methods: {
    /**
   * 暴露给父组件调用的刷新方法
   */
  async refreshTree() {
    console.log("正在刷新目录数据...");
    await this.getTags(); // 重新调用获取标签的接口
    console.log("目录已刷新");
  },
    /**
     * 获取所有标签，并刷新已展开标签的文档列表
     */
    async getTags() {
      try {
        this.loading = true
        // 确保接口路径正确
        const response = await axios.get('http://localhost:8080/docs/getTags')
        // console.log("后端原始数据:", response.data);
        this.tags = response.data

        // 刷新所有当前已展开标签的文档列表，以展示新上传的文件
        if (this.expandedTags.length > 0) {
          const promises = this.expandedTags.map(id => this.getDocsByTag(id));
          await Promise.allSettled(promises); // 使用 allSettled 避免单个接口失败影响整体
        }
      } catch (error) {
        console.error('获取标签失败:', error)
      } finally {
        this.loading = false
      }
    },

    /**
     * 点击标签：处理展开/收起，并按需加载文档
     */
    async handleTagClick(tag) {
      const id = tag.tag_id; // 使用下划线 ID
      const index = this.expandedTags.indexOf(id)
      
      if (index > -1) {
        // 如果已展开，则收起
        this.expandedTags.splice(index, 1)
      } else {
        // 如果未展开，先检查是否已经缓存了数据
        if (!this.tagDocs[id]) {
          await this.getDocsByTag(id)
        }
        this.expandedTags.push(id)
      }
    },

    /**
     * 根据 tag_id 获取文档列表
     */
    async getDocsByTag(tagId) {
  try {
    const response = await axios.get(`http://localhost:8080/docs/getDocListByTagId/${tagId}`);
    console.log("后端原始数据:", response.data);
    // Vue 3 直接赋值即可，它是响应式的
    this.tagDocs[tagId] = response.data;
    
    // 为了保险起见，如果你发现界面没刷新，可以强制替换对象引用（可选）
    // this.tagDocs = { ...this.tagDocs, [tagId]: response.data };
    
  } catch (error) {
    console.error('获取文档列表失败:', error);
  }
},

    /**
     * 点击文档：将 doc 信息传递给父组件
     */
    handleDocClick(doc) {
      // 构造传递给父组件的数据结构
      const fileNode = {
        id: doc.doc_id, // 假设后端文档 ID 也是 doc_id
        name: doc.title || doc.doc_name,
        type: 'file',
        path: doc.path
      }
      this.$emit('file-select', fileNode)
    }
  }
}
</script>

<style scoped>
/* 样式部分保持不变 */
.file-tree-container {
  user-select: none;
}
.tag-list, .doc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.tag-header {
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  border-radius: 4px;
  transition: background 0.2s;
  font-size: 14px;
  color: #333;
}
.tag-header:hover {
  background-color: #e6f7ff;
}
.tag-header.is-active {
  color: #1890ff;
  font-weight: 500;
}
.doc-list {
  padding-left: 24px;
}
.doc-item {
  padding: 6px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
  border-radius: 4px;
}
.doc-item:hover {
  background-color: #f5f5f5;
  color: #1890ff;
}
.icon {
  margin-right: 8px;
  font-size: 14px;
}
.empty-tip {
  padding: 6px 12px;
  font-size: 12px;
  color: #999;
  font-style: italic;
}
.loading {
  text-align: center;
  color: #999;
  padding: 20px;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}
</style>