<template>
  <div class="app-container">
    <GenerateView @paper-generated="setupPaper" />
    
    <hr />

    <ExamView :paper="currentPaper" @reset="currentPaper = []" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import GenerateView from './views/GenerateView.vue';
import ExamView from './views/ExamView.vue';

const currentPaper = ref([]);

const setupPaper = (data) => {
  currentPaper.value = data;
};
</script>
<style scoped></style>
