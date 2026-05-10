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
            <div ref="radarChartRef" class="chart-box"></div>
          </el-card>

          <el-card class="chart-card" shadow="hover" v-loading="loadingStats">
            <template #header>
              <div class="card-header">
                <span>近7日做题统计</span>
              </div>
            </template>
            <div ref="lineChartRef" class="chart-box"></div>
          </el-card>
        </div>

        <el-card class="chart-card" shadow="hover" v-loading="loadingStats" style="width: 100%; margin-top: 20px;">
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
  .user-center { padding: 20px; max-width: 1000px; margin: 0 auto; }
  .user-info-card { margin-bottom: 20px; border-radius: 12px; }

  .user-profile {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  .info-text h2 { margin: 0 0 8px 0; }
  .user-bio { color: #666; font-size: 14px; }

  /* 图表布局 */
  .statistics-container {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    padding: 10px 0;
  }
  .chart-card {
    flex: 1;
    min-width: 400px;
    border-radius: 8px;
  }
  .chart-box {
    width: 100%;
    height: 350px;
  }

  /* 视频网格布局 */
  .video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
    padding: 20px 0;
  }
  .video-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
    transition: transform 0.3s;
    cursor: pointer;
  }
  .video-card:hover {
    transform: translateY(-5px);
  }
  .video-cover-wrapper {
    position: relative;
    height: 140px;
    background: #f0f0f0;
  }
  .video-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .placeholder-cover {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    color: #999;
    background: #e4e7ed;
  }

  /* 进度条样式 */
  .progress-bar {
      position: absolute;
      bottom: 0; left: 0; width: 100%; height: 4px;
      background: rgba(255,255,255,0.3);
  }
  .progress-inner {
      height: 100%;
      background: #f56c6c;
  }

  .play-overlay {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
  }
  .video-cover-wrapper:hover .play-overlay { opacity: 1; }
  .play-overlay i { font-size: 40px; color: #fff; }
  .video-info { padding: 12px; }
  .video-title {
    margin: 0 0 10px 0;
    font-size: 16px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .video-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .video-time { font-size: 12px; color: #999; }
  .unfav-btn { color: #f56c6c; }
  .unfav-btn:hover { color: #ff4949; }
  .empty-state { grid-column: 1 / -1; }

  /* 模态框样式 */
  .modal-overlay {
    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
    background-color: rgba(0, 0, 0, 0.7);
    display: flex; align-items: center; justify-content: center;
    z-index: 2000; backdrop-filter: blur(4px);
  }
  .modal-container {
    background: white; border-radius: 16px; width: 90%; max-width: 880px;
    max-height: 85vh; display: flex; flex-direction: column;
    box-shadow: 0 20px 35px rgba(0, 0, 0, 0.2);
  }
  .modal-header {
    display: flex; justify-content: space-between; align-items: center;
    padding: 16px 20px; border-bottom: 1px solid #e9ecef;
  }
  .modal-header h3 { margin: 0; font-size: 18px; color: #18191c; }
  .close-btn {
    background: none; border: none; font-size: 24px; cursor: pointer; color: #9499a0;
  }
  .close-btn:hover { color: #fb7299; }
  .modal-body { flex: 1; overflow-y: auto; padding: 20px; }
  .video-wrapper {
    position: relative; padding-bottom: 56.25%; height: 0; background: #000;
    border-radius: 12px; overflow: hidden;
  }
  .video-wrapper iframe, .video-wrapper video {
    position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;
  }
  .video-description { margin-top: 16px; font-size: 14px; color: #4e555e; line-height: 1.5; }
  .no-video { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #ccc; }

  /* === 题目详情弹窗卡片样式 === */
  .question-card-detail {
    padding: 10px 5px;
  }

  .q-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 20px;
    line-height: 1.6;
    color: #18191c;
  }

  .q-tag {
    display: inline-block;
    background: #eef4ff;
    color: #3478e5;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    margin-right: 8px;
    font-weight: normal;
    vertical-align: text-bottom;
  }

  .q-options {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;
  }

  .q-option-item {
    padding: 12px 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background: #fafafa;
    font-size: 14px;
    color: #606266;
    transition: all 0.2s;
  }

  /* 自动高亮正确答案 */
  .q-option-item.is-correct {
    background: #f0f9eb;
    border-color: #67c23a;
    color: #67c23a;
    font-weight: bold;
  }

  /* 如果是错题，自动标红用户选错的答案 */
  .q-option-item.is-error {
    background: #fef0f0;
    border-color: #f56c6c;
    color: #f56c6c;
  }
  .q-analysis {
    margin-top: 15px;
    padding-top: 18px;
    border-top: 1px dashed #ebeef5;
  }
  .ans-row {
    margin-bottom: 8px;
    font-size: 14px;
  }
  .correct-ans { color: #67c23a; }
  .user-ans { color: #f56c6c; }

  /*  题目详情弹窗  */
  .q-analysis {
    margin-top: 15px;
    padding-top: 18px;
    border-top: 1px dashed #ebeef5;
  }

  .ans-row {
    margin-bottom: 12px; /* 增加行间距 */
    font-size: 14px;
  }

/* 给解析加一个浅灰色背景框 */
  .analysis-info {
    margin-top: 15px;
    background: #f8f9fa;
    padding: 12px;
    border-radius: 6px;
  }

  .analysis-label {
    font-weight: bold;
    color: #3478e5;
    margin-bottom: 6px;
  }

  .analysis-text {
    line-height: 1.6;
    color: #555;
    white-space: pre-wrap;
  }

  .correct-ans { color: #67c23a; font-weight: bold; }
  .user-ans { color: #f56c6c; font-weight: bold; }
</style>
