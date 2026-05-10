<template>
  <div class="manager-layout">
    <header class="nav-header">
      <div class="header-top">
        <div class="header-left">
          <div class="admin-brand">
            <span class="brand-text">系统后台管理中心</span>
          </div>
        </div>
        <div class="admin-user-info">
          <span class="admin-name">{{ adminName }}</span>
          <el-button size="small" type="danger" plain @click="handleLogout">退出后台</el-button>
        </div>
      </div>
    </header>

    <div class="content-container">
      <ManagerContent :adminInfo="currentAdmin" />
    </div>
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import ManagerContent from './ManagerContent.vue';

  const router = useRouter();
  const adminName = ref('加载中...');
  const currentAdmin = ref(null);

  // 初始化获取当前登录的管理员信息
  onMounted(() => {
    const userJson = localStorage.getItem('currentUser');
    if (userJson) {
      const user = JSON.parse(userJson);
      currentAdmin.value = user;
      // 显示昵称
      adminName.value = user.nickname || user.username || '管理员';
    } else {
      router.push('/login');
    }
  });

  const handleLogout = () => {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    router.push('/login');
  };
</script>

<style scoped>
  .manager-layout {
    background-color: #f7f8fa;
    min-height: 100vh;
  }

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
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 64px;
  }

  .brand-text {
    font-size: 18px;
    font-weight: 600;
    color: #18191c;
  }

  .admin-user-info {
    display: flex;
    align-items: center;
    gap: 15px;
  }

  .admin-name {
    font-size: 14px;
    color: #fb7299; /* 粉色主题 */
    font-weight: bold;
  }

  .content-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 84px 20px 20px;
  }
</style>
