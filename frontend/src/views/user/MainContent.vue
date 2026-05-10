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
      <ExamView v-if="recommendedQuestions.length > 0" :questions="recommendedQuestions" @reset="recommendedQuestions = []" />
    </div>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import GenerateView from './GenerateView.vue';
  import ExamView from './ExamView.vue';
  import VideoResources from './VideoResources.vue';

  // 接收父组件 MainView 传来的 user 对象和搜索词
  const props = defineProps(['user', 'searchQuery']);

  const activeTab = ref("resources");
  const currentPaper = ref([]);
  const recommendedQuestions = ref([]);

  const tabs = [
    { id: 'resources', name: '学习资源' },
    { id: 'paper', name: '做卷分区' },
    { id: 'practice', name: '做题分区' }
  ];

  const handlePaperGenerated = (data) => { currentPaper.value = data; };
  const handleRecommendFetched = (data) => { recommendedQuestions.value = data; };
  const handleReset = () => { currentPaper.value = []; };
</script>

<style scoped>
  .category-nav {

 display: flex;

 padding: 0 20px;

 gap: 32px;

 height: 46px;

 align-items: center;

 justify-content: center;

 }
   .category-nav { display: flex; gap: 30px; margin-bottom: 20px; border-bottom: 1px solid #f2f2f2; }
   .nav-item { padding: 10px 0; cursor: pointer; position: relative; }
   .nav-item.active { color: #fb7299; font-weight: bold; }
   .active-line { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 20px; height: 3px; background: #fb7299; }
</style>
