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

          <label>推荐题数：
            <input type="number" v-model.number="recommendCount" />
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
  import { ref, onMounted, defineEmits, defineProps } from 'vue';

  const props = defineProps({
    currentUser: Object,
    mode: String
  });

  const subjects = ref([]);
  const selectedSubjectId = ref('');
  const questionCount = ref(5);
  const recommendCount = ref(5);
  const errorMessage = ref('');

  // 定义所有需要的 emit
  const emit = defineEmits(['paper-generated', 'recommend-fetched', 'logout']);

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
      const res = await fetch(`http://localhost:8080/generate?subId=${selectedSubjectId.value}&count=${questionCount.value}`);
      if (!res.ok) throw new Error('生成试卷失败');
      const data = await res.json();
      emit('paper-generated', data.map(q => ({ ...q, userAnswer: '' })));
    } catch (err) {
      errorMessage.value = err.message;
    }
  };

  const handleRecommend = async () => {
  errorMessage.value = '';
  const userId = props.currentUser?.userId;
  if (!userId) {
    errorMessage.value = '用户信息缺失，请重新登录';
    return;
  }
  try {
    const res = await fetch(`http://localhost:8080/recommend?userId=${userId}&count=${recommendCount.value}&subjectId=${selectedSubjectId.value}`);

    if (!res.ok) throw new Error('获取推荐失败');
    const data = await res.json();
    emit('recommend-fetched', data);
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
