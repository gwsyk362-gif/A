<template>
  <div class="manager-content-wrapper">
    <nav class="category-nav">
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

    <div class="view-section">
      <div v-if="activeTab === 'dashboard'">
        <Dashboard />
      </div>

      <div v-else-if="activeTab === 'users'">
        <UserManage />
      </div>

      <div v-else-if="activeTab === 'questions'">
        <QuestionManage />
      </div>

      <div v-else-if="activeTab === 'videos'">
        <VideoManage />
      </div>

      <div v-else>
        <el-empty description="正在加载模块或模块不存在..."></el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import Dashboard from './Dashboard.vue';
  import UserManage from './UserManage.vue';
  import QuestionManage from './QuestionManage.vue';
  import VideoManage from './VideoManage.vue';

  const props = defineProps(['adminInfo']);

  const activeTab = ref("dashboard");

  const tabs = [
    { id: 'dashboard', name: '📊 数据大盘' },
    { id: 'users', name: '👥 用户管理' },
    { id: 'questions', name: '📝 题库管理' },
    { id: 'videos', name: '🎬 视频管理' }
  ];
</script>

<style scoped>
  .category-nav {
    display: flex; /* 这个属性决定了它们是横向排列的 */
    gap: 40px;
    margin-bottom: 25px;
    border-bottom: 1px solid #f2f2f2;
    background: #fff;
    padding: 0 20px;
    border-radius: 8px 8px 0 0;
  }

  .nav-item {
    padding: 15px 0;
    cursor: pointer;
    position: relative;
    font-size: 15px;
    color: #61666d;
    transition: color 0.3s;
  }

  .nav-item.active {
    color: #fb7299; /* 粉色主题 */
    font-weight: bold;
  }

  .active-line {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 24px;
    height: 3px;
    background: #fb7299;
    border-radius: 2px;
  }

  .view-section {
    background: #fff;
    padding: 20px;
    border-radius: 0 0 8px 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  }
</style>
