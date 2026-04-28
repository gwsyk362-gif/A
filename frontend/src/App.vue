<template>
  <router-view></router-view>
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
