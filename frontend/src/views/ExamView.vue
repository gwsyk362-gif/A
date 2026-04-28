<template>
  <div v-if="questions && questions.length > 0" class="exam-container">

    <div class="cards-grid">
      <div v-for="(question, index) in questions" :key="question.quesId" class="question-card">

        <div class="question-header">
          <div class="question-title">
            {{ index + 1 }}. {{ question.quesContent }}
          </div>
          <span v-if="question.quesKp" class="knowledge-tag">
            {{ question.quesKp }}
          </span>
        </div>

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
                :disabled="question.submitted"
              />
              <span>{{ opt }}</span>
            </label>
          </div>
        </div>

        <div class="card-footer">
          <button
            v-if="!question.submitted"
            :disabled="!question.userAnswer"
            class="submit-single-btn"
            @click="submitSingle(question)">
            提交答案
          </button>

          <div v-else :class="['result-box', question.userAnswer === question.quesAnswer ? 'correct' : 'wrong']">
            <span v-if="question.userAnswer === question.quesAnswer">✓ 正确</span>
            <span v-else>✗ 错误。正确答案是：{{ question.quesAnswer }}</span>
          </div>
        </div>

      </div>
    </div> <div class="action-bar">
    <button @click="resetPaper" class="clear-btn">清空当前题目</button>
  </div>

  </div>

  <div v-else class="empty-state">
    <p>暂无符合该知识点的题目，请更换条件重试...</p>
  </div>
</template>

<script setup>
  import { defineProps, defineEmits } from 'vue';

  const props = defineProps({
    questions: {
      type: Array,
      default: () => []
    }
  });

  const emit = defineEmits(['reset']);

  const formatOptions = (optionsStr) => {
    if (!optionsStr) return [];
    const cleanStr = optionsStr.replace(/\s+/g, ' ').trim() + " ";
    const regex = /[A-D][\.．、\s][\s\S]*?(?=[A-D][\.．、\s]|$)/g;
    const matches = cleanStr.match(regex);
    return matches ? matches.map(o => o.trim()).filter(o => o.length > 2) : [];
  };

  const getOptionLetter = (optText) => {
    return optText.trim().charAt(0).toUpperCase();
  };

  // 单题选择
  const selectAnswer = (question, opt) => {
    if (!question.submitted) {
      question.userAnswer = getOptionLetter(opt);
    }
  };

  // ✨ 单题提交逻辑
  const submitSingle = (question) => {
    if (question.userAnswer) {
      question.submitted = true; // 只锁定并显示当前这道题的结果
    }
  };

  // 清空面板
  const resetPaper = () => {
    emit('reset');
  };
</script>

<style scoped>
  .exam-container {
    margin-top: 20px;
  }

  .cards-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
    align-items: start;
    margin-bottom: 30px;
  }

  .question-card {
    border: none;
    border-radius: 16px;
    background: #ffffff;
    padding: 24px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    display: flex;
    flex-direction: column; /* 让底部区域能推到底部 */
  }

  .question-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
  }

  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 15px;
    margin-bottom: 15px;
  }
  .question-title {
    font-weight: 700;
    font-size: 1.05em;
    line-height: 1.5;
    margin-bottom: 0;
    flex: 1;
  }
  .knowledge-tag {
    background: #eef4ff;
    color: #3478e5;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    white-space: nowrap;
    border: 1px solid #d1e3fa;
  }

  .options-group {
    margin-top: 10px;
    flex: 1; /* 撑开中间区域 */
  }
  .option-item {
    margin: 8px 0;
    padding: 10px 14px;
    border: 1px solid #f0f2f5;
    border-radius: 10px;
    background: #fafafa;
    transition: all 0.2s;
    cursor: pointer;
  }
  .option-item:hover:not(.disabled) {
    background: #f0f7ff;
  }
  .option-item.selected {
    background: #f0f7ff;
    border-color: #3478e5;
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

  /* ✨ 单题提交区域样式 */
  .card-footer {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px dashed #f0f2f5;
    display: flex;
    justify-content: flex-end;
  }
  .submit-single-btn {
    padding: 8px 20px;
    background: #3478e5;
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    font-weight: bold;
    transition: background 0.3s;
  }
  .submit-single-btn:disabled {
    background: #e1e4ea;
    color: #9499a0;
    cursor: not-allowed;
  }
  .submit-single-btn:hover:not(:disabled) {
    background: #285fb3;
  }

  .result-box {
    width: 100%;
    padding: 10px 15px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: bold;
    text-align: center;
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
  .clear-btn {
    padding: 12px 40px;
    border: none;
    border-radius: 8px;
    color: #fff;
    font-weight: bold;
    cursor: pointer;
    background: #9499a0;
  }

  .empty-state {
    text-align: center;
    padding: 50px;
    color: #999;
    font-size: 16px;
    background: #f9fafc;
    border-radius: 10px;
    margin-top: 20px;
  }
</style>
