<template>
  <div class="main-layout">

    <header class="nav-header">
      <div class="header-top">
        <div class="user-avatar" @click="activeTab = 'userCenter'" style="cursor: pointer;">
          <div class="avatar-placeholder"></div>
          <span class="username-tip" :class="{ 'active-text': activeTab === 'userCenter' }">
            {{ user?.nickname || '同学' }}
          </span>
        </div>

        <div class="search-bar-container">
          <div class="search-input-wrapper">
            <span class="search-icon">🔍</span>
            <input type="text" v-model="searchQuery" placeholder="搜索题目、资源或知识点" />
          </div>
        </div>
        <div style="width: 60px;"></div>
      </div>

      <nav class="category-nav" v-if="activeTab !== 'userCenter'">
        <div
          v-for="tab in tabs"
          :key="tab.id"
          class="nav-item"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          {{ tab.name }}
          <div class="active-line" v-if="activeTab === tab.id"></div>
        </div>
      </nav>
    </header>

    <main class="content" :class="{ 'has-header': true, 'center-mode': activeTab === 'userCenter' }">
      <div v-if="activeTab === 'userCenter'" class="view-section">
        <UserView :currentUser="user" @back="handleBackToHome" />
      </div>

      <div v-else-if="activeTab === 'resources'" class="view-section">
        <h3>📚 推荐学习资源</h3>
        <VideoResources :search-query="searchQuery" />
      </div>

      <div v-else-if="activeTab === 'paper'" class="view-section">
        <GenerateView :currentUser="user" mode="paper" @paper-generated="handlePaperGenerated" />
        <hr v-if="currentPaper.length > 0" class="divider" />
        <ExamView v-if="currentPaper.length > 0" :questions="currentPaper" @reset="handleReset" />
      </div>

      <div v-else-if="activeTab === 'practice'" class="view-section">
        <GenerateView :currentUser="user" mode="practice" @recommend-fetched="handleRecommendFetched" />
        <hr v-if="recommendedQuestions.length > 0" class="divider" />
        <div v-if="recommendedQuestions.length > 0" class="practice-list">
          <ExamView :questions="recommendedQuestions" @reset="recommendedQuestions = []" />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
  import { ref, defineProps } from 'vue';
  import GenerateView from './GenerateView.vue';
  import ExamView from './ExamView.vue';
  import VideoResources from './VideoResources.vue';
  import UserView from './UserView.vue';

  const props = defineProps({
    user: Object
  });

  const activeTab = ref("resources");
  const searchQuery = ref("");
  const currentPaper = ref([]);
  const recommendedQuestions = ref([]);

  const tabs = [
    { id: 'resources', name: '学习资源' },
    { id: 'paper', name: '做卷分区' },
    { id: 'practice', name: '做题分区' }
  ];

  const handleBackToHome = () => {
    activeTab.value = 'resources'; // 切换回学习资源首页
  };

  const handlePaperGenerated = (data) => { currentPaper.value = data; };
  const handleRecommendFetched = (data) => { recommendedQuestions.value = data; };
  const handleReset = () => { currentPaper.value = []; };
</script>

<style scoped>
  body {
    background-color: #ffffff;
    margin: 0;
    padding: 0;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
    -webkit-font-smoothing: antialiased;
  }

  /* 导航栏外层容器：固定在顶部，背景白色 */
  .nav-header {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    background: #fff;
    z-index: 1000;
    border-bottom: 1px solid #f2f2f2;
    display: flex;
    flex-direction: column; /* 纵向排列 header-top 和 category-nav */
  }

  /* 第一行：头像 + 搜索框 */
  .header-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 20px;
    height: 50px;
  }

  /* 用户头像和昵称部分 */
  .user-avatar {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 120px;
  }

  .avatar-placeholder {
    width: 38px;
    height: 38px;
    background: #e7e7e7;
    border-radius: 50%;
    border: 1px solid #e3e5e7;
    flex-shrink: 0;
  }

  .username-tip {
    font-size: 13px;
    color: #18191c;
    font-weight: 500;
  }

  /* 搜索框居中容器 */
  .search-bar-container {
    flex: 1;
    max-width: 500px;
    margin: 0 20px;
  }

  /* 搜索框背景及输入 */
  .search-input-wrapper {
    display: flex;
    align-items: center;
    background: #f1f2f3;
    border: 1px solid transparent;
    border-radius: 8px; /* 仿B站新版稍微方圆的风格 */
    padding: 4px 12px;
    transition: all 0.3s;
  }

  .search-input-wrapper:hover {
    background: #fff;
    border-color: #fb7299;
  }

  .search-icon {
    margin-right: 8px;
    color: #61666d;
    font-style: normal;
  }

  .search-input-wrapper input {
    border: none;
    background: transparent;
    width: 100%;
    padding: 6px 0;
    outline: none;
    font-size: 14px;
    color: #18191c;
  }

  /* 第二行：分区导航 */
  .category-nav {
    display: flex;
    padding: 0 20px;
    gap: 32px;
    height: 46px;
    align-items: center;
    justify-content: center;
  }

  .nav-item {
    position: relative;
    font-size: 15px;
    color: #61666d;
    cursor: pointer;
    transition: color 0.2s;
    white-space: nowrap;
    margin: 0;
  }

  .nav-item:hover {
    color: #fb7299;
  }

  .nav-item.active {
    color: #fb7299;
    font-weight: bold;
  }

  .active-line {
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 20px;
    height: 3px;
    background: #fb7299;
    border-radius: 2px;
  }

  /* 主内容区域布局控制 */
  .content {
    max-width: 1160px;
    margin: 0 auto;
    padding: 20px;
  }

  /* 当用户登录后，给顶部导航栏留出 100px 的高度 */
  .has-header {
    padding-top: 110px;
  }

  /* 分区切换时的淡入动画 */
  .view-section {
    animation: fadeIn 0.4s ease-out;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(5px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .placeholder-card {
    background: #f6f7f8;
    border: 1px dashed #ccd0d7;
    border-radius: 8px;
    padding: 80px 20px;
    text-align: center;
    color: #9499a0;
    font-size: 14px;
    margin-top: 20px;
  }

  .divider {
    margin: 40px 0;
    border: none;
    border-top: 1px solid #e3e5e7;
  }

  .user-avatar {
  cursor: pointer;
  transition: all 0.2s;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-avatar:hover {
  background-color: #f1f2f3; /* 悬停时有个浅灰色背景 */
}

.user-avatar.is-active .username-tip {
  color: #fb7299; /* 选中时文字变色 */
}
</style>
