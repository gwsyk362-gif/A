<template>
  <div class="user-center">
    <div style="margin-bottom: 15px;">
      <el-button icon="Back" @click="handleBack">返回首页</el-button>
    </div>

    <el-card class="user-info-card">
      <div class="user-profile">
        <el-avatar :size="80" :src="userInfo.avatarUrl">
          {{ String(userInfo.nickname || userInfo.username || 'U').charAt(0) }}
        </el-avatar>
        <div class="info-text">
          <h2>{{ userInfo.nickname || userInfo.username }}</h2>
          <p class="user-bio">账号：{{ userInfo.username }}</p>
        </div>
        <el-button type="danger" size="small" class="logout-btn-in-card" @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="user-tabs" @tab-click="handleTabClick">

      <!-- 1. 学习数据 -->
      <el-tab-pane label="学习数据" name="statistics">
        <div class="statistics-container">
          <el-card class="chart-card" shadow="hover" v-loading="loadingStats">
            <template #header>
              <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                <span>知识点掌握情况</span>
                <el-select v-model="selectedSubjectForChart" size="small" style="width: 150px" @change="updateRadarChart" placeholder="请选择科目">
                  <el-option v-for="sub in subjects" :key="sub.subId" :label="sub.subName" :value="sub.subId" />
                </el-select>
              </div>
            </template>
            <div ref="radarChartRef" class="chart-box" style="height: 450px;"></div>
          </el-card>

          <el-card class="chart-card" shadow="hover" v-loading="loadingStats">
            <template #header>
              <div class="card-header">
                <span>近7日做题统计</span>
              </div>
            </template>
            <div ref="lineChartRef" class="chart-box" style="height: 450px;"></div>
          </el-card>
        </div>

        <el-card class="chart-card" shadow="hover" v-loading="loadingStats" style="width: 100%; margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>全年学习活跃度</span>
            </div>
          </template>
          <div ref="heatmapChartRef" class="chart-box" style="height: 250px;"></div>
        </el-card>

        <el-card class="chart-card" shadow="hover" style="width: 100%; margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>各科目知识点覆盖率</span>
            </div>
          </template>
          <div ref="coverageChartRef" class="chart-box" style="height: 300px;"></div>
        </el-card>
      </el-tab-pane>

      <!-- 2. 收藏题目 -->
      <el-tab-pane label="收藏题目" name="questions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索题目内容..."
          style="width: 300px; margin-bottom: 12px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-table :data="filteredFavoriteQuestions" style="width: 100%" v-loading="loadingQuestions">
          <el-table-column prop="quesContent" label="题目内容" show-overflow-tooltip />
          <el-table-column label="科目" width="120">
            <template #default="scope">
              {{ getSubjectName(scope.row.quesSubId) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" type="primary" @click="openDetail(scope.row, 'fav')">查看</el-button>
              <el-button size="small" type="danger" @click="unfavQues(scope.row.quesId)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 3. 我的错题 -->
      <el-tab-pane label="我的错题" name="errors">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索错题内容..."
          style="width: 300px; margin-bottom: 12px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-table :data="filteredErrorRecords" style="width: 100%" v-loading="loadingErrors">
          <el-table-column prop="quesContent" label="错题题干" show-overflow-tooltip />
          <el-table-column prop="recUserAnswer" label="你的错误答案" width="150" />
          <el-table-column prop="quesAnswer" label="正确答案" width="150" />
          <el-table-column label="做错时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.recTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button size="small" type="primary" @click="openDetail(scope.row, 'error')">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 4. 观看历史 -->
      <el-tab-pane label="观看历史" name="recentVideo">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索视频标题..."
          style="width: 300px; margin-bottom: 12px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div v-loading="loadingRecentVideo" class="video-grid">
          <div v-if="filteredRecentVideos.length === 0" class="empty-state">
            <el-empty description="无观看历史" />
          </div>
          <div v-for="video in filteredRecentVideos" :key="video.vidId" class="video-card" @click="openVideo(video)">
            <div class="video-cover-wrapper">
              <img v-if="video.vidCoverUrl" :src="'http://localhost:8080' + video.vidCoverUrl" class="video-cover" />
              <div v-else class="placeholder-cover">
                <span>{{ video.vidTitle?.charAt(0) }}</span>
              </div>
              <div class="progress-bar">
                <div class="progress-inner" :style="{width: calculateProgress(video)}"></div>
              </div>
              <div class="play-overlay">
                <i class="el-icon-video-play"></i>
              </div>
            </div>
            <div class="video-info">
              <h4 class="video-title">{{ video.vidTitle }}</h4>
              <div class="video-footer">
                <span class="video-time">上次观看到: {{ formatSeconds(video.lastPosition || video.lastposition || video.LASTPOSITION || 0) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 5. 收藏视频 -->
      <el-tab-pane label="收藏视频" name="videos">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索视频标题..."
          style="width: 300px; margin-bottom: 12px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div v-loading="loadingVideos" class="video-grid">
          <div v-if="filteredFavoriteVideos.length === 0" class="empty-state">
            <el-empty description="暂无收藏视频" />
          </div>
          <div v-for="video in filteredFavoriteVideos" :key="video.vidId" class="video-card" @click="openVideo(video)">
            <div class="video-cover-wrapper">
              <img v-if="video.vidCoverUrl" :src="'http://localhost:8080' + video.vidCoverUrl" class="video-cover" />
              <div v-else class="placeholder-cover">
                <span>{{ video.vidTitle?.charAt(0) }}</span>
              </div>
              <div class="play-overlay">
                <i class="el-icon-video-play"></i>
              </div>
            </div>
            <div class="video-info">
              <h4 class="video-title">{{ video.vidTitle }}</h4>
              <div class="video-footer">
                <span class="video-time">{{ formatDate(video.vidCreateTime) }}</span>
                <el-button type="text" class="unfav-btn" @click.stop="unfavVideo(video.vidId)">取消收藏</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 6. 做题历史 -->
      <el-tab-pane label="做题历史" name="history">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索题目内容..."
          style="width: 300px; margin-bottom: 12px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-table :data="filteredHistoryRecords" style="width: 100%" v-loading="loadingHistory">
          <el-table-column prop="quesContent" label="题目内容" show-overflow-tooltip />
          <el-table-column prop="recUserAnswer" label="你的答案" width="100" align="center" />
          <el-table-column prop="quesAnswer" label="正确答案" width="100" align="center" />
          <el-table-column label="结果" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.recIsCorrect === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.recIsCorrect === 1 ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="做题时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.recTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button size="small" type="primary" @click="openDetail(scope.row, 'history')">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 7. 收藏文章 -->
      <el-tab-pane label="收藏文章" name="favoriteArticles">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章标题..."
          style="width: 300px; margin-bottom: 12px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div v-loading="loadingArticles" class="article-grid">
          <div v-if="filteredFavoriteArticles.length === 0" class="empty-state">
            <el-empty description="暂无收藏文章" />
          </div>
          <div
            v-for="article in filteredFavoriteArticles"
            :key="article.articleId"
            class="article-card"
            @click="openArticleDetail(article)"
          >
            <div class="card-content-wrapper">
              <div class="text-zone">
                <h4 class="article-title">{{ article.title }}</h4>
                <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
                <div class="article-meta">
                  <span>✍️ {{ article.authorName }}</span>
                  <span>📅 {{ formatDate(article.createTime) }}</span>
                  <span>👁️ {{ article.viewCount }}</span>
                </div>
              </div>
              <div class="image-zone" v-if="article.coverUrl">
                <img :src="article.coverUrl" alt="cover" class="article-cover" />
              </div>
            </div>
            <el-button
              type="danger"
              size="small"
              class="unfav-btn-article"
              @click.stop="unfavArticle(article.articleId)"
            >取消收藏</el-button
            >
          </div>
        </div>
      </el-tab-pane>

      <!-- 8. 阅读历史 -->
      <el-tab-pane label="阅读历史" name="articleHistory">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章标题..."
          style="width: 300px; margin-bottom: 12px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div v-loading="loadingArticleHistory" class="article-grid">
          <div v-if="filteredArticleHistoryList.length === 0 && !loadingArticleHistory" class="empty-state">
            <el-empty description="暂无阅读记录" />
          </div>
          <div
            v-for="item in filteredArticleHistoryList"
            :key="item.historyId"
            class="article-card"
            @click="openArticleFromHistory(item)"
          >
            <div class="card-content-wrapper">
              <div class="text-zone">
                <h4 class="article-title">{{ item.article?.title || '文章已删除' }}</h4>
                <p class="article-summary">{{ item.article?.summary || '暂无摘要' }}</p>
                <div class="article-meta">
                  <span>🕒 {{ formatDateTime(item.viewTime) }}</span>
                  <span v-if="item.article">👁️ {{ item.article.viewCount }} 浏览</span>
                </div>
              </div>
              <div class="image-zone" v-if="item.article?.coverUrl">
                <img :src="item.article.coverUrl" alt="cover" class="article-cover" />
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

    </el-tabs>

    <!-- 题目详情弹窗 -->
    <el-dialog v-model="showQuestionModal" title="题目详情" width="500px" custom-class="question-dialog" destroy-on-close>
      <div v-if="currentQuestion" class="question-card-detail">
        <div class="q-title">
          <span class="q-tag" v-if="currentQuestion.quesKp">{{ currentQuestion.quesKp }}</span>
          {{ currentQuestion.quesContent }}
        </div>
        <div class="q-options" v-if="currentQuestion.quesOptions">
          <div
            v-for="(opt, index) in formatOptions(currentQuestion.quesOptions)"
            :key="index"
            class="q-option-item"
            :class="{
              'is-correct': getOptionLetter(opt) === currentQuestion.quesAnswer,
              'is-error': currentQuestionType === 'error' && currentQuestion.recUserAnswer && getOptionLetter(opt) === currentQuestion.recUserAnswer
            }"
          >
            {{ opt }}
          </div>
        </div>
        <div class="q-analysis">
          <div class="ans-row correct-ans">
            <strong>正确答案：</strong> {{ currentQuestion.quesAnswer }}
          </div>
          <div class="ans-row user-ans" v-if="currentQuestionType === 'error'">
            <strong>你的错答：</strong> {{ currentQuestion.recUserAnswer }}
          </div>
          <div class="ans-row analysis-info" v-if="currentQuestion.quesAnalysis">
            <div class="analysis-label">题目解析：</div>
            <div class="analysis-text">{{ currentQuestion.quesAnalysis }}</div>
          </div>
          <div class="ans-row analysis-info" v-else>
            <div class="analysis-label">题目解析：</div>
            <div class="analysis-text" style="color: #999; font-style: italic;">暂无解析内容</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showQuestionModal = false" type="primary">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 视频播放模态框 -->
    <VideoPlayer
      :video="currentVideo"
      :visible="showModal"
      :start-position="videoStartPosition"
      @close="closeModal"
      @progress="saveProgressToBackend"
    />

    <!-- 文章阅读弹窗 -->
    <div v-if="showArticleModal" class="modal-overlay-reader" @click="closeArticleModal">
      <div class="reader-container" @click.stop>
        <button class="close-btn" @click="closeArticleModal">✕</button>
        <div v-if="loadingDetail" class="loading-state">📖 加载中...</div>
        <div v-else>
          <h1 class="reader-title">{{ currentArticleDetail?.title }}</h1>
          <div class="reader-meta">
            <span>✍️ {{ currentArticleDetail?.authorName }}</span>
            <span>📅 {{ formatDate(currentArticleDetail?.createTime) }}</span>
            <span>👁️ {{ currentArticleDetail?.viewCount }} 浏览</span>
          </div>
          <div class="article-body" v-html="renderContent(currentArticleDetail)"></div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
  import { ref, computed, onMounted, nextTick } from 'vue';
  import { useRouter } from 'vue-router';
  import axios from 'axios';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import * as echarts from 'echarts';
  import { formatDate, formatDateTime, formatSeconds, formatOptions, getOptionLetter, renderContent, getCurrentUserId } from '../../utils/common.js';
  import VideoPlayer from '../../components/VideoPlayer.vue';

  const router = useRouter();
  const activeTab = ref('statistics');
  const userInfo = ref({});
  const searchKeyword = ref('');

  // 各列表的搜索过滤计算属性
  const filteredFavoriteQuestions = computed(() => {
    if (!searchKeyword.value) return favoriteQuestions.value;
    const kw = searchKeyword.value.toLowerCase();
    return favoriteQuestions.value.filter(q => q.quesContent?.toLowerCase().includes(kw));
  });
  const filteredErrorRecords = computed(() => {
    if (!searchKeyword.value) return errorRecords.value;
    const kw = searchKeyword.value.toLowerCase();
    return errorRecords.value.filter(r => r.quesContent?.toLowerCase().includes(kw));
  });
  const filteredRecentVideos = computed(() => {
    if (!searchKeyword.value) return recentVideos.value;
    const kw = searchKeyword.value.toLowerCase();
    return recentVideos.value.filter(v => v.vidTitle?.toLowerCase().includes(kw));
  });
  const filteredFavoriteVideos = computed(() => {
    if (!searchKeyword.value) return favoriteVideos.value;
    const kw = searchKeyword.value.toLowerCase();
    return favoriteVideos.value.filter(v => v.vidTitle?.toLowerCase().includes(kw));
  });
  const filteredHistoryRecords = computed(() => {
    if (!searchKeyword.value) return historyRecords.value;
    const kw = searchKeyword.value.toLowerCase();
    return historyRecords.value.filter(r => r.quesContent?.toLowerCase().includes(kw));
  });
  const filteredFavoriteArticles = computed(() => {
    if (!searchKeyword.value) return favoriteArticles.value;
    const kw = searchKeyword.value.toLowerCase();
    return favoriteArticles.value.filter(a => a.title?.toLowerCase().includes(kw));
  });
  const filteredArticleHistoryList = computed(() => {
    if (!searchKeyword.value) return articleHistoryList.value;
    const kw = searchKeyword.value.toLowerCase();
    return articleHistoryList.value.filter(h => h.article?.title?.toLowerCase().includes(kw));
  });

  // 图表相关
  const radarChartRef = ref(null);
  const lineChartRef = ref(null);
  const heatmapChartRef = ref(null);
  const coverageChartRef = ref(null);
  let radarChart = null;
  let lineChart = null;
  let heatmapChart = null;
  let coverageChart = null;
  const loadingStats = ref(false);
  const rawKnowledgeData = ref([]);
  const selectedSubjectForChart = ref(null);

  // 题目相关
  const favoriteQuestions = ref([]);
  const subjects = ref([]);
  const loadingQuestions = ref(false);
  const errorRecords = ref([]);
  const loadingErrors = ref(false);
  const historyRecords = ref([]);
  const loadingHistory = ref(false);

  // 视频相关
  const favoriteVideos = ref([]);
  const loadingVideos = ref(false);
  const recentVideos = ref([]);
  const loadingRecentVideo = ref(false);
  const showModal = ref(false);
  const currentVideo = ref(null);
  const videoStartPosition = ref(0);

  // 题目详情弹窗
  const showQuestionModal = ref(false);
  const currentQuestion = ref(null);
  const currentQuestionType = ref('fav');

  // 文章收藏相关
  const favoriteArticles = ref([]);
  const loadingArticles = ref(false);
  const showArticleModal = ref(false);
  const currentArticleDetail = ref(null);
  const loadingDetail = ref(false);

  // 辅助方法
  const calculateProgress = (video) => {
    const lastPos = video.lastPosition || 0;
    const duration = video.vidDuration || 1;
    return Math.min((lastPos / duration) * 100, 100) + '%';
  };
  const getSubjectName = (subId) => subjects.value.find(s => s.subId === subId)?.subName || '未知';

  // 标签页切换
  const handleTabClick = async (tab) => {
    searchKeyword.value = '';
    const name = tab.paneName;
    if (name === 'videos' && favoriteVideos.value.length === 0) loadFavoriteVideos();
    else if (name === 'questions' && favoriteQuestions.value.length === 0) loadFavoriteQuestions();
    else if (name === 'errors' && errorRecords.value.length === 0) loadErrorRecords();
    else if (name === 'recentVideo' && recentVideos.value.length === 0) loadRecentVideos();
    else if (name === 'history' && historyRecords.value.length === 0) loadHistoryRecords();
      else if (name === 'articleHistory' && articleHistoryList.value.length === 0) {
    loadArticleHistory();
  }
    else if (name === 'statistics') {
      await nextTick();
      if (!radarChart) loadStatisticsData();
    } else if (name === 'favoriteArticles' && favoriteArticles.value.length === 0) {
      loadFavoriteArticles();
    }
  };

  const handleBack = () => router.push('/main');
  const handleLogout = () => {
    localStorage.removeItem('currentUser');
    router.push('/login');
    ElMessage.success('已退出登录');
  };

  // 加载科目列表
  const loadSubjects = async () => {
    const res = await axios.get('http://localhost:8080/subjects');
    subjects.value = res.data;
  };

  // ===================== 数据统计（保持原有实现） =====================
  const loadStatisticsData = async () => {
    loadingStats.value = true;
    await nextTick();
    if (!radarChart) radarChart = echarts.init(radarChartRef.value);
    if (!lineChart) lineChart = echarts.init(lineChartRef.value);
    if (!heatmapChart) heatmapChart = echarts.init(heatmapChartRef.value);
    if (!coverageChart) coverageChart = echarts.init(coverageChartRef.value);

    try {
      const userId = userInfo.value.userId;
      const [radarRes, lineRes, yearlyRes, coverageRes] = await Promise.all([
        axios.get(`http://localhost:8080/records/stats/knowledge?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/daily?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/yearly?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/coverage?userId=${userId}`)
      ]);

      rawKnowledgeData.value = radarRes.data;
      if (rawKnowledgeData.value.length > 0) {
        selectedSubjectForChart.value = rawKnowledgeData.value[0].subId;
        updateRadarChart();
      }

      const dailyData = lineRes.data;
      lineChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['做题数量', '正确率(%)'] },
        xAxis: { type: 'category', data: dailyData.map(d => d.dateStr) },
        yAxis: [
          { type: 'value', name: '做题数量', minInterval: 1 },
          { type: 'value', name: '正确率', max: 100, axisLabel: { formatter: '{value} %' } }
        ],
        series: [
          { name: '做题数量', type: 'bar', data: dailyData.map(d => d.totalCount), itemStyle: { color: '#E6A23C' } },
          { name: '正确率(%)', type: 'line', yAxisIndex: 1, data: dailyData.map(d => d.correctRate), smooth: true, itemStyle: { color: '#67C23A' } }
        ]
      });

      const heatmapData = yearlyRes.data.map(d => [d.dateStr, d.totalCount]);
      const maxCount = heatmapData.length > 0 ? Math.max(...heatmapData.map(d => d[1])) : 20;
      heatmapChart.setOption({
        tooltip: { formatter: (p) => `${p.data[0]} : ${p.data[1]} 题` },
        visualMap: {
          min: 0, max: maxCount, calculable: true, orient: 'horizontal', left: 'center', bottom: '5%',
          text: [`上限 (≤ ${Math.ceil(maxCount / 10) * 10 || 20})`, `下限 (≥ 0)`],
          textStyle: { color: '#666', fontSize: 12 },
          inRange: { color: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127'] }
        },
        calendar: {
          top: 30, left: 30, right: 30, cellSize: ['auto', 18], range: new Date().getFullYear(),
          itemStyle: { borderWidth: 0.5, borderColor: '#fff' },
          yearLabel: { show: false }, dayLabel: { nameMap: 'ZH' }, monthLabel: { nameMap: 'ZH' }
        },
        series: [{ type: 'heatmap', coordinateSystem: 'calendar', data: heatmapData }]
      });

      const cvgData = coverageRes.data;
      coverageChart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: (params) => {
            let practiced = params[0].value;
            let unpracticed = params[1].value;
            let total = practiced + unpracticed;
            let rate = total === 0 ? 0 : ((practiced / total) * 100).toFixed(1);
            return `${params[0].name}<br/>已练习: ${practiced}<br/>未练习: ${unpracticed}<br/>覆盖率: ${rate}%`;
          }
        },
        legend: { data: ['已练知识点', '未练知识点'] },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: cvgData.map(d => d.subName) },
        series: [
          { name: '已练知识点', type: 'bar', stack: 'total', data: cvgData.map(d => d.practicedKp), itemStyle: { color: '#409EFF' } },
          { name: '未练知识点', type: 'bar', stack: 'total', data: cvgData.map(d => (d.totalKp - d.practicedKp)), itemStyle: { color: '#E4E7ED' } }
        ]
      });
    } catch (error) {
      console.error("加载统计数据失败", error);
      ElMessage.error("获取统计数据失败");
    } finally {
      loadingStats.value = false;
    }
  };

  const updateRadarChart = () => {
    if (!radarChart || !selectedSubjectForChart.value) return;
    const currentSubjectData = rawKnowledgeData.value.filter(item => item.subId == selectedSubjectForChart.value);
    let indicator = [], values = [];
    if (currentSubjectData.length > 0) {
      currentSubjectData.forEach(item => {
        indicator.push({ name: item.kpName || '未知', max: 100 });
        values.push(item.correctRate || 0);
      });
    } else {
      indicator = [{ name: '暂无数据', max: 100 }];
      values = [0];
    }
    radarChart.setOption({
      tooltip: { trigger: 'item' },
      radar: {
        indicator: indicator,
        radius: '60%',
        axisName: { color: '#333', formatter: (v) => v.length > 6 ? v.slice(0, 6) + '...' : v }
      },
      series: [{
        type: 'radar',
        areaStyle: { color: 'rgba(64, 158, 255, 0.3)' },
        data: [{ value: values, name: '掌握度(%)' }]
      }]
    });
  };

  const handleResize = () => {
    [radarChart, lineChart, heatmapChart, coverageChart].forEach(chart => chart?.resize());
  };

  // ===================== 题目数据加载（保留原实现） =====================
  const loadFavoriteQuestions = async () => {
    loadingQuestions.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/favoriteQuestions/details?userId=${userInfo.value.userId}`);
      favoriteQuestions.value = res.data;
    } finally { loadingQuestions.value = false; }
  };
  const unfavQues = async (quesId) => {
    const res = await axios.post('http://localhost:8080/favoriteQuestions/remove', {
      favUserId: userInfo.value.userId,
      favQuesId: quesId
    });
    if (res.data === 'success') {
      ElMessage.success('取消收藏成功');
      favoriteQuestions.value = favoriteQuestions.value.filter(q => q.quesId !== quesId);
    }
  };
  const loadErrorRecords = async () => {
    loadingErrors.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/records/errors?userId=${userInfo.value.userId}`);
      errorRecords.value = res.data;
    } finally { loadingErrors.value = false; }
  };
  const loadHistoryRecords = async () => {
    loadingHistory.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/records/history?userId=${userInfo.value.userId}`);
      historyRecords.value = res.data;
    } finally { loadingHistory.value = false; }
  };

  // ===================== 视频数据加载 =====================
  const loadRecentVideos = async () => {
    loadingRecentVideo.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/videoProgress/recent?userId=${userInfo.value.userId}`);
      recentVideos.value = res.data;
    } finally { loadingRecentVideo.value = false; }
  };
  const loadFavoriteVideos = async () => {
    loadingVideos.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/favoriteVideos/details?userId=${userInfo.value.userId}`);
      favoriteVideos.value = res.data;
    } finally { loadingVideos.value = false; }
  };
  const unfavVideo = async (vidId) => {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', { type: 'warning' });
    const res = await axios.post(`http://localhost:8080/favoriteVideos/remove`, {
      favUserId: userInfo.value.userId,
      favVidsId: vidId
    });
    if (res.data === 'success') {
      ElMessage.success('已取消收藏');
      favoriteVideos.value = favoriteVideos.value.filter(v => v.vidId !== vidId);
    }
  };
  const openVideo = (video) => {
    currentVideo.value = video;
    videoStartPosition.value = video.lastPosition || 0;
    showModal.value = true;
  };
  const closeModal = () => {
    showModal.value = false;
    currentVideo.value = null;
  };
  const saveProgressToBackend = async ({ vidId, position }) => {
    if (!userInfo.value.userId || position === 0) return;
    try {
      await axios.post('http://localhost:8080/videoProgress/save', {
        progUserId: userInfo.value.userId,
        progVidId: vidId,
        progLastPosition: position,
        progIsFinished: 0
      });
      loadRecentVideos();
    } catch (e) { console.error("保存进度失败", e); }
  };

  // 题目详情弹窗
  const openDetail = (row, type = 'fav') => {
    currentQuestion.value = row;
    currentQuestionType.value = type;
    showQuestionModal.value = true;
  };

  // ===================== 文章收藏 =====================
  const loadFavoriteArticles = async () => {
    loadingArticles.value = true;
    try {
      const userId = userInfo.value.userId;
      const idsRes = await axios.get(`http://localhost:8080/api/favoriteArticles/ids?userId=${userId}`);
      const ids = idsRes.data;
      if (!ids || ids.length === 0) {
        favoriteArticles.value = [];
        return;
      }
      const requests = ids.map(id => axios.get(`http://localhost:8080/api/articles/${id}`));
      const responses = await Promise.all(requests);
      favoriteArticles.value = responses.map(res => res.data);
    } catch (error) {
      ElMessage.error('加载收藏文章失败');
    } finally {
      loadingArticles.value = false;
    }
  };
  const unfavArticle = async (articleId) => {
    try {
      await axios.post('http://localhost:8080/api/favoriteArticles/remove', {
        favUserId: userInfo.value.userId,
        favArticleId: articleId
      });
      ElMessage.success('已取消收藏');
      favoriteArticles.value = favoriteArticles.value.filter(a => a.articleId !== articleId);
    } catch (e) {
      ElMessage.error('操作失败');
    }
  };
  const openArticleDetail = async (article) => {
    showArticleModal.value = true;
    loadingDetail.value = true;
    document.body.style.overflow = 'hidden';
    try {
      const res = await axios.get(`http://localhost:8080/api/articles/${article.articleId}`);
      currentArticleDetail.value = res.data;
    } catch (e) {
      ElMessage.error('获取正文失败');
    } finally {
      loadingDetail.value = false;
    }
  };
  const closeArticleModal = () => {
    showArticleModal.value = false;
    currentArticleDetail.value = null;
    document.body.style.overflow = '';
  };

    // 阅读历史相关
  const articleHistoryList = ref([]);
  const loadingArticleHistory = ref(false);

  // 根据历史记录打开文章（直接复用已有的文章弹窗）
  const openArticleFromHistory = (historyItem) => {
    // 构造一个类似 article 的对象，因为 openArticleDetail 内部会再次请求详情
    openArticleDetail({ articleId: historyItem.articleId });
  };
  const loadArticleHistory = async () => {
  loadingArticleHistory.value = true;
  try {
    const userId = userInfo.value.userId;
    // 1. 先获取分页历史记录（可扩展分页，这里先拿第一页的所有数据）
    const res = await axios.get('http://localhost:8080/api/history/userPage', {
      params: {
        userId: userId,
        current: 1,
        size: 50       // 暂时多拿一点，实际可改为分页组件
      }
    });
    const historyRecords = res.data.records || [];
    if (historyRecords.length === 0) {
      articleHistoryList.value = [];
      return;
    }

    // 2. 提取所有 articleId 并去重
    const articleIds = [...new Set(historyRecords.map(r => r.articleId))];

    // 3. 批量请求文章详情（并发请求，注意后端性能）
    const articleRequests = articleIds.map(id =>
      axios.get(`http://localhost:8080/api/articles/${id}`)
        .then(resp => ({ id, data: resp.data }))
        .catch(() => ({ id, data: null }))   // 文章已删除等情况
    );
    const articleMap = {};
    const results = await Promise.all(articleRequests);
    results.forEach(item => { articleMap[item.id] = item.data; });

    // 4. 组装数据：将文章详情挂到每条历史记录上
    articleHistoryList.value = historyRecords.map(record => ({
      ...record,
      article: articleMap[record.articleId] || null
    }));
  } catch (error) {
    console.error('加载阅读历史失败', error);
    ElMessage.error('获取阅读历史失败');
  } finally {
    loadingArticleHistory.value = false;
  }
};

  onMounted(async () => {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      userInfo.value = JSON.parse(savedUser);
      await loadSubjects();
      loadStatisticsData();
      window.addEventListener('resize', handleResize);
    } else {
      router.push('/login');
    }
  });
