<template>
  <div class="main-content-wrapper">
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

    <div v-if="activeTab === 'resources'" class="view-section">
      <VideoResources :search-query="searchQuery" />
    </div>

    <div v-else-if="activeTab === 'textResources'" class="view-section">
      <TextResources :search-query="searchQuery" />
    </div>

    <div v-else-if="activeTab === 'paper'" class="view-section">
      <PaperView />
    </div>

    <div v-else-if="activeTab === 'practice'" class="view-section">
      <PracticeView />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import VideoResources from './VideoResources.vue';
import TextResources from './TextResources.vue';
import PaperView from './PaperView.vue';
import PracticeView from './PracticeView.vue';

defineProps(['user', 'searchQuery']);

const activeTab = ref('resources');

const tabs = [
  { id: 'resources', name: '视频学习资源' },
  { id: 'textResources', name: '图文学习资源' },
  { id: 'paper', name: '全真模拟' },
  { id: 'practice', name: '训练大厅' }
];
</script>

<style scoped>
.category-nav {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  border-bottom: 1px solid #f2f2f2;
  padding: 0 20px;
  justify-content: center;
}
.nav-item {
  padding: 10px 0;
  cursor: pointer;
  position: relative;
  font-size: 15px;
  color: #61666d;
  transition: color 0.2s;
}
.nav-item:hover { color: #18191c; }
.nav-item.active { color: #fb7299; font-weight: 700; }
.active-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  border-radius: 2px;
  background: #fb7299;
}
.view-section {
  padding: 0;
}
</style>
