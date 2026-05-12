<template>
  <div class="paper-view">
    <PaperGenerate @paper-generated="handlePaperGenerated" />
    <div v-if="questions.length > 0" class="section-divider"></div>
    <ExamView v-if="questions.length > 0" :questions="questions" @reset="handleReset" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import PaperGenerate from './PaperGenerate.vue';
import ExamView from './ExamView.vue';

const questions = ref([]);

const handlePaperGenerated = (data) => { questions.value = data; };
const handleReset = () => { questions.value = []; };
</script>