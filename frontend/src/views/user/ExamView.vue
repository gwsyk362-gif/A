<template>
  <div v-if="questions && questions.length > 0" class="exam-container">

    <template v-if="!isPracticeMode">
      <div v-if="showResult" class="score-banner">正确率：{{ score }} / {{ questions.length }}</div>
      <div class="paper-list">
        <div v-for="(question, index) in questions" :key="question.quesId" class="paper-question-item">
          <div class="question-header">
            <div class="question-title">{{ index + 1 }}. {{ question.quesContent }}</div>
          </div>
          <div class="options-group">
            <div
              v-for="(opt, optIndex) in formatOptions(question.quesOptions)" :key="optIndex"
              class="option-item" :class="{ 'selected': question.userAnswer === getOptionLetter(opt) }"
              @click="!showResult && selectAnswer(question, opt)">
              <label class="option-label">
                <input type="radio" :name="'q-' + question.quesId" :value="getOptionLetter(opt)" v-model="question.userAnswer" :disabled="showResult" />
                <span>{{ opt }}</span>
              </label>
            </div>
          </div>
          <div v-if="showResult" class="result-container">
            <div :class="['result-box', question.userAnswer === question.quesAnswer ? 'correct' : 'wrong']">
              <span v-if="question.userAnswer === question.quesAnswer">✓ 正确</span>
              <span v-else>✗ 错误。正确答案是：{{ question.quesAnswer }}</span>
            </div>

            <div class="analysis-box">
              <strong class="analysis-title">💡 题目解析：</strong>
              <p class="analysis-content">{{ question.quesAnalysis || '该题目暂无解析' }}</p>
            </div>
          </div>
        </div>
      </div>
      <div class="action-bar">
        <button v-if="!showResult" @click="submitPaper" class="submit-btn">提交所有答案</button>
        <button v-else @click="resetPaper" class="clear-btn">清除当前试卷</button>
      </div>
    </template>

    <template v-else>
      <div class="cards-grid">
        <div v-for="(question, index) in questions" :key="question.quesId" class="question-card">
          <div class="question-header">
            <div class="question-title">{{ index + 1 }}. {{ question.quesContent }}</div>
            <div class="header-right">
              <span v-if="question.quesKp" class="knowledge-tag">{{ question.quesKp }}</span>
              <button class="fav-btn" :class="{ 'is-fav': isFavorited(question.quesId) }" @click="toggleFavorite(question)" title="收藏题目">
                {{ isFavorited(question.quesId) ? '★' : '☆' }}
              </button>
            </div>
          </div>

          <div class="options-group">
            <div
              v-for="(opt, optIndex) in formatOptions(question.quesOptions)" :key="optIndex"
              class="option-item" :class="{ 'selected': question.userAnswer === getOptionLetter(opt) }"
              @click="selectAnswer(question, opt)">
              <label class="option-label">
                <input type="radio" :name="'q-' + question.quesId" :value="getOptionLetter(opt)" v-model="question.userAnswer" :disabled="question.submitted" />
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
              <span v-else>✗ 错误。正确答案是：{{ question.quesAnswer }}<br><br>解析: {{ question.quesAnalysis || '暂无解析' }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>

  <div v-else class="empty-state">
    <p>暂无符合条件的题目</p>
  </div>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import axios from 'axios';

  const props = defineProps({
    questions: { type: Array, default: () => [] }
  });

  const emit = defineEmits(['reset']);

  // 判断是否为练习模式
  const isPracticeMode = computed(() => {
    return props.questions.length > 0 && props.questions[0].isPractice === true;
  });

  const favoriteIds = ref([]);

  const getCurrentUserId = () => {
    const savedUser = localStorage.getItem('currentUser');
    if (!savedUser) return null;
    return JSON.parse(savedUser).userId;
  };

  const loadFavorites = async () => {
    const userId = getCurrentUserId();
    if (!userId) return;
    try {
      const res = await axios.get(`http://localhost:8080/favoriteQuestions/ids?userId=${userId}`);
      favoriteIds.value = res.data;
    } catch (error) {
      console.error("加载收藏列表失败:", error);
    }
  };

  const isFavorited = (quesId) => favoriteIds.value.includes(quesId);

  const toggleFavorite = async (question) => {
    const userId = getCurrentUserId();
    if (!userId) {
      alert("请先登录再进行收藏");
      return;
    }
    const quesId = question.quesId;
    const currentlyFavorited = isFavorited(quesId);
    const url = currentlyFavorited ? 'http://localhost:8080/favoriteQuestions/remove' : 'http://localhost:8080/favoriteQuestions/add';
    const favData = { favUserId: userId, favQuesId: quesId };

    try {
      await axios.post(url, favData);
      if (currentlyFavorited) {
        favoriteIds.value = favoriteIds.value.filter(id => id !== quesId);
      } else {
        favoriteIds.value.push(quesId);
      }
    } catch (error) {
      alert("收藏操作失败，请检查网络");
    }
  };

  watch(() => props.questions, (newQuestions) => {
    if (newQuestions && newQuestions.length > 0) {
      loadFavorites();
    }
  }, { immediate: true });

  const showResult = ref(false);
  const score = ref(0);

const submitPaper = async () => {
    const userId = getCurrentUserId();
    if (!userId) {
      alert("登录已过期或未登录，请先登录");
      return;
    }

    let currentScore = 0;
    const recordsToSubmit = [];

    // 遍历试卷中的所有题目
    props.questions.forEach(q => {
      const isCorrect = (q.userAnswer === q.quesAnswer) ? 1 : 0;
      if (isCorrect) currentScore++;

      // 只有用户作答了的题目才记录（或者你想记录未作答的为错误，去掉这个if判断即可）
      if (q.userAnswer) {
        recordsToSubmit.push({
          recUserId: userId,
          recQuesId: q.quesId,
          recUserAnswer: q.userAnswer,
          recIsCorrect: isCorrect
        });
      }
    });

    // 1. 先在前端展示分数和解析
    score.value = currentScore;
    showResult.value = true;

    // 2. 将整张试卷的做题情况一次性写入数据库
    if (recordsToSubmit.length > 0) {
      try {
        // 调用后端现有的批量提交接口
        await axios.post('http://localhost:8080/records/submitBatch', recordsToSubmit);
        console.log("整卷做题记录已成功写入数据库，用户能力画像已更新！");
      } catch (error) {
        console.error("保存整卷记录失败:", error);
        alert("成绩已计算，但作答记录保存失败，请检查网络连接");
      }
    }
  };

  const formatOptions = (optionsStr) => {
    if (!optionsStr) return [];
    const cleanStr = optionsStr.replace(/\s+/g, ' ').trim() + " ";
    const regex = /[A-D][\.．、\s][\s\S]*?(?=[A-D][\.．、\s]|$)/g;
    const matches = cleanStr.match(regex);
    return matches ? matches.map(o => o.trim()).filter(o => o.length > 2) : [];
  };

  const getOptionLetter = (optText) => optText.trim().charAt(0).toUpperCase();

  const selectAnswer = (question, opt) => {
    if (isPracticeMode.value && question.submitted) return;
    if (!isPracticeMode.value && showResult.value) return;
    question.userAnswer = getOptionLetter(opt);
  };

  // ====== 练习模式：单题提交，触发后端算法的动态分数更新 ======
  const submitSingle = async (question) => {
    if (!question.userAnswer) return;
    const userId = getCurrentUserId();
    if (!userId) {
      alert("登录已过期或未登录，请先登录");
      return;
    }

    const isCorrect = (question.userAnswer === question.quesAnswer) ? 1 : 0;
    const recordData = {
      recUserId: userId,
      recQuesId: question.quesId,
      recUserAnswer: question.userAnswer,
      recIsCorrect: isCorrect
    };

    try {
      // 提交后，你的后端 PracticeRecordServiceimpl 会自动计算并更新 Elo 能力分！
      await axios.post('http://localhost:8080/records/submitBatch', [recordData]);
      question.submitted = true;
    } catch (error) {
      console.error("保存做题记录失败:", error);
      alert("提交失败，请检查网络连接");
    }
  };

  const resetPaper = () => {
    showResult.value = false;
    score.value = 0;
    emit('reset');
  };
</script>

<style scoped>
  .exam-container {
    margin-top: 20px;
  }

  /* 通用：题干和选项 */
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

  .header-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
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

  .fav-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.3em;
    color: #ccc; /* 默认灰色网格星 */
    padding: 0;
    line-height: 1;
    transition: color 0.2s, transform 0.1s;
  }
  .fav-btn:hover {
    transform: scale(1.1);
    color: #f1c40f; /* 悬停变黄 */
  }
  .fav-btn.is-fav {
    color: #f1c40f; /* 收藏后实心黄星 */
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

  /* 组卷样式 ====== */
  .score-banner {
    background-color: #eef4ff;
    color: #3478e5;
    padding: 15px 20px;
    border-radius: 8px;
    font-size: 1.2em;
    font-weight: bold;
    text-align: center;
    margin-bottom: 20px;
    border: 1px solid #d1e3fa;
  }
  .paper-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  .paper-question-item {
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    border: 1px solid #eaeaea;
  }

  /* 卡片样式 ====== */
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
    flex-direction: column;
  }
  .question-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
  }
  .options-group {
    margin-top: 10px;
    flex: 1;
  }
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

  /* 结果与按钮 */
  .result-box {
    width: 100%;
    margin-top: 15px;
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
  /* ====== 新增与修改的解析面板样式 ====== */
  .result-container {
    margin-top: 15px;
    display: flex;
    flex-direction: column;
    gap: 12px; /* 结果和解析之间的间距 */
  }

  .result-box {
    width: 100%;
    /* margin-top: 15px;  <-- 如果你原有的样式里有这行，请删掉，由外层 container 的 margin-top 接管 */
    padding: 10px 15px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: bold;
    text-align: center;
  }

  /* 解析框样式 */
  .analysis-box {
    background: #f8f9fa;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    padding: 15px;
    font-size: 14px;
    line-height: 1.6;
    color: #4e555e;
  }

  .analysis-title {
    color: #3478e5;
    display: block;
    margin-bottom: 8px;
    font-size: 15px;
  }

  .analysis-content {
    margin: 0;
    white-space: pre-wrap; /* 保留后端解析文本可能带有的换行符 */
  }

  .action-bar {
    margin-top: 30px;
    text-align: center;
    display: flex;
    justify-content: center;
    gap: 15px;
  }
  .submit-btn {
    padding: 12px 40px;
    background: #27ae60;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
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
