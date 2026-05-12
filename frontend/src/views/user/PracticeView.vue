<template>
  <div class="practice-view">
    <PracticeGenerate @recommend-fetched="handleRecommendFetched" />
    <div v-if="questions.length > 0" class="section-divider"></div>
    <ExamView v-if="questions.length > 0" :questions="questions" @reset="questions = []" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import PracticeGenerate from './PracticeGenerate.vue';
import ExamView from './ExamView.vue';

const questions = ref([]);

const handleRecommendFetched = (data) => { questions.value = data; };
</script>