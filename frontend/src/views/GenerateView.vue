<template>
  <div class="generate-container">
    <h1>个性化学习助手</h1>

    <div class="config-section">
      <div class="row central-row">
        <template v-if="mode === 'paper'">
          <label>选择科目：
            <select v-model="selectedSubjectId">
              <option disabled value="">请选择科目</option>
              <option v-for="subject in subjects" :key="subject.subId" :value="subject.subId">
                {{ subject.subName }}
              </option>
            </select>
          </label>

          <label>组卷题数：
            <input type="number" v-model.number="questionCount" />
          </label>
        </template>

        <template v-else-if="mode === 'practice'">
          <label>选择科目：
            <select v-model="selectedSubjectId">
              <option disabled value="">请选择科目</option>
              <option v-for="subject in subjects" :key="subject.subId" :value="subject.subId">
                {{ subject.subName }}
              </option>
            </select>
          </label>

          <label>选择知识点：
            <select v-model="selectedKnowledgePoint" :disabled="!selectedSubjectId">
              <option value="">全部知识点</option>
              <option v-for="kp in knowledgePoints" :key="kp" :value="kp">
                {{ kp }}
              </option>
            </select>
          </label>

        </template>
      </div>

      <div class="row action-row">
        <button
          v-if="mode === 'paper'"
          :disabled="!selectedSubjectId || questionCount <= 0"
          @click="handleGenerate"
        >
          生成试卷
        </button>

        <button
          v-if="mode === 'practice'"
          :disabled="!selectedSubjectId"
          @click="handlePracticeFetch"
        >
          获取题目
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
  </div>
</template>

<script setup>
  import { ref, onMounted, watch } from 'vue';

  const props = defineProps({
    currentUser: Object,
    mode: String
  });

  const subjects = ref([]);
  const selectedSubjectId = ref('');
  const questionCount = ref(10);
  const errorMessage = ref('');

  // 知识点相关的响应式变量
  const knowledgePoints = ref([]);
  const selectedKnowledgePoint = ref('');

  const emit = defineEmits(['paper-generated', 'recommend-fetched', 'logout']);

  // 加载科目
  const loadSubjects = async () => {
    try {
      const res = await fetch('http://localhost:8080/subjects');
      if (!res.ok) throw new Error('无法加载科目');
      subjects.value = await res.json();
    } catch (err) {
      errorMessage.value = err.message;
    }
  };

  // 监听所选科目的变化，拉取对应的知识点
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

  // ====== 组卷模式 ======
// ====== 组卷模式 ======
  const handleGenerate = async () => {
    errorMessage.value = '';
    try {
      // 1. 获取当前登录用户
      const savedUser = localStorage.getItem('currentUser');
      if (!savedUser) {
        errorMessage.value = "请先登录";
        return;
      }
      const userId = JSON.parse(savedUser).userId;

      // 2. 调用后端新的个性化组卷接口
      const res = await fetch(`http://localhost:8080/generatePersonalized?userId=${userId}&subId=${selectedSubjectId.value}&count=${questionCount.value}`);
      if (!res.ok) throw new Error('生成试卷失败');

      const data = await res.json();
      // 组卷模式 isPractice 依然是 false
      emit('paper-generated', data.map(q => ({ ...q, userAnswer: '', submitted: false, isPractice: false })));
    } catch (err) {
      errorMessage.value = err.message;
    }
  };

  // ====== 练习模式 (触发推荐算法) ======
  const handlePracticeFetch = async () => {
    errorMessage.value = '';
    try {
      const savedUser = localStorage.getItem('currentUser');
      if (!savedUser) {
        errorMessage.value = "请先登录";
        return;
      }
      const user = JSON.parse(savedUser);
      const userId = user.userId;

      // 重点：调用后端新写的 RecommendController
      let url = `http://localhost:8080/recommend/getQuestions?userId=${userId}&subjectId=${selectedSubjectId.value}&count=${questionCount.value}`;

      if (selectedKnowledgePoint.value) {
        url += `&kp=${encodeURIComponent(selectedKnowledgePoint.value)}`;
      }

      const res = await fetch(url);
      if (!res.ok) throw new Error('推荐系统获取题目失败');

      const data = await res.json();

      // 将推荐的题目打上 isPractice: true 的标记，交给 ExamView 渲染
      emit('recommend-fetched', data.map(q => ({
        ...q,
        userAnswer: '',
        submitted: false,
        isPractice: true
      })));
    } catch (err) {
      errorMessage.value = err.message;
      console.error("推荐请求出错:", err);
    }
  };

  onMounted(loadSubjects);
</script>

<style scoped>
  .generate-container {
    position: relative;
    padding: 60px 20px 40px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    text-align: center;
  }

  .config-section {
    display: flex;
    flex-direction: column;
    gap: 25px;
    margin-top: 30px;
  }

  .central-row {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 30px;
  }

  .action-row {
    display: flex;
    justify-content: center;
    gap: 20px;
  }

  label {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    font-size: 14px;
    color: #666;
  }

  select, input {
    width: 180px;
    padding: 10px 12px;
    border-radius: 6px;
    border: 1px solid #ccd0d5;
    margin-top: 6px;
  }

  button {
    padding: 10px 24px;
    border: none;
    border-radius: 6px;
    background: #3478e5;
    color: #fff;
    cursor: pointer;
    transition: background 0.3s;
  }

  button:hover:not(:disabled) {
    background: #285fb3;
  }

  button:disabled {
    background: #a5b4cf;
    cursor: not-allowed;
  }

  .error {
    color: #c0392b;
    padding: 10px;
    background: #fdf2f2;
    margin: 20px auto 0;
    max-width: 400px;
    border-radius: 6px;
  }
</style>
