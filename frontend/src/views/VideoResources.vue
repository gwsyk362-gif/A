// VideoResources.vue
<template>
  <div class="video-resources">
    <div v-if="videoList.length === 0" class="empty-placeholder">
      😢 没有找到相关视频资源
    </div>
    <div class="video-grid">
      <div
        v-for="video in videoList"
        :key="video.vidId"  class="video-card"
        @click="openVideo(video)"
      >
        <div class="video-cover">
          <span class="play-icon">▶️</span>
          <span class="cover-text">{{ video.vidTitle ? video.vidTitle.charAt(0) : '' }}</span>
        </div>
        <div class="video-info">
          <div class="video-header-row">
            <h4 class="video-title">{{ video.vidTitle }}</h4>
            <button
              class="fav-btn"
              :class="{ 'is-fav': isFavorited(video.vidId) }"
              @click.stop="toggleFavorite(video)"
              title="收藏视频"
            >
              {{ isFavorited(video.vidId) ? '★' : '☆' }}
            </button>
          </div>
          <p class="video-desc">{{ video.vidDescription }}</p>
        </div>
      </div>
    </div>

    <!-- 视频播放模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>{{ currentVideo?.vidTitle }}</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="video-wrapper">
            <iframe
              v-if="currentVideo?.vidUrl"
              :src="currentVideo.vidUrl"
              frameborder="0"
              allowfullscreen
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            ></iframe>
            <div v-else class="no-video">
              ⚠️ 视频链接暂不可用
            </div>
          </div>
          <p class="video-description">{{ currentVideo?.vidDescription }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import {  computed, ref, onMounted, watch } from 'vue';
   import axios from 'axios';
   import { ElMessage } from 'element-plus';

   const props = defineProps({
     searchQuery: {
       type: String,
       default: ''
     }
   });

   const videoList = ref([]);
   const loading = ref(false);

   // === 1. 收藏相关响应式状态 ===
  // 获取当前用户ID
  const getCurrentUserId = () => {
    const savedUser = localStorage.getItem('currentUser');
    if (!savedUser) return null;
    const user = JSON.parse(savedUser);
    return user.userId;
  };

   const favoritedIds = ref([]);

   // 判断视频是否被收藏
   const isFavorited = (vidId) => {
     return favoritedIds.value.includes(vidId);
   };

   // === 2. 核心网络请求 ===
   // 获取视频列表
   const fetchVideos = async () => {
     try {
       const response = await axios.get('/api/videos/list', {
         params: { keyword: props.searchQuery }
       });
       videoList.value = response.data;
     } catch (error) {
       console.error("加载视频失败", error);
     }
   };

   // 获取用户收藏的视频ID列表
const fetchFavoriteIds = async () => {
     const userId = getCurrentUserId(); // 1. 调用函数获取 userId
     if (userId) { // 2. 判断 userId 是否存在
       try {
         const res = await axios.get(`http://localhost:8080/favoriteVideos/ids?userId=${userId}`);
         favoritedIds.value = res.data;
       } catch (e) {
         console.error("加载收藏状态失败", e);
       }
     }
   };

   // 切换收藏/取消收藏
   const toggleFavorite = async (video) => {
     const userId = getCurrentUserId(); // 1. 调用函数获取 userId

     // 2. 判断是否登录
     if (!userId) {
       ElMessage.warning("请先登录后再操作");
       return;
     }

     const isFav = isFavorited(video.vidId);
     const url = isFav ? '/favoriteVideos/remove' : '/favoriteVideos/add';

     try {
       const res = await axios.post(`http://localhost:8080${url}`, {
         favUserId: userId, // 3. 这里使用刚刚获取到的 userId
         favVidsId: video.vidId
       });

       if (res.data === 'success') {
         if (isFav) {
           favoritedIds.value = favoritedIds.value.filter(id => id !== video.vidId);
           ElMessage.success("已取消收藏");
         } else {
           favoritedIds.value.push(video.vidId);
           ElMessage.success("收藏成功");
         }
       } else if (res.data === 'already exists') {
         ElMessage.warning("您已经收藏过该视频了");
       } else {
         ElMessage.error("操作失败，请稍后再试");
       }
     } catch (e) {
       console.error("后台报错详细信息:", e);
       ElMessage.error("网络请求失败，请按 F12 查看控制台报错");
     }
   };

   // === 3. 生命周期与监听 ===
   onMounted(() => {
     fetchVideos();       // 加载视频大厅
     fetchFavoriteIds();  // 加载当前用户的收藏点亮状态
   });

   watch(() => props.searchQuery, () => {
     fetchVideos();
   });

  // === 4. 模态框与进度记录逻辑 ===
   const showModal = ref(false);
   const currentVideo = ref(null);

   // 新增：进度相关的状态
   const currentPosition = ref(0); // 记录观看了多少秒
   let watchTimer = null; // 计时器

   // 打开视频弹窗
   const openVideo = (video) => {
     currentVideo.value = video;
     showModal.value = true;
     document.body.style.overflow = 'hidden';

     // 每次打开新视频，重置观看秒数，并启动计时器
     currentPosition.value = 0;
     startWatchTimer();
   };

   // 关闭视频弹窗
   const closeModal = () => {
     // 关闭前，先保存一次进度到后端
     saveProgressToBackend();

     // 停止计时器，清理状态
     stopWatchTimer();
     showModal.value = false;
     currentVideo.value = null;
     document.body.style.overflow = '';
   };

   // 启动计时器：每秒钟将观看时间 +1
   const startWatchTimer = () => {
     if (watchTimer) clearInterval(watchTimer);
     watchTimer = setInterval(() => {
       currentPosition.value += 1;
     }, 1000);
   };

   // 停止计时器
   const stopWatchTimer = () => {
     if (watchTimer) {
       clearInterval(watchTimer);
       watchTimer = null;
     }
   };

   // 核心逻辑：将进度保存到后端
   const saveProgressToBackend = async () => {
     const userId = getCurrentUserId();
     // 如果没登录，或者没看视频（秒数为0），就不保存
     if (!userId || !currentVideo.value || currentPosition.value === 0) return;

     try {
       await axios.post('http://localhost:8080/videoProgress/save', {
         progUserId: userId,
         progVidId: currentVideo.value.vidId,
         progLastPosition: currentPosition.value,
         // 因为 iframe 无法知道视频是否真的放完了，这里统一传 0（未完结）
         // 这样这个视频就会一直出现在"继续学习"的列表里，直到用户手动取消收藏或其他操作
         progIsFinished: 0
       });
       console.log(`已保存进度：${currentPosition.value}秒`);
     } catch (error) {
       console.error("保存视频进度失败", error);
     }
   };
</script>

<style scoped>
  .video-resources {
    margin-top: 16px;
  }

  .video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }

  .video-card {
    background: #ffffff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    transition: all 0.2s ease;
    cursor: pointer;
    border: 1px solid #f0f1f2;
  }

  .video-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    border-color: #fb7299;
  }

  .video-cover {
    position: relative;
    height: 140px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
  }

  .play-icon {
    position: absolute;
    font-size: 36px;
    opacity: 0.8;
    transition: opacity 0.2s;
  }

  .video-card:hover .play-icon {
    opacity: 1;
    transform: scale(1.05);
  }

  .cover-text {
    font-size: 48px;
    font-weight: 600;
    opacity: 0.9;
  }

  .video-info {
    padding: 12px 16px;
  }

  .video-vidtitle {
    font-size: 16px;
    font-weight: 600;
    color: #18191c;
    margin: 0 0 8px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .video-desc {
    font-size: 13px;
    color: #61666d;
    line-height: 1.4;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .empty-placeholder {
    text-align: center;
    padding: 60px 20px;
    background: #f7f8f9;
    border-radius: 12px;
    color: #9499a0;
    font-size: 14px;
  }

  /* 模态框样式 */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    backdrop-filter: blur(4px);
  }

  .modal-container {
    background: white;
    border-radius: 16px;
    width: 90%;
    max-width: 880px;
    max-height: 85vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 20px 35px rgba(0, 0, 0, 0.2);
    animation: modalFadeIn 0.2s ease;
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e9ecef;
  }

  .modal-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #18191c;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #9499a0;
    transition: color 0.2s;
    line-height: 1;
    padding: 0;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
  }

  .close-btn:hover {
    color: #fb7299;
    background: #f1f2f3;
  }

  .modal-body {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
  }

  .video-wrapper {
    position: relative;
    padding-bottom: 56.25%; /* 16:9 比例 */
    height: 0;
    background: #000;
    border-radius: 12px;
    overflow: hidden;
  }

  .video-wrapper iframe,
  .video-wrapper video {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: none;
  }

  .video-vidDescription {
    margin-top: 16px;
    font-size: 14px;
    color: #4e555e;
    line-height: 1.5;
  }

  .no-video {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #ccc;
    font-size: 14px;
  }

  @keyframes modalFadeIn {
    from {
      opacity: 0;
      transform: scale(0.96);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }

  /* 响应式调整 */
  @media (max-width: 768px) {
    .video-grid {
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 12px;
    }

    .modal-container {
      width: 95%;
      max-height: 90vh;
    }

    .modal-header h3 {
      font-size: 16px;
    }
  }

  /* 让标题和星星在一行 */
.video-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.video-title {
  /* 覆盖原有的 margin，由父容器控制 */
  margin: 0;
  flex: 1;
}

/* 你提供的收藏按钮样式 */
.fav-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.3em;
  color: #ccc;
  padding: 0;
  line-height: 1;
  transition: color 0.2s, transform 0.1s;
  flex-shrink: 0; /* 防止星星被挤压 */
}

.fav-btn:hover {
  transform: scale(1.2);
  color: #f1c40f;
}

.fav-btn.is-fav {
  color: #f1c40f;
  text-shadow: 0 0 5px rgba(241, 196, 15, 0.3);
}
</style>
