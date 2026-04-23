<template>
  <div class="posts-container">
    <div class="posts-header">
      <h2>社区交流</h2>
      <button class="create-btn" @click="showCreateModal = true">发布帖子</button>
    </div>

    <!-- 帖子列表 -->
    <div v-if="loading" class="loading">正在加载帖子...</div>
    <div v-else class="posts-list">
      <div v-for="post in posts" :key="post.post_id" class="post-card">
        <div class="post-info">
          <span class="author">👤 {{ post.owner_name }}</span>
          <span class="post-id">#{{ post.post_id }}</span>
        </div>
        <h3 class="post-title">{{ post.post_title }}</h3>
        <p class="post-content">{{ post.post_content }}</p>
        
        <div v-if="post.file_name" class="post-attachment">
          <span class="file-icon">📎</span>
          <span class="file-name">{{ post.file_name }}</span>
          <button class="download-btn" @click="downloadFile(post.post_id, post.file_name)">下载附件</button>
        </div>
      </div>
      
      <div v-if="posts.length === 0" class="empty-state">
        暂无帖子，快来发布第一篇吧！
      </div>
    </div>

    <!-- 发布帖子弹窗 -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>发布新帖子</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>标题</label>
            <input v-model="newPost.post_title" placeholder="请输入帖子标题" />
          </div>
          <div class="form-item">
            <label>内容</label>
            <textarea v-model="newPost.post_content" placeholder="请输入帖子内容"></textarea>
          </div>
          <div class="form-item">
            <label>附件 (可选)</label>
            <div class="file-upload">
              <input type="file" ref="fileInput" @change="handleFileChange" />
              <p class="file-tip">支持任意文件类型上传</p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button 
            class="submit-btn" 
            :disabled="!newPost.post_title || !newPost.post_content || submitting"
            @click="createPost"
          >
            {{ submitting ? '发布中...' : '发布帖子' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Posts',
  data() {
    return {
      posts: [],
      loading: false,
      submitting: false,
      showCreateModal: false,
      newPost: {
        post_title: '',
        post_content: '',
        owner_id: 1, // 假设当前用户ID为1，实际应从登录信息获取
        owner_name: '当前用户' // 假设当前用户名
      },
      selectedFile: null
    }
  },
  mounted() {
    this.getPostList()
  },
  methods: {
    async getPostList() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8080/community/getPostList')
        this.posts = response.data
      } catch (error) {
        console.error('获取帖子列表失败:', error)
      } finally {
        this.loading = false
      }
    },

    handleFileChange(e) {
      this.selectedFile = e.target.files[0]
    },

    async createPost() {
      this.submitting = true
      try {
        const formData = new FormData()
        // 后端要求 @RequestParam("post") String postJson
        formData.append('post', JSON.stringify(this.newPost))
        
        if (this.selectedFile) {
          formData.append('file', this.selectedFile)
        }

        await axios.post('http://localhost:8080/community/createPost', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })

        alert('发布成功！')
        this.closeModal()
        this.getPostList() // 刷新列表
      } catch (error) {
        console.error('发布帖子失败:', error)
        alert('发布失败，请检查网络或后端服务')
      } finally {
        this.submitting = false
      }
    },

    async downloadFile(postId, fileName) {
      try {
        const response = await axios.get(`http://localhost:8080/community/downloadFile`, {
          params: { post_id: postId },
          responseType: 'blob' // 重要：处理文件流
        })

        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } catch (error) {
        console.error('下载文件失败:', error)
        alert('下载失败')
      }
    },

    closeModal() {
      this.showCreateModal = false
      this.newPost.post_title = ''
      this.newPost.post_content = ''
      this.selectedFile = null
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = ''
      }
    }
  }
}
</script>

<style scoped>
.posts-container {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}

.posts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.posts-header h2 {
  margin: 0;
  color: #333;
}

.create-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.3s;
}

.create-btn:hover {
  background-color: #40a9ff;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

.post-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  color: #8c8c8c;
}

.post-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #262626;
}

.post-content {
  color: #595959;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 16px;
}

.post-attachment {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.file-icon {
  margin-right: 8px;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #1890ff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.download-btn {
  background-color: transparent;
  color: #1890ff;
  border: 1px solid #1890ff;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.download-btn:hover {
  background-color: #1890ff;
  color: white;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  width: 500px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #bfbfbf;
  cursor: pointer;
}

.modal-body {
  padding: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #262626;
}

.form-item input, 
.form-item textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-item textarea {
  height: 120px;
  resize: vertical;
}

.file-upload {
  border: 1px dashed #d9d9d9;
  padding: 16px;
  text-align: center;
  border-radius: 4px;
}

.file-tip {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #8c8c8c;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  padding: 6px 16px;
  border: 1px solid #d9d9d9;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn {
  padding: 6px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:disabled {
  background-color: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
  border: 1px solid #d9d9d9;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  color: #8c8c8c;
}
</style>