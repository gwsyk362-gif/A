<template>
  <div class="practice-generate">
    <h3 class="section-title">🎯 专项练习</h3>
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
          <span class="config-label">知识点（可选）</span>
          <div v-if="!selectedSubjectId" class="chip-placeholder">请先选择科目</div>
          <template v-else>
            <div class="chip-row">
              <span
                class="chip"
                :class="{ active: !selectedKnowledgePoint }"
                @click="selectedKnowledgePoint = ''"
              >全部</span>
              <span
                v-for="kp in knowledgePoints"
                :key="kp"
                class="chip"
                :class="{ active: selectedKnowledgePoint === kp }"
                @click="selectedKnowledgePoint = kp"
              >{{ kp }}</span>
            </div>
            <p v-if="knowledgePoints.length === 0" class="chip-hint">该科目暂无知识点数据</p>
          </template>
        </div>
        <div class="action-row">
          <button
            class="action-btn action-btn-all"
            :disabled="!selectedSubjectId"
            @click="handlePracticeFetch"
          >获取题目</button>
          <button
            class="action-btn action-btn-all"
            :disabled="!selectedSubjectId"
            @click="handleViewAll"
          >查看所有题目</button>
        </div>
      </div>
    </div>
    <div v-if="errorMessage" class="error-msg">{{ errorMessage }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';

const subjects = ref([]);
const selectedSubjectId = ref('');
const knowledgePoints = ref([]);
const selectedKnowledgePoint = ref('');
const errorMessage = ref('');

const emit = defineEmits(['recommend-fetched']);

const loadSubjects = async () => {
  try {
    const res = await fetch('http://localhost:8080/subjects');
    if (!res.ok) throw new Error('无法加载科目');
    subjects.value = await res.json();
  } catch (err) {
    errorMessage.value = err.message;
  }
};

watch(selectedSubjectId, async (newVal) => {
  selectedKnowledgePoint.value = '';
  knowledgePoints.value = [];
  if (!newVal) return;
  try {
    const res = await fetch(`http://localhost:8080/knowledgePoints?subId=${newVal}`);
    if (!res.ok) throw new Error('无法加载知识点');
    knowledgePoints.value = await res.json();
  } catch (err) {
    console.error('获取知识点失败:', err);
  }
});

const handlePracticeFetch = async () => {
  errorMessage.value = '';
  try {
    const savedUser = localStorage.getItem('currentUser');
    if (!savedUser) {
      errorMessage.value = '请先登录';
      return;
    }
    const user = JSON.parse(savedUser);
    const userId = user.userId;
    let url = `http://localhost:8080/recommend/getQuestions?userId=${userId}&subjectId=${selectedSubjectId.value}&count=8`;
    if (selectedKnowledgePoint.value) {
      url += `&kp=${encodeURIComponent(selectedKnowledgePoint.value)}`;
    }
    const res = await fetch(url);
    if (!res.ok) throw new Error('推荐系统获取题目失败');
    const data = await res.json();
    emit('recommend-fetched', data.map(q => ({
      ...q, userAnswer: '', submitted: false, isPractice: true
    })));
  } catch (err) {
    errorMessage.value = err.message;
    console.error('推荐请求出错:', err);
  }
};

const handleViewAll = async () => {
  errorMessage.value = '';
  try {
    let url = `http://localhost:8080/questions/page?current=1&size=9999&subId=${selectedSubjectId.value}`;
    if (selectedKnowledgePoint.value) {
      url += `&kp=${encodeURIComponent(selectedKnowledgePoint.value)}`;
    }
    const res = await fetch(url);
    if (!res.ok) throw new Error('获取题目失败');
    const result = await res.json();
    const data = result.records || [];
    emit('recommend-fetched', data.map(q => ({
      ...q, userAnswer: '', submitted: false, isPractice: true
    })));
  } catch (err) {
    errorMessage.value = err.message;
    console.error('获取全部题目出错:', err);
  }
};

onMounted(loadSubjects);
</script>

<style scoped>
.section-title { border-left-color: #3478e5; }
.chip-placeholder {
  color: #9499a0;
  font-size: 14px;
  padding: 8px 0;
}
.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.action-btn-all {
  background: #fff;
  color: #3478e5;
  border: 1px solid #3478e5;
}
.action-btn-all:hover:not(:disabled) {
  background: #eef4ff;
  color: #285fb3;
  border-color: #285fb3;
}
</style>
