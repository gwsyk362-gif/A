// VideoResources.vue
<template>
  <div class="video-resources">

    <div class="hot-carousel-section" v-if="topVideos.length > 0 && !searchQuery">
      <h3 class="section-title">🔥 热门推荐</h3>
      <el-carousel :interval="4000" type="card" height="260px" v-loading="loadingHot">
        <el-carousel-item v-for="video in topVideos" :key="video.vidId">
          <div class="carousel-card" @click="openVideo(video)">
            <img
              v-if="video.vidCoverUrl"
              :src="'http://localhost:8080' + video.vidCoverUrl"
              class="carousel-img"
            />
            <div v-else class="carousel-placeholder">
              {{ video.vidTitle ? video.vidTitle.charAt(0) : '' }}
            </div>

            <div class="carousel-info">
              <h3>{{ video.vidTitle }}</h3>
              <p class="view-count">▶ 播放量：{{ video.viewCount || 0 }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="all-resources-section">
      <h3 class="section-title" v-if="!searchQuery">📚 全部学习资源</h3>
      <h3 class="section-title" v-else>🔍 搜索结果</h3>

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
            <img
              v-if="video.vidCoverUrl"
              :src="'http://localhost:8080' + video.vidCoverUrl"
              class="real-cover"
            />
            <span v-else class="cover-text">
              {{ video.vidTitle ? video.vidTitle.charAt(0) : '' }}
            </span>
            <span class="play-icon">▶️</span>
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
    </div>

    <VideoPlayer
      :video="currentVideo"
      :visible="showModal"
      @close="closeModal"
      @progress="saveProgressToBackend"
    />
  </div>
</template>

<script setup>
  import { computed, ref, onMounted, watch } from 'vue';
  import axios from 'axios';
  import { ElMessage } from 'element-plus';
  import { getCurrentUserId } from '../../utils/common.js';
  import VideoPlayer from '../../components/VideoPlayer.vue';

  const props = defineProps({
    searchQuery: {
      type: String,
      default: ''
    }
  });

  const videoList = ref([]);
  const loading = ref(false);

  // 新增：热门视频状态
  const topVideos = ref([]);
  const loadingHot = ref(false);

  // === 1. 收藏相关响应式状态 ===
  const favoritedIds = ref([]);

  const isFavorited = (vidId) => {
    return favoritedIds.value.includes(vidId);
  };

  // === 2. 核心网络请求 ===

  // 新增：获取热度最高的前5个视频
  const fetchHotVideos = async () => {
    loadingHot.value = true;
    try {
      const res = await axios.get('http://localhost:8080/api/admin/videos/list', {
        params: {
          page: 1,
          size: 5,
          sortField: 'viewCount',
          sortOrder: 'desc'
        }
      });
      // 兼容后端的分页格式或直接数组格式
      topVideos.value = res.data.records || res.data;
    } catch (error) {
      console.error("加载热门视频失败", error);
    } finally {
      loadingHot.value = false;
    }
  };

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

  const fetchFavoriteIds = async () => {
    const userId = getCurrentUserId();
    if (userId) {
      try {
        const res = await axios.get(`http://localhost:8080/favoriteVideos/ids?userId=${userId}`);
        favoritedIds.value = res.data;
      } catch (e) {
        console.error("加载收藏状态失败", e);
      }
    }
  };

  const toggleFavorite = async (video) => {
    const userId = getCurrentUserId();
    if (!userId) {
      ElMessage.warning("请先登录后再操作");
      return;
    }

    const isFav = isFavorited(video.vidId);
    const url = isFav ? '/favoriteVideos/remove' : '/favoriteVideos/add';

    try {
      const res = await axios.post(`http://localhost:8080${url}`, {
        favUserId: userId,
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
    fetchHotVideos();    // 加载走马灯热门视频
    fetchVideos();       // 加载视频大厅
    fetchFavoriteIds();  // 加载当前用户的收藏点亮状态
  });

  watch(() => props.searchQuery, () => {
    fetchVideos();
  });

 // === 4. 视频播放模态框 ===
  const showModal = ref(false);
  const currentVideo = ref(null);

  const openVideo = (video) => {
    currentVideo.value = video;
    showModal.value = true;
  };

  const closeModal = () => {
    showModal.value = false;
    currentVideo.value = null;
  };

  const saveProgressToBackend = async ({ vidId, position }) => {
    const userId = getCurrentUserId();
    if (!userId || position === 0) return;
    try {
      await axios.post('http://localhost:8080/videoProgress/save', {
        progUserId: userId,
        progVidId: vidId,
        progLastPosition: position,
        progIsFinished: 0
      });
    } catch (error) {
      console.error("保存视频进度失败", error);
    }
  };
</script>

<style scoped>
  .video-resources {
    margin-top: 10px;
  }

  /* === 新增：标题与走马灯样式 === */
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #18191c;
    margin: 10px 0 16px 0;
    padding-left: 10px;
    border-left: 4px solid #fb7299; /* B站粉色点缀 */
  }

  .hot-carousel-section {
    margin-bottom: 40px;
  }

  .carousel-card {
    position: relative;
    width: 100%;
    height: 100%;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s;
  }

  .carousel-card:hover {
    transform: translateY(-4px);
  }

  .carousel-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .carousel-placeholder {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #fb7299 0%, #ff9e8b 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 72px;
    color: white;
    font-weight: bold;
  }

  .carousel-info {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 50px 20px 20px;
    background: linear-gradient(to top, rgba(0,0,0,0.85), transparent);
    color: white;
    box-sizing: border-box;
  }

  .carousel-info h3 {
    margin: 0 0 8px 0;
    font-size: 20px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .carousel-info .view-count {
    margin: 0;
    font-size: 14px;
    color: #ffd700;
  }

  /* === 以下是原有的样式，保持不变 === */
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

  .real-cover {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    z-index: 1;
  }

  .play-icon {
    position: absolute;
    font-size: 36px;
    opacity: 0.8;
    transition: opacity 0.2s;
    z-index: 2;
  }

  .video-card:hover .play-icon {
    opacity: 1;
    transform: scale(1.05);
  }

  .cover-text {
    font-size: 48px;
    font-weight: 600;
    opacity: 0.9;
    z-index: 2;
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

  @media (max-width: 768px) {
    .video-grid {
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 12px;
    }
  }

  .video-header-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 8px;
  }

  .video-title {
    margin: 0;
    flex: 1;
  }

  .fav-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.3em;
    color: #ccc;
    padding: 0;
    line-height: 1;
    transition: color 0.2s, transform 0.1s;
    flex-shrink: 0;
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
