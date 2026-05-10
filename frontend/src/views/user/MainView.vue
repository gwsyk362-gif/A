<template>
  <div class="main-layout">
    <header class="nav-header">
      <div class="header-top">
        <div class="header-left">
          <span class="system-title" @click="router.push('/main')">🎓 个性化学习助手</span>
          <span v-if="isUserCenter" class="page-indicator"> | 个人中心</span>
          <span v-else class="page-indicator"> | 主页</span>
        </div>

        <div class="search-bar-container" >
          <div class="search-input-wrapper" v-if="!isUserCenter">
            <span class="search-icon">🔍</span>
            <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索相关资源"
              class="round-input"
            />
          </div>
        </div>

        <div class="header-right">
          <div v-if="!isUserCenter" class="user-avatar-wrapper" @click="router.push('/main/user')">
            <div class="avatar-circle">
              <span v-if="!user?.avatar">{{ user?.nickname?.charAt(0) || 'U' }}</span>
              <img v-else :src="user.avatar" alt="avatar" />
            </div>
            <span class="username-text">{{ user?.nickname || '同学' }}</span>
          </div>

        </div>
      </div>
    </header>

    <main class="content-container" style="padding-top: 84px">
      <router-view :user="user" :searchQuery="searchQuery" />
    </main>
  </div>
</template>

<script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useRouter, useRoute } from 'vue-router';

  const router = useRouter();
  const route = useRoute();
  const searchQuery = ref("");
  const user = ref(null);

  // 判断当前是否在用户中心页面
  const isUserCenter = computed(() => route.path === '/main/user');

  onMounted(() => {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      user.value = JSON.parse(savedUser);
    }
  });

</script>

<style scoped>
  /* 整体布局 */
  .main-layout {
    background-color: #ffffff;
    min-height: 100vh;
  }

  /* 顶部导航栏固定定位 */
  .nav-header {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    background: #fff;
    z-index: 1000;
    border-bottom: 1px solid #f1f2f3;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  }

  /* 导航栏内部 Flex 布局 */
  .header-top {
    display: flex;
    align-items: center;
    justify-content: space-between; /* 左右两端对齐的核心 */
    padding: 0 24px;
    height: 64px;
  }

  /* --- 左侧区域：系统标题 + 页面标识 --- */
  .header-left {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    min-width: 240px; /* 设定最小宽度，防止内容挤压 */
  }

  .system-title {
    font-size: 18px;
    font-weight: bold;
    color: #18191c;
    white-space: nowrap;
    cursor: pointer;
    transition: color 0.3s;
  }

  .system-title:hover {
    color: #fb7299;
  }

  /* 页面标识（主页 / 个人中心） */
  .page-indicator {
    margin-left: 12px;
    font-size: 15px;
    color: #9499a0;
    font-weight: normal;
    white-space: nowrap;
  }

  /* --- 中间区域：搜索框容器 --- */
  .search-bar-container {
    flex: 1; /* 占据剩余空间，充当弹簧将左右推开 */
    display: flex;
    justify-content: center;
    padding: 0 40px;
  }

  .search-input-wrapper {
    width: 100%;
    max-width: 480px;
    position: relative;
    display: flex;
    align-items: center;
  }

  .search-icon {
    position: absolute;
    left: 16px;
    color: #9499a0;
    font-size: 14px;
  }

  .round-input {
    width: 100%;
    height: 40px;
    padding: 0 20px 0 42px;
    background-color: #f1f2f3;
    border: 1px solid transparent;
    border-radius: 20px;
    font-size: 14px;
    outline: none;
    transition: all 0.3s;
  }

  .round-input:focus {
    background-color: #fff;
    border-color: #fb7299;
    box-shadow: 0 0 6px rgba(251, 114, 153, 0.15);
  }

  /* --- 右侧区域：头像和昵称 --- */
  .header-right {
    display: flex;
    align-items: center;
    justify-content: flex-end; /* 强制内容向右靠拢 */
    flex-shrink: 0;
    min-width: 240px;
  }

  .user-avatar-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    padding: 4px 12px;
    border-radius: 24px;
    transition: background-color 0.2s;
  }

  .user-avatar-wrapper:hover {
    background-color: #f1f2f3;
  }

  .avatar-circle {
    width: 36px;
    height: 36px;
    background-color: #fb7299;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    overflow: hidden;
    border: 1px solid #eee;
  }

  .avatar-circle img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .username-text {
    font-size: 15px;
    color: #18191c;
    font-weight: 600;
    white-space: nowrap;
  }

  /* --- 下方内容容器 --- */
  .content-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 84px 20px 20px; /* 留出顶部导航栏的高度 */
  }
</style>
