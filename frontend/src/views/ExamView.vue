<template>
  <div v-if="questions.length" class="exam-container">
    <div v-if="showResult" class="score-banner">
      正确率：{{ score }} / {{ questions.length }}
    </div>

    <div v-for="(question, index) in questions" :key="question.quesId" class="question-card">
      <div class="question-title">{{ index + 1 }}. {{ question.quesContent }}</div>

      <div class="options-group">
        <div
          v-for="(opt, optIndex) in formatOptions(question.quesOptions)"
          :key="optIndex"
          class="option-item"
          :class="{ 'selected': question.userAnswer === getOptionLetter(opt) }"
          @click="selectAnswer(question, opt)"
        >
          <label class="option-label">
            <input
              type="radio"
              :name="'q-' + question.quesId"
              :value="getOptionLetter(opt)"
              v-model="question.userAnswer"
              :disabled="showResult"
            />
            <span>{{ opt }}</span>
          </label>
        </div>
      </div>

      <div v-if="showResult" :class="['result-box', question.userAnswer === question.quesAnswer ? 'correct' : 'wrong']">
        <span v-if="question.userAnswer === question.quesAnswer">✓ 正确</span>
        <span v-else>✗ 错误。正确答案是：{{ question.quesAnswer }}</span>
      </div>
    </div>

    <div class="action-bar">
      <button v-if="!showResult" @click="submitPaper" class="submit-btn">提交所有答案</button>
      <button v-else @click="resetPaper" class="clear-btn">清除当前试卷</button>
    </div>
  </div>
</template>

<script setup>
  import { ref, defineProps, defineEmits, watch } from 'vue';

  const props = defineProps({
  questions: Array
});

  const showResult = ref(false);
  const score = ref(0);
  const emit = defineEmits(['reset']);

  watch(() => props.questions, () => {
  showResult.value = false;
  score.value = 0;
}, { deep: false });

  // 解析选项文本 (A.xxx B.xxx)
  const formatOptions = (optionsStr) => {
    if (!optionsStr) return [];
    return optionsStr
      .split(/(?=[A-D][\.．、\s])/)
      .map(o => o.trim())
      .filter(o => o.length > 0);
  };

  // 获取选项字母 (A, B, C, D)
  const getOptionLetter = (optText) => {
    return optText.trim().charAt(0).toUpperCase();
  };

  // 处理点击选择
  const selectAnswer = (question, opt) => {
    if (!showResult.value) {
      question.userAnswer = getOptionLetter(opt);
    }
  };

  // 提交判分
  const submitPaper = () => {
    score.value = props.questions.filter(q => q.userAnswer === q.quesAnswer).length;
    showResult.value = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // 重置状态
  const resetPaper = () => {
    showResult.value = false;
    score.value = 0;
    emit('reset'); // 通知父组件清空数据
  };
</script>

<style scoped>
.exam-container {
  margin-top: 20px;
}
.score-banner {
  text-align: center;
  font-size: 24px;
  padding: 20px;
  color: #3478e5;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
}
.question-card {
  border: 1px solid #e1e4ea;
  border-radius: 10px;
  background: #fbfdff;
  padding: 20px;
  margin-bottom: 16px;
}
.question-title {
  font-weight: 700;
  margin-bottom: 15px;
  font-size: 1.1em;
}
.option-item {
  margin: 10px 0;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 6px;
  transition: all 0.2s;
  cursor: pointer;
}
.option-item:hover:not(.disabled) {
  background: #f0f7ff;
}
.option-label {
  cursor: pointer;
  display: flex;
  align-items: center;
  width: 100%;
}
.option-label input {
  margin-right: 12px;
}
.result-box {
  margin-top: 15px;
  padding: 12px;
  border-radius: 6px;
  font-weight: bold;
}
.correct {
  background: #e6fffa;
  color: #27ae60;
  border: 1px solid #b2f2bb;
}
.wrong {
  background: #fff5f5;
  color: #c0392b;
  border: 1px solid #feb2b2;
}
.action-bar {
  margin-top: 30px;
  text-align: center;
}
button {
  padding: 12px 40px;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-weight: bold;
  cursor: pointer;
}
.submit-btn { background: #27ae60; }
.clear-btn { background: #7f8c8d; }
</style>
