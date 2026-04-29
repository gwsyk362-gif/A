<template>
  <div class="user-view">
    <el-card class="user-info-card">
      <div class="user-profile">
        <el-avatar :size="80" :src="userInfo.avatarUrl">{{ userInfo.username?.charAt(0) }}</el-avatar>
        <div class="info-text">
          <h2>{{ userInfo.username }}</h2>
          <p class="user-bio">{{ userInfo.bio || '这个人很懒，什么都没有留下。' }}</p>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="user-tabs" @tab-click="handleTabClick">

      <el-tab-pane label="收藏视频" name="videos">
        <div v-loading="loading" class="video-grid">
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
                <el-button
                  type="text"
                  class="unfav-btn"
                  @click.stop="unfavVideo(video.vidId)"
                >
                  取消收藏
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的题目" name="questions">
        <div class="placeholder-text">题目收藏功能开发中...</div>
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

  // 1. 响应式数据
  const activeTab = ref('videos');
  const loading = ref(false);
  const favoriteVideos = ref([]);
  const userInfo = ref(JSON.parse(localStorage.getItem('currentUser') || '{}'));

  // 2. 初始化加载
  onMounted(() => {
    if (!userInfo.value.userId) {
      ElMessage.warning('请先登录');
      // router.push('/login'); // 根据你的路由配置跳转
      return;
    }
    loadFavoriteVideos();
  });

  // 3. 获取收藏视频详情
  const loadFavoriteVideos = async () => {
    loading.value = true;
    try {
      // 对应你后端 FavoriteVideoController 中的 @GetMapping("/details")
      const res = await axios.get(`http://localhost:8080/favoriteVideos/details`, {
        params: { userId: userInfo.value.userId }
      });
      favoriteVideos.value = res.data;
    } catch (error) {
      console.error("加载失败:", error);
      ElMessage.error('无法获取收藏列表');
    } finally {
      loading.value = false;
    }
  };

  // 4. 取消收藏逻辑
  const unfavVideo = async (vidId) => {
    try {
      await ElMessageBox.confirm('确定要取消收藏这个视频吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });

      const res = await axios.post(`http://localhost:8080/favoriteVideos/remove`, {
        fvUserId: userInfo.value.userId,
        fvVidId: vidId
      });

      if (res.data === 'success') {
        ElMessage.success('已取消收藏');
        // 前端直接过滤掉，不需要重新刷接口
        favoriteVideos.value = favoriteVideos.value.filter(v => v.vidId !== vidId);
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败');
      }
    }
  };

  // 5. 跳转逻辑
  const goToVideo = (video) => {
    // 跳转到播放页面的逻辑，可根据实际情况修改
    router.push({ name: 'VideoPlayer', params: { id: video.vidId } });
  };

  // 6. 辅助工具
  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString();
  };

  const handleTabClick = (tab) => {
    if (tab.paneName === 'videos') {
      loadFavoriteVideos();
    }
  };
</script>

<style scoped>
  .user-view {
    max-width: 1200px;
    margin: 20px auto;
    padding: 0 20px;
  }

  .user-info-card {
    margin-bottom: 20px;
    border-radius: 12px;
  }

  .user-profile {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .info-text h2 {
    margin: 0 0 8px 0;
  }

  .user-bio {
    color: #666;
    font-size: 14px;
  }

  /* 视频网格布局 */
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

  .video-cover-wrapper:hover .play-overlay {
    opacity: 1;
  }

  .play-overlay i {
    font-size: 40px;
    color: #fff;
  }

  .video-info {
    padding: 12px;
  }

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

  .video-time {
    font-size: 12px;
    color: #999;
  }

  .unfav-btn {
    color: #f56c6c;
  }

  .unfav-btn:hover {
    color: #ff4949;
  }
</style>
