<template>
  <div class="text-resources">
    <h3 class="section-title" v-if="!searchQuery">📚 优选专栏与笔记</h3>
    <h3 class="section-title" v-else>🔍 "{{ searchQuery }}" 的搜索结果</h3>
    <div v-if="articleList.length === 0" class="empty-placeholder">
      😢 暂时没有找到相关文章
    </div>
    <div class="article-list">
      <div
        v-for="article in articleList"
        :key="article.articleId"
        class="article-card"
        @click="openArticle(article)"
      >
        <div class="card-content-wrapper">
          <div class="text-zone">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-summary">{{ article.summary || '本文暂无摘要...' }}</p>
            <div class="article-meta">
              <span class="meta-item">✍️ {{ article.authorName || '加载中...' }}</span>
              <span class="meta-item">📅 {{ formatDate(article.createTime) }}</span>
              <span class="meta-item">👁️ {{ article.viewCount || 0 }} 浏览</span>
              <span class="meta-item">⭐ {{ article.favoriteCount || 0 }} 收藏</span>
            </div>
          </div>
          <div class="image-zone" v-if="article.coverUrl">
            <img :src="article.coverUrl" alt="cover" class="article-cover" />
          </div>
        </div>
      </div>
    </div>
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="reader-container" @click.stop>
        <div v-if="loadingDetail" class="loading-state">
          <span>📖 正在加载正文...</span>
        </div>
        <div v-else class="reader-content">
          <button class="close-btn" @click="closeModal">✕</button>
          <h1 class="reader-title">{{ currentArticleDetail?.title }}</h1>
          <div class="reader-meta">
            <div class="meta-left">
              <span>✍️ {{ currentArticleDetail?.authorName || '加载中...' }}</span>
              <span>📅 {{ formatDate(currentArticleDetail?.createTime) }}</span>
              <span>👁️ {{ currentArticleDetail?.viewCount }} 浏览</span>
            </div>
            <button
              class="detail-fav-btn"
              :class="{ 'is-fav': isFavorited(currentArticleDetail?.articleId) }"
              @click="toggleFavorite(currentArticleDetail)"
            >
              <span class="fav-icon">{{ isFavorited(currentArticleDetail?.articleId) ? '★' : '☆' }}</span>
              {{ isFavorited(currentArticleDetail?.articleId) ? '已收藏' : '收藏' }}
            </button>
          </div>
          <div
            class="article-body"
            v-html="renderContent(currentArticleDetail)"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
  import { ref, onMounted, watch } from 'vue';
  import axios from 'axios';
  import { ElMessage } from 'element-plus';
  import { getCurrentUserId, formatDate, renderContent } from '../../utils/common.js';
  const props = defineProps({
    searchQuery: { type: String, default: '' },
  });
  const articleList = ref([]);
  const favoritedIds = ref([]);
  const loading = ref(false);
  // 加载文章列表
  const fetchArticles = async () => {
    loading.value = true;
    try {
      const res = await axios.get('http://localhost:8080/api/articles/page', {
        params: {
          current: 1,
          size: 20,
          keyword: props.searchQuery,
        },
      });
      articleList.value = res.data.records || res.data;
    } catch (error) {
      console.error('加载文章失败', error);
      ElMessage.error('获取文章列表失败');
    } finally {
      loading.value = false;
    }
  };
  // 加载用户的收藏 ID
  const fetchFavoriteIds = async () => {
    const userId = getCurrentUserId();
    if (!userId) return;
    try {
      const res = await axios.get(`http://localhost:8080/api/favoriteArticles/ids?userId=${userId}`);
      favoritedIds.value = res.data;
    } catch (e) {
      console.error('加载收藏状态失败', e);
    }
  };
  const isFavorited = (articleId) => favoritedIds.value.includes(articleId);
  // 收藏与取消收藏
  const toggleFavorite = async (article) => {
    const userId = getCurrentUserId();
    if (!userId) {
      ElMessage.warning('请先登录后再操作');
      return;
    }
    const isFav = isFavorited(article.articleId);
    const url = isFav ? '/api/favoriteArticles/remove' : '/api/favoriteArticles/add';
    try {
      const res = await axios.post(`http://localhost:8080${url}`, {
        favUserId: userId,
        favArticleId: article.articleId,
      });
      if (res.data === 'success') {
        if (isFav) {
          favoritedIds.value = favoritedIds.value.filter((id) => id !== article.articleId);
          ElMessage.success('已取消收藏');
        } else {
          favoritedIds.value.push(article.articleId);
          ElMessage.success('收藏成功');
        }
      } else if (res.data === 'already exists') {
        ElMessage.warning('您已经收藏过该文章了');
      }
    } catch (e) {
      ElMessage.error('收藏操作失败，请检查网络');
    }
  };
  // 沉浸式阅读器逻辑
  const showModal = ref(false);
  const currentArticleDetail = ref(null);
  const loadingDetail = ref(false);