</script>

<style scoped>
  .user-center {
    padding: 20px;
    max-width: 1000px;
    margin: 0 auto;
  }

  .user-info-card {
    margin-bottom: 20px;
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  }

  .user-profile {
    display: flex !important;
    flex-direction: row !important;
    align-items: center;
    gap: 24px;
    padding: 10px;
  }

  .user-profile :deep(.el-avatar) {
    background-color: #fb7299 !important;
    color: #ffffff;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(251, 114, 153, 0.2);
  }

  .info-text {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .info-text h2 {
    margin: 0 0 4px 0;
    font-size: 22px;
    color: #18191c;
  }

  .user-bio {
    color: #9499a0;
    font-size: 14px;
    margin: 0;
  }

  .logout-btn-in-card {
    margin-left: auto;
    padding: 10px 20px;
  }

  /* ---------- 视频卡片栅格 ---------- */
  .video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
  }

  .video-card {
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: box-shadow 0.2s, transform 0.2s;
  }

  .video-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }

  .video-cover-wrapper {
    position: relative;
    width: 100%;
    padding-top: 56.25%;
    background: #f5f5f5;
  }

  .video-cover {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .placeholder-cover {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #ececec;
    font-size: 32px;
    color: #999;
  }

  .progress-bar {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 3px;
    background: rgba(255, 255, 255, 0.6);
  }

  .progress-inner {
    height: 100%;
    background: #fb7299;
  }

  .play-overlay {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 32px;
    color: rgba(255, 255, 255, 0.85);
  }

  .video-info {
    padding: 10px 12px;
  }

  .video-title {
    margin: 0 0 6px;
    font-size: 14px;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .video-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #999;
  }

  .unfav-btn {
    padding: 0;
  }

  /* ---------- 文章卡片 ---------- */
  .article-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .unfav-btn-article {
    position: absolute;
    top: 10px;
    right: 10px;
  }

  /* ---------- 文章阅读弹窗 ---------- */
  .modal-overlay-reader {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(246, 246, 246, 0.95);
    display: flex;
    justify-content: center;
    align-items: flex-start;
    z-index: 2000;
    overflow-y: auto;
  }

  .reader-container {
    width: 100%;
    max-width: 800px;
    background: #fff;
    min-height: 100vh;
    padding: 60px 50px;
    box-shadow: 0 0 20px rgba(0,0,0,0.05);
    position: relative;
    box-sizing: border-box;
  }

  .reader-title {
    font-size: 28px;
    font-weight: bold;
    color: #121212;
    line-height: 1.4;
    margin-bottom: 20px;
  }

  .reader-meta {
    font-size: 14px;
    color: #8590a6;
    margin-bottom: 40px;
    display: flex;
    gap: 20px;
    border-bottom: 1px solid #f0f1f2;
    padding-bottom: 20px;
  }

  /* 题目详情卡片样式 */
  .question-card-detail {
    padding: 10px 5px;
  }

  .q-title {
    font-size: 17px;
    font-weight: 600;
    margin-bottom: 24px;
    line-height: 1.6;
    color: #1d1d1f;
  }

  .q-tag {
    display: inline-block;
    background: #f0f5ff;
    color: #2f54eb;
    border: 1px solid #adc6ff;
    padding: 2px 10px;
    border-radius: 6px;
    font-size: 12px;
    margin-right: 10px;
    font-weight: 500;
  }

  .q-options {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-bottom: 28px;
  }

  .q-option-item {
    padding: 14px 18px;
    border: 1px solid #f0f0f0;
    border-radius: 10px;
    background: #ffffff;
    font-size: 14px;
    color: #444;
    transition: all 0.25s;
  }

  .q-option-item.is-correct {
    background: #f6ffed;
    border-color: #b7eb8f;
    color: #52c41a;
    font-weight: 600;
  }

  .q-option-item.is-error {
    background: #fff1f0;
    border-color: #ffa39e;
    color: #f5222d;
    font-weight: 600;
  }

  .q-analysis {
    margin-top: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 12px;
  }

  .ans-row {
    margin-bottom: 12px;
    font-size: 14px;
    display: flex;
    align-items: baseline;
  }

  .analysis-label {
    font-weight: 700;
    color: #3478e5;
    margin-right: 8px;
    flex-shrink: 0;
  }

  .analysis-text {
    line-height: 1.7;
    color: #555;
  }

  .correct-ans {
    color: #52c41a;
    font-weight: 600;
  }

  .user-ans {
    color: #f5222d;
    font-weight: 600;
  }
</style>
