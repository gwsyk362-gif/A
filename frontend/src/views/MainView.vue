<template>
  <div class="main-layout">
    <header class="nav-header">
      <div class="header-top">
        <div class="header-left">
          <div class="user-avatar-wrapper" @click="router.push('/main/user')" title="个人中心">
            <div class="avatar-circle">
              <span v-if="!user?.avatar">{{ user?.nickname?.charAt(0) || 'U' }}</span>
              <img v-else :src="user.avatar" alt="avatar" />
            </div>
            <span class="username-text">{{ user?.nickname || '同学' }}</span>
          </div>
        </div>

        <div class="search-bar-container">
          <div class="search-input-wrapper">
            <span class="search-icon">🔍</span>
            <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索题目、资源或知识点..."
              class="round-input"
            />
          </div>
        </div>

        <div class="header-right">
          <el-button size="small" round type="info" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </header>

    <main class="content-container">
      <router-view :user="user" :searchQuery="searchQuery" />
    </main>
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const searchQuery = ref("");
  const user = ref(null);

  onMounted(() => {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      user.value = JSON.parse(savedUser);
    }
  });

  const handleLogout = () => {
    localStorage.removeItem('currentUser');
    router.push('/login');
  };
</script>

<style scoped>
  .main-layout {
    background-color: #ffffff;
    min-height: 100vh;
  }

  /* 顶部导航头 */
  .nav-header {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    background: #fff;
    z-index: 1000;
    border-bottom: 1px solid #f1f2f3;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  }

  .header-top {
    /* 移除 max-width 或者将其设为 100% 以便内容能撑满左右 */
    padding: 0 24px;
    display: flex;
    align-items: center;
    height: 64px;
  }

  /* 左侧区域：强制靠左 */
  .header-left {
    display: flex;
    align-items: center;
    flex-shrink: 0; /* 防止被搜索框挤压 */
  }

  .user-avatar-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: opacity 0.2s;
  }

  .avatar-circle {
    width: 40px;
    height: 40px;
    background-color: #fb7299; /* 主题粉色 */
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

  /* 中间搜索框：自适应宽度并保持居中感 */
  .search-bar-container {
    flex: 1;
    display: flex;
    justify-content: center; /* 让搜索框在剩余空间中居中 */
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
    border-radius: 20px; /* 胶囊形状 */
    font-size: 14px;
    outline: none;
    transition: all 0.3s;
  }

  .round-input:focus {
    background-color: #fff;
    border-color: #fb7299;
    box-shadow: 0 0 6px rgba(251, 114, 153, 0.15);
  }

  .header-right {
    flex-shrink: 0;
  }

  .content-container {
    max-width: 1160px;
    margin: 0 auto;
    padding: 84px 20px 20px;
  }
</style>
