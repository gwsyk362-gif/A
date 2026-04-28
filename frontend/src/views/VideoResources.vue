// VideoResources.vue
<template>
  <div class="video-resources">
    <div v-if="videoList.length === 0" class="empty-placeholder">
      😢 没有找到相关视频资源
    </div>
    <div class="video-grid">
      <div
        v-for="video in videoList"
        :key="video.id"
        class="video-card"
        @click="openVideo(video)"
      >
        <div class="video-cover">
          <span class="play-icon">▶️</span>
          <span class="cover-text">{{ video.title.charAt(0) }}</span>
        </div>
        <div class="video-info">
          <h4 class="video-title">{{ video.title }}</h4>
          <p class="video-desc">{{ video.description }}</p>
        </div>
      </div>
    </div>

    <!-- 视频播放模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>{{ currentVideo?.title }}</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="video-wrapper">
            <iframe
              v-if="currentVideo?.embedUrl"
              :src="currentVideo.embedUrl"
              frameborder="0"
              allowfullscreen
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            ></iframe>
            <div v-else class="no-video">
              ⚠️ 视频链接暂不可用
            </div>
          </div>
          <p class="video-description">{{ currentVideo?.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, computed, onMounted, watch } from 'vue'; 
 import axios from 'axios';

   const props = defineProps({
     searchQuery: {
       type: String,
       default: ''
     }
   });

   const videoList = ref([]);
   const loading = ref(false);

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

   onMounted(fetchVideos);

   // 监听搜索框变化
   watch(() => props.searchQuery, () => {
     fetchVideos();
   });


   // 模态框状态
   const showModal = ref(false);
   const currentVideo = ref(null);

   const openVideo = (video) => {
     currentVideo.value = video;
     showModal.value = true;
     document.body.style.overflow = 'hidden';
   };

   const closeModal = () => {
     showModal.value = false;
     currentVideo.value = null;
     document.body.style.overflow = '';
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

  .video-title {
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

  .video-description {
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
</style>
