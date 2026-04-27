<template>
  <div class="user-center">
    <div style="margin-bottom: 15px;">
      <el-button icon="Back" @click="$emit('back')">返回首页</el-button>
    </div>

    <el-card class="user-info-card">
      <el-avatar :size="64">{{ userInfo.nickname?.charAt(0) }}</el-avatar>
      <h2>{{ userInfo.nickname }}</h2>
      <p>账号：{{ userInfo.username }}</p>
    </el-card>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="收藏题目" name="questions">
        <el-table :data="favoriteQuestions" style="width: 100%">
          <el-table-column prop="quesContent" label="题目内容" show-overflow-tooltip />
          <el-table-column prop="subName" label="科目" width="120" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="goToQuestion(scope.row.quesId)">查看</el-button>
              <el-button size="small" type="danger" @click="unfavQues(scope.row.favId)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="收藏视频" name="videos">
        <div class="video-grid">
          <el-card v-for="vid in favoriteVideos" :key="vid.vidId" class="video-item">
            <img :src="vid.vidCoverUrl" class="cover" />
            <div class="vid-title">{{ vid.vidTitle }}</div>
            <el-button link type="primary" @click="goToVideo(vid.vidId)">立即观看</el-button>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
  import { ref, onMounted, defineProps, defineEmits } from 'vue'

  // 定义事件，确保父组件能接收到 'back'
  const emit = defineEmits(['back']);

  // 接收父组件传来的 user 对象
  const props = defineProps({
    currentUser: Object
  });

  const activeTab = ref('questions')
  const userInfo = ref({
    nickname: props.currentUser?.nickname || '未登录',
    username: props.currentUser?.username || '未登录'
  })


  const handleTabChange = (name) => {
    if (name === 'videos') {
      console.log("切换到视频收藏页签");
    }
  }
</script>

<style scoped>
.user-center { padding: 20px; max-width: 1000px; margin: 0 auto; }
.user-info-card { text-align: center; margin-bottom: 20px; }
.video-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.video-item .cover { width: 100%; height: 120px; object-fit: cover; }
</style>
