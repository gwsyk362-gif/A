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
          :disabled="!selectedSubjectId || recommendCount <= 0"
          @click="handleRecommend"
        >
          刷新题目
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
  </div>
</template>

<script setup>
  // ✨ 1. 别忘了从 vue 中导入 watch
  import { ref, onMounted, defineEmits, defineProps, watch } from 'vue';

  const props = defineProps({
    currentUser: Object,
    mode: String
  });

  const subjects = ref([]);
  const selectedSubjectId = ref('');
  const questionCount = ref(5);
  const recommendCount = ref(5);
  const errorMessage = ref('');

  // ✨ 2. 新增的响应式变量，用于存放知识点下拉列表和用户选中的知识点
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

  // 监听所选科目的变化。一旦换了科目，就拉取对应的知识点
  watch(selectedSubjectId, async (newVal) => {
    selectedKnowledgePoint.value = ''; // 切换科目时，清空之前选的知识点
    knowledgePoints.value = []; // 清空旧的知识点列表

    if (!newVal) return;
    try {
      const res = await fetch(`http://localhost:8080/knowledgePoints?subId=${newVal}`);
      if (!res.ok) throw new Error('无法加载知识点');
      const data = await res.json();
      knowledgePoints.value = data;
    } catch (err) {
      console.error('获取知识点失败:', err);
    }
  });

 // 组卷模式 (打上 isPractice: false 标记)
  const handleGenerate = async () => {
    errorMessage.value = '';
    try {
      const res = await fetch(`http://localhost:8080/generate?subId=${selectedSubjectId.value}&count=${questionCount.value}`);
      if (!res.ok) throw new Error('生成试卷失败');
      const data = await res.json();
      // ✨ 加上 isPractice: false
      emit('paper-generated', data.map(q => ({ ...q, userAnswer: '', submitted: false, isPractice: false })));
    } catch (err) {
      errorMessage.value = err.message;
    }
  };

  // 练习模式 (打上 isPractice: true 标记)
  // 练习：获取个性化推荐题目
  const handleRecommend = async () => {
    errorMessage.value = '';
    try {
      // 1. 从 localStorage 获取当前用户的 ID
      const savedUser = localStorage.getItem('currentUser');
      if (!savedUser) {
        errorMessage.value = "请先登录";
        return;
      }
      const user = JSON.parse(savedUser);
      const userId = user.userId;

      // 2. 调用recommend 接口
      let url = `http://localhost:8080/recommend?userId=${userId}&count=${recommendCount.value}&subjectId=${selectedSubjectId.value}`;

      if (selectedKnowledgePoint.value) {
        url += `&kp=${encodeURIComponent(selectedKnowledgePoint.value)}`;
      }

      const res = await fetch(url);
      if (!res.ok) throw new Error('获取推荐题目失败');

      const data = await res.json();

      // 补充前端所需的状态字段
      emit('recommend-fetched', data.map(q => ({
        ...q,
        userAnswer: '',
        submitted: false,
        isPractice: true
      })));
    } catch (err) {
      errorMessage.value = err.message;
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

  .logout-btn {
    padding: 4px 10px;
    background: #ff4d4f;
    font-size: 12px;
  }

  .logout-btn:hover {
    background: #ff7875;
  }

  .config-section {
    display: flex;
    flex-direction: column;
    gap: 25px;
    margin-top: 30px;
  }

  /* 核心布局：水平居中 */
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
    align-items: flex-start; /* 标签文字左对齐 */
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