const openArticle = async (article) => {
  showModal.value = true;
  loadingDetail.value = true;
  document.body.style.overflow = 'hidden';

    // 记录观看历史
   const userId = getCurrentUserId();
    if (userId) {
      axios.post('http://localhost:8080/api/history/record', {
        userId: userId,
        articleId: article.articleId
      }).catch(e => console.error('记录观看历史失败', e));
    }

    try {
      const res = await axios.get(`http://localhost:8080/api/articles/${article.articleId}`);
      currentArticleDetail.value = res.data;
      const listItem = articleList.value.find((a) => a.articleId === article.articleId);
      if (listItem) listItem.viewCount = res.data.viewCount;
    } catch (error) {
      ElMessage.error('获取正文失败');
    } finally {
      loadingDetail.value = false;
    }
  };
  const closeModal = () => {
    showModal.value = false;
    currentArticleDetail.value = null;
    document.body.style.overflow = '';
  };
  onMounted(() => {
    fetchArticles();
    fetchFavoriteIds();
  });
  watch(() => props.searchQuery, fetchArticles);
</script>

<style scoped>
  .text-resources {
    margin-top: 10px;
  }

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #18191c;
    margin: 10px 0 16px 0;
    padding-left: 10px;
    border-left: 4px solid #3478e5;
  }

  /* === 文章列表 === */
  .article-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .author-tag {
    color: #444 !important;
    font-weight: 500;
  }

  /* === 新增：右侧媒体区（包含阅读量和图片） === */
  .media-zone {
    display: flex;
    align-items: center; /* 让阅读量和图片垂直居中对齐 */
    gap: 20px;           /* 阅读量和图片之间的间距 */
    flex-shrink: 0;      /* 防止右侧被左侧长文本挤压变形 */
  }

  /* === 新增：阅读量样式设计 === */
  .view-stats {
    display: flex;
    flex-direction: column; /* 让数字和文字上下排列，更有设计感 */
    align-items: center;
    justify-content: center;
    color: #8590a6;
    min-width: 50px;
  }

  .view-icon {
    font-size: 14px;
    margin-bottom: 2px;
  }

  .view-number {
    font-size: 16px;
    font-weight: bold;
    color: #fb7299; /* 使用点赞粉色突出数据 */
    line-height: 1;
  }

  .view-text {
    font-size: 12px;
    transform: scale(0.9); /* 稍微缩小文字，主次分明 */
  }

  .article-title {
    padding-right: 40px;
  }

    /* === 收藏 === */
  .reader-meta {
    font-size: 14px;
    color: #8590a6;
    margin-bottom: 40px;
    display: flex;
    justify-content: space-between; /* 让信息在左，收藏按钮在右 */
    align-items: center;
    border-bottom: 1px solid #f0f1f2;
    padding-bottom: 20px;
  }

  .meta-left {
    display: flex;
    gap: 20px;
  }

  .detail-fav-btn {
    padding: 8px 20px;
    border-radius: 20px;
    border: 1px solid #0066ff; /* 知乎经典的交互蓝 */
    background: transparent;
    color: #0066ff;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 6px;
    transition: all 0.3s ease;
  }

  .detail-fav-btn:hover {
    background: rgba(0, 102, 255, 0.05);
  }

  /* 收藏后的高亮状态 */
  .detail-fav-btn.is-fav {
    background: #0066ff;
    color: #ffffff;
  }

  .fav-icon {
    font-size: 16px;
    line-height: 1;
  }

  /* === 沉浸式阅读器模态框 === */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(246, 246, 246, 0.95);
    display: flex;
    justify-content: center;
    align-items: flex-start;
    z-index: 2000;
    overflow-y: auto;
  }

  .reader-container {
    width: 100%;
    max-width: 800px;
    background: #fff;
    min-height: 100vh;
    height: auto;
    padding: 60px 50px;
    box-shadow: 0 0 20px rgba(0,0,0,0.05);
    position: relative;
    animation: slideUp 0.3s ease;
    box-sizing: border-box;
  }

  .close-btn {
    position: fixed;
    top: 30px;
    right: calc(50% - 400px - 60px); /* 吸附在阅读白纸的右侧 */
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #fff;
    border: 1px solid #e5e9ef;
    font-size: 18px;
    cursor: pointer;
    color: #999;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    transition: all 0.2s;
  }

  .close-btn:hover {
    color: #121212;
    border-color: #121212;
  }

  .reader-title {
    font-size: 28px;
    font-weight: bold;
    color: #121212;
    line-height: 1.4;
    margin-bottom: 20px;
  }

  .reader-meta {
    font-size: 14px;
    color: #8590a6;
    margin-bottom: 40px;
    display: flex;
    gap: 20px;
  }

  .loading-state {
    text-align: center;
    padding-top: 100px;
    color: #8590a6;
    font-size: 16px;
  }

  @keyframes slideUp {
    from { transform: translateY(30px); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
  }

</style>
