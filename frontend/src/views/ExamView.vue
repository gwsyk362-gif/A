<template>
  <div v-if="questions && questions.length > 0" class="exam-container">

    <template v-if="!isPracticeMode">
      <div v-if="showResult" class="score-banner">
        正确率：{{ score }} / {{ questions.length }}
      </div>

      <div class="paper-list">
        <div v-for="(question, index) in questions" :key="question.quesId" class="paper-question-item">
          <div class="question-header">
            <div class="question-title">
              {{ index + 1 }}. {{ question.quesContent }}
            </div>
            <div class="header-right">
              <span v-if="question.quesKp" class="knowledge-tag">{{ question.quesKp }}</span>
              <button
                class="fav-btn"
                :class="{ 'is-fav': isFavorited(question.quesId) }"
                @click="toggleFavorite(question)"
                title="收藏题目"
              >
                {{ isFavorited(question.quesId) ? '★' : '☆' }}
              </button>
            </div>
          </div>

          <div class="options-group">
            <div
              v-for="(opt, optIndex) in formatOptions(question.quesOptions)"
              :key="optIndex"
              class="option-item"
              :class="{ 'selected': question.userAnswer === getOptionLetter(opt) }"
              @click="!showResult && selectAnswer(question, opt)"
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
              <button
                class="fav-btn"
                :class="{ 'is-fav': isFavorited(question.quesId) }"
                @click="toggleFavorite(question)"
                title="收藏题目"
              >
                {{ isFavorited(question.quesId) ? '★' : '☆' }}
              </button>
            </div>
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
      </div>
    </template>

  </div>

  <div v-else class="empty-state">
    <p>暂无符合条件的题目</p>
  </div>
</template>

<script setup>
  import { defineProps, defineEmits, computed, ref, onMounted, watch } from 'vue';
  import axios from 'axios';

  const props = defineProps({
    questions: {
      type: Array,
      default: () => []
    }
  });

  const emit = defineEmits(['reset']);

  // 判断当前是组卷还是练习
  const isPracticeMode = computed(() => {
    return props.questions.length > 0 && props.questions[0].isPractice === true;
  });

  const favoriteIds = ref([]); // 存放当前用户已收藏的题目ID

  // 获取当前用户ID
  const getCurrentUserId = () => {
    const savedUser = localStorage.getItem('currentUser');
    if (!savedUser) return null;
    const user = JSON.parse(savedUser);
    return user.userId;
  };

// 加载用户的收藏ID列表
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


  // 判断某题是否已收藏
  const isFavorited = (quesId) => {
    return favoriteIds.value.includes(quesId);
  };

  const toggleFavorite = async (question) => {
    const userId = getCurrentUserId();
    if (!userId) {
      alert("请先登录再进行收藏");
      return;
    }

    const quesId = question.quesId;
    const currentlyFavorited = isFavorited(quesId);
    const url = currentlyFavorited
      ? 'http://localhost:8080/favoriteQuestions/remove'
      : 'http://localhost:8080/favoriteQuestions/add';

    const favData = {
      favUserId: userId,
      favQuesId: quesId
    };

    try {
      // 发送请求给后端
      await axios.post(url, favData);

      // 前端状态同步
      if (currentlyFavorited) {
        // 取消收藏
        favoriteIds.value = favoriteIds.value.filter(id => id !== quesId);
      } else {
        // 添加收藏
        favoriteIds.value.push(quesId);
      }
    } catch (error) {
      alert("收藏操作失败，请检查网络");
    }
  };

  // 当题目列表变化时，重新加载收藏状态
  watch(() => props.questions, (newQuestions) => {
    if (newQuestions && newQuestions.length > 0) {
      loadFavorites();
    }
  }, { immediate: true });


  // ====== 组卷模式 ======
  const showResult = ref(false);
  const score = ref(0);

  const submitPaper = () => {
    let currentScore = 0;
    props.questions.forEach(q => {
      if (q.userAnswer === q.quesAnswer) currentScore++;
    });
    score.value = currentScore;
    showResult.value = true;
  };

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

  const selectAnswer = (question, opt) => {
    // 如果是练习模式且已提交，不允许再选
    if (isPracticeMode.value && question.submitted) return;
    // 如果是组卷模式且已交卷，不允许再选
    if (!isPracticeMode.value && showResult.value) return;

    question.userAnswer = getOptionLetter(opt);
  };

  // 练习模式：单题提交
  const submitSingle = async (question) => {
    if (!question.userAnswer) return;

    // 1. 获取当前用户ID
    const userId = getCurrentUserId();
    if (!userId) {
      alert("登录已过期或未登录，请先登录");
      return;
    }

    // 2. 判题逻辑：比对用户答案与正确答案
    const isCorrect = (question.userAnswer === question.quesAnswer) ? 1 : 0;

    // 3. 构造符合 PracticeRecord 实体类的数据结构
    const recordData = {
      recUserId: userId,
      recQuesId: question.quesId,
      recUserAnswer: question.userAnswer,
      recIsCorrect: isCorrect
    };

    try {
      // 4. 发送 POST 请求保存记录
      await axios.post('/api/records/submitBatch', [recordData]);

      // 5. 保存成功后，才在前端标记为已提交，展示对错解析
      question.submitted = true;

    } catch (error) {
      console.error("保存做题记录失败:", error);
      alert("提交失败，请检查网络连接");
    }
  };

  // 清空面板
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
