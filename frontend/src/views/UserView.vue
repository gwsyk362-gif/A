<template>
  <div class="user-center">
    <div style="margin-bottom: 15px;">
      <el-button icon="Back" @click="handleBack">返回首页</el-button>
    </div>

    <el-card class="user-info-card">
      <div class="user-profile">
        <el-avatar :size="80" :src="userInfo.avatarUrl">
          {{ String(userInfo.nickname || userInfo.username || 'U').charAt(0) }}
        </el-avatar>
        <div class="info-text">
          <h2>{{ userInfo.nickname || userInfo.username }}</h2>
          <p class="user-bio">账号：{{ userInfo.username }}</p>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="user-tabs" @tab-click="handleTabClick">

      <el-tab-pane label="收藏题目" name="questions">
        <el-table :data="favoriteQuestions" style="width: 100%" v-loading="loadingQuestions">
          <el-table-column prop="quesContent" label="题目内容" show-overflow-tooltip />

          <el-table-column label="科目" width="120">
            <template #default="scope">
              {{ getSubjectName(scope.row.quesSubId) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" type="primary" @click="openDetail(scope.row)">查看</el-button>
              <el-button size="small" type="danger" @click="unfavQues(scope.row.quesId)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="收藏视频" name="videos">
        <div v-loading="loadingVideos" class="video-grid">
          <div v-if="favoriteVideos.length === 0" class="empty-state">
            <el-empty description="暂无收藏视频" />
          </div>

          <div v-for="video in favoriteVideos" :key="video.vidId" class="video-card">
            <div class="video-cover-wrapper" @click="goToVideo(video)">
              <img v-if="video.vidCoverUrl" :src="video.vidCoverUrl" class="video-cover" />
              <div v-else class="placeholder-cover">
                <span>{{ video.vidTitle?.charAt(0) }}</span>
              </div>
              <div class="play-overlay">
                <i class="el-icon-video-play"></i>
              </div>
            </div>

            <div class="video-info">
              <h4 class="video-title">{{ video.vidTitle }}</h4>
              <div class="video-footer">
                <span class="video-time">{{ formatDate(video.vidCreateTime) }}</span>
                <el-button type="text" class="unfav-btn" @click.stop="unfavVideo(video.vidId)">取消收藏</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import axios from 'axios';
  import { ElMessage, ElMessageBox } from 'element-plus';

  const router = useRouter();

  // 全局状态
  const activeTab = ref('questions'); // 默认显示题目
  const userInfo = ref({});

  // === 题目相关状态 ===
  const favoriteQuestions = ref([]);
  const subjects = ref([]);
  const loadingQuestions = ref(false);

  // === 视频相关状态 ===
  const favoriteVideos = ref([]);
  const loadingVideos = ref(false);

  // ================= 生命周期 =================
  onMounted(async () => {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      userInfo.value = JSON.parse(savedUser);

      // 初始化时加载两个模块的数据
      await loadSubjects();
      loadFavoriteQuestions();
      loadFavoriteVideos();
    } else {
      router.push('/login');
    }
  });

  const handleBack = () => router.push('/main');

  // ================= 原有逻辑：题目收藏 =================
  const loadSubjects = async () => {
    try {
      const res = await axios.get('http://localhost:8080/subjects/list'); // 请确保此处是你原本的获取科目接口
      subjects.value = res.data;
    } catch (e) {
      console.error('加载科目失败', e);
    }
  };

  const getSubjectName = (subId) => {
    const sub = subjects.value.find(s => s.subId === subId);
    return sub ? sub.subName : '未知';
  };

  const loadFavoriteQuestions = async () => {
    loadingQuestions.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/favoriteQuestions/details?userId=${userInfo.value.userId}`);
      favoriteQuestions.value = res.data;
    } catch (e) {
      console.error('加载收藏题目失败', e);
    } finally {
      loadingQuestions.value = false;
    }
  };

  const openDetail = (row) => {
    // 这里填入你原本的题目详情跳转逻辑
    router.push(`/question/${row.quesId}`);
  };

  const unfavQues = async (quesId) => {
    try {
      const res = await axios.post('http://localhost:8080/favoriteQuestions/remove', {
        favUserId: userInfo.value.userId,
        favQuesId: quesId
      });
      if (res.data === 'success') {
        ElMessage.success('已取消收藏题目');
        favoriteQuestions.value = favoriteQuestions.value.filter(q => q.quesId !== quesId);
      }
    } catch (e) {
      console.error('取消收藏题目失败', e);
    }
  };

  // ================= 新增逻辑：视频收藏 =================
  const loadFavoriteVideos = async () => {
    loadingVideos.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/favoriteVideos/details`, {
        params: { userId: userInfo.value.userId }
      });
      favoriteVideos.value = res.data;
    } catch (error) {
      console.error("加载视频失败:", error);
    } finally {
      loadingVideos.value = false;
    }
  };

  const unfavVideo = async (vidId) => {
    try {
      await ElMessageBox.confirm('确定要取消收藏这个视频吗？', '提示', { type: 'warning' });
      const res = await axios.post(`http://localhost:8080/favoriteVideos/remove`, {
        favUserId: userInfo.value.userId,
        favVidsId: vidId
      });
      if (res.data === 'success') {
        ElMessage.success('已取消收藏');
        favoriteVideos.value = favoriteVideos.value.filter(v => v.vidId !== vidId);
      }
    } catch (error) {
      if (error !== 'cancel') ElMessage.error('操作失败');
    }
  };

  const goToVideo = (video) => {
    router.push({ name: 'VideoPlayer', params: { id: video.vidId } });
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString();
  };

  const handleTabClick = (tab) => {
    if (tab.paneName === 'videos' && favoriteVideos.value.length === 0) {
      loadFavoriteVideos();
    } else if (tab.paneName === 'questions' && favoriteQuestions.value.length === 0) {
      loadFavoriteQuestions();
    }
  };
</script>

<style scoped>
  /* 原有的基础布局样式 */
  .user-center { padding: 20px; max-width: 1000px; margin: 0 auto; }
  .user-info-card { margin-bottom: 20px; border-radius: 12px; }

  .user-profile {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  .info-text h2 { margin: 0 0 8px 0; }
  .user-bio { color: #666; font-size: 14px; }

  /* 视频网格布局 (新增的) */
  .video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
    padding: 20px 0;
  }

  .video-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
    transition: transform 0.3s;
  }

  .video-card:hover {
    transform: translateY(-5px);
  }

  .video-cover-wrapper {
    position: relative;
    height: 140px;
    cursor: pointer;
    background: #f0f0f0;
  }

  .video-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .placeholder-cover {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    color: #999;
    background: #e4e7ed;
  }

  .play-overlay {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
  }

  .video-cover-wrapper:hover .play-overlay { opacity: 1; }
  .play-overlay i { font-size: 40px; color: #fff; }

  .video-info { padding: 12px; }
  .video-title {
    margin: 0 0 10px 0;
    font-size: 16px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .video-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .video-time { font-size: 12px; color: #999; }
  .unfav-btn { color: #f56c6c; }
  .unfav-btn:hover { color: #ff4949; }
</style>
