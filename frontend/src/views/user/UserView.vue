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
        <el-table :data="favoriteQuestions" style="width: 100%" v-loading="loadingQuestions">
          <el-table-column prop="quesContent" label="题目内容" show-overflow-tooltip />
          <el-table-column label="科目" width="120">
            <template #default="scope">
              {{ getSubjectName(scope.row.quesSubId) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <!-- 修改：传入 scope.row 以及来源类型 'fav' -->
              <el-button size="small" type="primary" @click="openDetail(scope.row, 'fav')">查看</el-button>
              <el-button size="small" type="danger" @click="unfavQues(scope.row.quesId)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 3. 我的错题 -->
      <el-tab-pane label="我的错题" name="errors">
        <el-table :data="errorRecords" style="width: 100%" v-loading="loadingErrors">
          <el-table-column prop="quesContent" label="错题干" show-overflow-tooltip />
          <el-table-column prop="recUserAnswer" label="你的错误答案" width="150" />
          <el-table-column prop="quesAnswer" label="正确答案" width="150" />
          <el-table-column label="做错时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.recTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <!-- 修改：传入完整的 scope.row 以及来源类型 'error' -->
              <el-button size="small" type="primary" @click="openDetail(scope.row, 'error')">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 4. 观看历史 -->
      <el-tab-pane label="观看历史" name="recentVideo">
        <div v-loading="loadingRecentVideo" class="video-grid">
          <div v-if="recentVideos.length === 0" class="empty-state">
            <el-empty description="无观看历史" />
          </div>
          <div v-for="video in recentVideos" :key="video.vidId" class="video-card" @click="openVideo(video)">
            <div class="video-cover-wrapper">
              <img v-if="video.vidCoverUrl" :src="'http://localhost:8080' + video.vidCoverUrl" class="video-cover" />
              <div v-else class="placeholder-cover">
                <span>{{ video.vidTitle?.charAt(0) }}</span>
              </div>
              <!-- 动态计算百分比的进度条 -->
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
        <div v-loading="loadingVideos" class="video-grid">
          <div v-if="favoriteVideos.length === 0" class="empty-state">
            <el-empty description="暂无收藏视频" />
          </div>
          <div v-for="video in favoriteVideos" :key="video.vidId" class="video-card" @click="openVideo(video)">
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
        <el-table :data="historyRecords" style="width: 100%" v-loading="loadingHistory">
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

    </el-tabs>

    <!-- 题目详情卡片弹窗 -->
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

    <!-- 视频播放模态框 (增强版：支持续播) -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>{{ currentVideo?.vidTitle }}</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="video-wrapper">
            <!-- 直接的文件视频：原生的精准续播 -->
            <video
              v-if="isMp4(currentVideo?.vidUrl)"
              ref="videoPlayerRef"
              :src="currentVideo.vidUrl"
              controls
              @loadedmetadata="onVideoLoaded"
            ></video>
            <!-- 外链视频：现在使用固定链接，不再每秒刷新 -->
            <iframe
              v-else-if="currentVideo?.vidUrl"
              :src="currentIframeUrl"
              frameborder="0"
              allowfullscreen
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            ></iframe>
            <div v-else class="no-video">⚠️ 视频链接暂不可用</div>
          </div>
          <p class="video-description">{{ currentVideo?.vidDescription }}</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
  import { ref, onMounted, nextTick } from 'vue';
  import { useRouter } from 'vue-router';
  import axios from 'axios';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import * as echarts from 'echarts';

  const router = useRouter();

  // === 状态定义 ===
  const activeTab = ref('statistics');
  const userInfo = ref({});

  const favoriteQuestions = ref([]);
  const subjects = ref([]);
  const loadingQuestions = ref(false);

  const favoriteVideos = ref([]);
  const loadingVideos = ref(false);

  const errorRecords = ref([]);
  const loadingErrors = ref(false);

  const recentVideos = ref([]);
  const loadingRecentVideo = ref(false);

  const historyRecords = ref([]);
  const loadingHistory = ref(false);

  // === 图表状态与引用 ===
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

  // === 生命周期 ===
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

  // === 标签页切换 ===
  const handleTabClick = async (tab) => {
    const name = tab.paneName;
    if (name === 'videos' && favoriteVideos.value.length === 0) loadFavoriteVideos();
    else if (name === 'questions' && favoriteQuestions.value.length === 0) loadFavoriteQuestions();
    else if (name === 'errors' && errorRecords.value.length === 0) loadErrorRecords();
    else if (name === 'recentVideo' && recentVideos.value.length === 0) loadRecentVideos();
    else if (name === 'history' && historyRecords.value.length === 0) loadHistoryRecords();
    else if (name === 'statistics') {
      await nextTick();
      if (!radarChart) loadStatisticsData();
    }
  };

  const handleBack = () => router.push('/main');

  // === 【核心】数据统计加载逻辑 ===
  const loadStatisticsData = async () => {
    loadingStats.value = true;
    await nextTick();

    // 1. 初始化所有图表实例
    if (!radarChart) radarChart = echarts.init(radarChartRef.value);
    if (!lineChart) lineChart = echarts.init(lineChartRef.value);
    if (!heatmapChart) heatmapChart = echarts.init(heatmapChartRef.value);
    if (!coverageChart) coverageChart = echarts.init(coverageChartRef.value);

    try {
      const userId = userInfo.value.userId;

      // 并行请求四个统计接口
      const [radarRes, lineRes, yearlyRes, coverageRes] = await Promise.all([
        axios.get(`http://localhost:8080/records/stats/knowledge?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/daily?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/yearly?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/coverage?userId=${userId}`)
      ]);

      // A. 雷达图：知识点掌握情况
      rawKnowledgeData.value = radarRes.data;
      if (rawKnowledgeData.value.length > 0) {
        selectedSubjectForChart.value = rawKnowledgeData.value[0].subId;
        updateRadarChart();
      }

      // B. 折线+柱形图：近7日统计
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

      // C. 热力图：全年活跃度
      const heatmapData = yearlyRes.data.map(d => [d.dateStr, d.totalCount]);
      const maxCount = heatmapData.length > 0 ? Math.max(...heatmapData.map(d => d[1])) : 20;
      const visualMax = Math.ceil(maxCount / 10) * 10 || 20;
      heatmapChart.setOption({
        tooltip: {
          formatter: (p) => `${p.data[0]} : ${p.data[1]} 题`
        },
        visualMap: {
          min: 0, max:maxCount , calculable: true, orient: 'horizontal', left: 'center', bottom: '5%',
          text: [`上限 (≤ ${visualMax})`, `下限 (≥ 0)`],
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

      // D. 堆叠柱图：知识点覆盖率
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

  // === 视频播放逻辑 (续播功能) ===
  const showModal = ref(false);
  const currentVideo = ref(null);
  const currentIframeUrl = ref('');
  const currentPosition = ref(0);
  let watchTimer = null;
  const videoPlayerRef = ref(null);

  const isMp4 = (url) => url && (url.toLowerCase().endsWith('.mp4') || url.toLowerCase().endsWith('.webm'));
  const calculateProgress = (video) => {
    const lastPos = video.lastPosition || 0;
    const duration = video.vidDuration || 1;
    return Math.min((lastPos / duration) * 100, 100) + '%';
  };

  const openVideo = (video) => {
    currentVideo.value = video;
    showModal.value = true;
    document.body.style.overflow = 'hidden';
    const lastPos = video.lastPosition || 0;
    currentPosition.value = lastPos;

    if (!isMp4(video.vidUrl)) {
      const sep = video.vidUrl.includes('?') ? '&' : '?';
      currentIframeUrl.value = `${video.vidUrl}${sep}t=${lastPos}`;
    }
    startWatchTimer();
  };

  const onVideoLoaded = () => {
    if (videoPlayerRef.value && currentPosition.value > 0) {
      videoPlayerRef.value.currentTime = currentPosition.value;
    }
  };

  const closeModal = () => {
    saveProgressToBackend();
    stopWatchTimer();
    showModal.value = false;
    currentVideo.value = null;
    document.body.style.overflow = '';
  };

  const startWatchTimer = () => {
    if (watchTimer) clearInterval(watchTimer);
    watchTimer = setInterval(() => { currentPosition.value += 1; }, 1000);
  };

  const stopWatchTimer = () => { if (watchTimer) clearInterval(watchTimer); };

  const saveProgressToBackend = async () => {
    if (!userInfo.value.userId || !currentVideo.value) return;
    try {
      await axios.post('http://localhost:8080/videoProgress/save', {
        progUserId: userInfo.value.userId,
        progVidId: currentVideo.value.vidId,
        progLastPosition: currentPosition.value,
        progIsFinished: 0
      });
      loadRecentVideos();
    } catch (e) { console.error("保存进度失败", e); }
  };

  // === 题目详情逻辑 ===
  const showQuestionModal = ref(false);
  const currentQuestion = ref(null);
  const currentQuestionType = ref('fav');

  const formatOptions = (str) => {
    if (!str) return [];
    const regex = /[A-D][\.．、\s][\s\S]*?(?=[A-D][\.．、\s]|$)/g;
    return (str.match(regex) || []).map(o => o.trim());
  };

  const getOptionLetter = (opt) => opt.trim().charAt(0).toUpperCase();

  const openDetail = (row, type = 'fav') => {
    currentQuestion.value = row;
    currentQuestionType.value = type;
    showQuestionModal.value = true;
  };

  // === 数据加载接口 ===
  const loadSubjects = async () => {
    const res = await axios.get('http://localhost:8080/subjects');
    subjects.value = res.data;
  };

  const getSubjectName = (subId) => subjects.value.find(s => s.subId === subId)?.subName || '未知';

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

  //做题历史
  const loadHistoryRecords = async () => {
    loadingHistory.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/records/history?userId=${userInfo.value.userId}`);
      historyRecords.value = res.data;
    } catch (error) {
      console.error("加载做题历史失败", error);
      ElMessage.error("获取做题历史失败");
    } finally {
      loadingHistory.value = false;
    }
  };

  // === 格式化工具 ===
  const formatDate = (s) => s ? new Date(s).toLocaleDateString() : '';
  const formatDateTime = (s) => {
    if (!s) return '';
    const d = new Date(s);
    return `${d.toLocaleDateString()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
  };
  const formatSeconds = (sec) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  };
</script>

<style scoped>
  /* 个人中心主容器 */
  .user-center {
    padding: 20px;
    max-width: 1000px;
    margin: 0 auto;
  }

  /* 用户信息卡片：确保内部是横向排列 */
  .user-info-card {
    margin-bottom: 20px;
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); /* 增加一点柔和阴影 */
  }

  /* 核心修复：确保头像、文字、按钮水平对齐 */
  .user-profile {
    display: flex !important; /* 强制开启 flex */
    flex-direction: row !important; /* 强制横向排列 */
    align-items: center; /* 垂直居中 */
    gap: 24px; /* 元素之间的间距 */
    padding: 10px;
  }

  /* 头像圆圈变粉色 */
  .user-profile :deep(.el-avatar) {
    background-color: #fb7299 !important;
    color: #ffffff;
    flex-shrink: 0; /* 防止头像被挤压变扁 */
    box-shadow: 0 2px 8px rgba(251, 114, 153, 0.2);
  }

  /* 文字信息区域 */
  .info-text {
    flex: 1; /* 占据中间剩余空间 */
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

  /* 退出登录按钮：靠右显示 */
  .logout-btn-in-card {
    margin-left: auto; /* 将按钮推向最右侧 */
    padding: 10px 20px;
  }

/* --- 题目详情弹窗美化版样式 --- */
  :deep(.question-dialog) {
    border-radius: 16px;
    overflow: hidden;
  }

  .question-card-detail {
    padding: 10px 5px;
  }

  /* 题干：加深颜色，提高行高 */
  .q-title {
    font-size: 17px;
    font-weight: 600;
    margin-bottom: 24px;
    line-height: 1.6;
    color: #1d1d1f;
  }

  /* 知识点标签：精致的小圆角边框 */
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
    vertical-align: middle;
  }

  /* 选项列表容器 */
  .q-options {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-bottom: 28px;
  }

  /* 单个选项：增加阴影和过渡动画 */
  .q-option-item {
    padding: 14px 18px;
    border: 1px solid #f0f0f0;
    border-radius: 10px;
    background: #ffffff;
    font-size: 14px;
    color: #444;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 6px rgba(0,0,0,0.02);
  }

  /* 正确选项样式：柔和的绿色 */
  .q-option-item.is-correct {
    background: #f6ffed;
    border-color: #b7eb8f;
    color: #52c41a;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(82, 196, 26, 0.1);
  }

  /* 错误选项样式：柔和的红色 */
  .q-option-item.is-error {
    background: #fff1f0;
    border-color: #ffa39e;
    color: #f5222d;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(245, 34, 45, 0.1);
  }

  /* 解析区域：灰底卡片化处理 */
  .q-analysis {
    margin-top: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 12px;
    border: 1px solid #edf2f7;
  }

  .ans-row {
    margin-bottom: 12px;
    font-size: 14px;
    display: flex;
    align-items: baseline;
  }

  .ans-row:last-child {
    margin-bottom: 0;
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
    white-space: pre-wrap;
  }

  .correct-ans {
    color: #52c41a;
    font-weight: 600;
  }

  .user-ans {
    color: #f5222d;
    font-weight: 600;
  }

  /* 底部按钮居中且加宽 */
  :deep(.el-dialog__footer) {
    text-align: center;
    padding-bottom: 24px;
  }

  :deep(.el-dialog__footer .el-button) {
    padding: 10px 40px;
    border-radius: 8px;
  }
</style>
