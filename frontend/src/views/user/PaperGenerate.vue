<template>
  <div class="paper-generate">
    <h3 class="section-title">📝 智能组卷</h3>
    <div class="config-card">
      <div class="config-body">
        <div class="config-item">
          <span class="config-label">选择科目</span>
          <div class="chip-row">
            <span
              v-for="sub in subjects"
              :key="sub.subId"
              class="chip"
              :class="{ active: selectedSubjectId === sub.subId }"
              @click="selectedSubjectId = sub.subId"
            >{{ sub.subName }}</span>
          </div>
        </div>
        <div class="config-item">
          <span class="config-label">题目数量</span>
          <div class="count-picker">
            <button class="count-btn" @click="adjustCount(-1)">−</button>
            <input
              class="count-input"
              type="number"
              v-model.number="questionCount"
              @blur="clampCount"
              min="1"
              max="50"
            />
            <button class="count-btn" @click="adjustCount(1)">+</button>
          </div>
        </div>
        <button
          class="action-btn"
          :disabled="!selectedSubjectId || questionCount <= 0"
          @click="handleGenerate"
        >生成试卷</button>
      </div>
    </div>
    <div v-if="errorMessage" class="error-msg">{{ errorMessage }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const subjects = ref([]);
const selectedSubjectId = ref('');
const questionCount = ref(10);
const errorMessage = ref('');

const emit = defineEmits(['paper-generated']);

const adjustCount = (delta) => {
  questionCount.value = questionCount.value + delta;
  clampCount();
};

const clampCount = () => {
  if (!Number.isFinite(questionCount.value) || questionCount.value < 1) {
    questionCount.value = 1;
  } else if (questionCount.value > 50) {
    questionCount.value = 50;
  }
};

const loadSubjects = async () => {
  try {
    const res = await fetch('http://localhost:8080/subjects');
    if (!res.ok) throw new Error('无法加载科目');
    subjects.value = await res.json();
  } catch (err) {
    errorMessage.value = err.message;
  }
};

const handleGenerate = async () => {
  errorMessage.value = '';
  try {
    const savedUser = localStorage.getItem('currentUser');
    if (!savedUser) {
      errorMessage.value = '请先登录';
      return;
    }
    const userId = JSON.parse(savedUser).userId;
    const res = await fetch(`http://localhost:8080/generatePersonalized?userId=${userId}&subId=${selectedSubjectId.value}&count=${questionCount.value}`);
    if (!res.ok) throw new Error('生成试卷失败');
    const data = await res.json();
    emit('paper-generated', data.map(q => ({ ...q, userAnswer: '', submitted: false, isPractice: false })));
  } catch (err) {
    errorMessage.value = err.message;
  }
};

onMounted(loadSubjects);
</script>

<style scoped>
.section-title { border-left-color: #fb7299; }
.count-input {
  width: 60px;
  height: 36px;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #18191c;
  border: 1px solid #e3e5e8;
  border-radius: 8px;
  outline: none;
  background: #fff;
  -moz-appearance: textfield;
}
.count-input::-webkit-inner-spin-button,
.count-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.count-input:focus {
  border-color: #fb7299;
  box-shadow: 0 0 0 2px rgba(251, 114, 153, 0.15);
}
</style>