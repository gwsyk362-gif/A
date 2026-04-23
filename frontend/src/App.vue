<template>
  <div class="app-container">
    <header class="nav-header" v-if="isLoggedIn">
      <div class="header-content">
        <h2 class="logo-text">个性化学习助手</h2>

        <div class="user-info">
          <span>当前用户：{{ currentUser?.nickname || currentUser?.userId || '同学' }}</span>
        </div>
      </div>
    </header>

    <main class="content" :class="{ 'has-header': isLoggedIn }">
      <LoginView
        v-if="!isLoggedIn"
        @login-success="onLoginSuccess"
      />

      <template v-else>
        <GenerateView
          :currentUser="currentUser"
          @paper-generated="handlePaperGenerated"
        />

        <hr v-if="currentPaper.length > 0" class="divider" />

        <ExamView
          v-if="currentPaper.length > 0"
          :questions="currentPaper"
          @reset="handleReset"
        />
      </template>
    </main>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import LoginView from './views/LoginView.vue';
  import GenerateView from './views/GenerateView.vue';
  import ExamView from './views/ExamView.vue';
  import 'element-plus/dist/index.css';

  const isLoggedIn = ref(false);
  const currentUser = ref(null);
  const currentPaper = ref([]);

  const onLoginSuccess = (user) => {
    currentUser.value = user;
    isLoggedIn.value = true;
  };

  const handlePaperGenerated = (data) => {
    currentPaper.value = data;
  };

  const handleReset = () => {
    currentPaper.value = [];
  };
</script>

<style>
  body {
    background-color: #f5f7fa;
    margin: 0;
    font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  }

  /* 顶部导航栏样式 */
  .nav-header {
    position: fixed; /* 固定在顶部 */
    top: 0;
    left: 0;
    width: 100%;
    height: 60px;
    background-color:#87ceeb; /* 淡蓝色 (LightBlue) */
    color: #333; /* 淡蓝色背景下建议使用深色文字，更清晰 */
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    z-index: 1000;
    display: flex;
    align-items: center;
  }

  .header-content {
    max-width: 1200px;
    width: 100%;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    justify-content: space-between; /* 标题居左，用户信息居右 */
    align-items: center;
  }

  .logo-text {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .user-info {
    font-size: 14px;
    font-weight: 500;
  }

  /* 主内容区域间距调整 */
  .content {
    max-width: 1000px;
    margin: 0 auto;
    padding: 20px;
  }

  .has-header {
    margin-top: 80px; /* 留出顶部栏的空间 */
  }

  .divider {
    margin: 30px 0;
    border: none;
    border-top: 1px solid #eee;
  }
</style>
