<template>
  <div class="dashboard-wrapper">
    <!-- 数据卡片 -->
    <el-row :gutter="20" class="panel-group">
      <el-col :span="12" v-for="(item, index) in panelData" :key="index" style="margin-bottom: 20px;">
        <el-card shadow="hover" class="data-card">
          <div class="card-icon" :style="{ backgroundColor: item.color + '20', color: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.title }}</div>
            <div class="card-num">{{ item.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 饼图 -->
    <div class="chart-group">
      <el-row>
        <el-col :span="24">
          <el-card shadow="hover" style="margin-bottom: 20px;">
            <template #header>各科目资源建设占比</template>
            <div ref="pieChartRef" style="height: 350px; width: 100%;"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 折线图1：近7天平均正确率 -->
      <el-row>
        <el-col :span="24">
          <el-card shadow="hover" style="margin-bottom: 20px;">
            <template #header>
              <div class="card-header">
                <span>近7天平均正确率</span>
                <el-button type="text" size="small" @click="showAccuracyMonthly">查看详情</el-button>
              </div>
            </template>
            <div ref="accuracyChartRef" style="height: 350px; width: 100%;"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 折线图2：近7天日活趋势 -->
      <el-row>
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>近7天日活趋势</span>
                <el-button type="text" size="small" @click="showActiveMonthly">查看详情</el-button>
              </div>
            </template>
            <div ref="activeChartRef" style="height: 350px; width: 100%;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 弹窗1：近30天正确率 -->
    <el-dialog
      v-model="accuracyDialogVisible"
      title="近30天平均正确率"
      width="70%"
      destroy-on-close
    >
      <div ref="monthlyAccuracyChartRef" style="height: 400px; width: 100%;"></div>
    </el-dialog>

    <!-- 弹窗2：近30天日活 -->
    <el-dialog
      v-model="activeDialogVisible"
      title="近30天日活人数"
      width="70%"
      destroy-on-close
    >
      <div ref="monthlyActiveChartRef" style="height: 400px; width: 100%;"></div>
    </el-dialog>
  </div>
</template>

<script setup>
  import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
  import * as echarts from 'echarts';
  import axios from 'axios';
  import { User, Document, VideoCamera, TrendCharts } from '@element-plus/icons-vue';

  const panelData = ref([
    { title: '系统总用户', value: 0, icon: 'User', color: '#40c9c6' },
    { title: '今日活跃用户', value: 0, icon: 'TrendCharts', color: '#36a3f7' },
    { title: '题库总数', value: 0, icon: 'Document', color: '#f4516c' },
    { title: '视频总数', value: 0, icon: 'VideoCamera', color: '#34bfa3' }
  ]);

  const pieChartRef = ref(null);
  const accuracyChartRef = ref(null);
  const activeChartRef = ref(null);

  let pieChart = null;
  let accuracyChart = null;
  let activeChart = null;

  // 弹窗相关
  const accuracyDialogVisible = ref(false);
  const activeDialogVisible = ref(false);
  const monthlyAccuracyChartRef = ref(null);
  const monthlyActiveChartRef = ref(null);
  let monthlyAccuracyChart = null;
  let monthlyActiveChart = null;

  const fetchDashboardData = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/admin/dashboard');
      const realData = res.data;

      // 更新卡片
      panelData.value[0].value = realData.totalUsers || 0;
      panelData.value[1].value = realData.dailyActive || 0;
      panelData.value[2].value = realData.totalQuestions || 0;
      panelData.value[3].value = realData.totalVideos || 0;

      // 饼图
      renderPieChart(realData.subjectResources || [], realData.subjectVideos || []);

      // 折线图 - 正确率
      const accuracyData = realData.accuracyTrend || { dates: [], correctRates: [] };
      renderAccuracyChart(accuracyData);

      // 折线图 - 日活
      const activeData = realData.activeTrend || { dates: [], activeUsers: [] };
      renderActiveChart(activeData);

    } catch (error) {
      console.error("请求大盘接口失败：", error);
    }
  };

  // 饼图
  const renderPieChart = (questionData, videoData) => {
    pieChart = echarts.init(pieChartRef.value);
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c} ({d}%)' },
      legend: { bottom: '0%', left: 'center' },
      title: [
        { text: '题库', left: '25%', top: '45%', textAlign: 'center', textStyle: { fontSize: 14, color: '#909399' } },
        { text: '视频', left: '75%', top: '45%', textAlign: 'center', textStyle: { fontSize: 14, color: '#909399' } }
      ],
      series: [
        {
          name: '题目数量',
          type: 'pie',
          radius: ['40%', '60%'],
          center: ['25%', '50%'],
          itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data: questionData
        },
        {
          name: '视频数量',
          type: 'pie',
          radius: ['40%', '60%'],
          center: ['75%', '50%'],
          itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data: videoData
        }
      ]
    });
  };

  // 正确率折线图
  const renderAccuracyChart = (data) => {
    accuracyChart = echarts.init(accuracyChartRef.value);
    accuracyChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: data.dates },
      yAxis: { type: 'value', name: '正确率(%)', min: 0, max: 100 },
      series: [
        {
          name: '平均正确率',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          itemStyle: { color: '#67C23A' },
          lineStyle: { width: 2 },
          areaStyle: { color: 'rgba(103, 194, 58, 0.1)' },
          data: data.correctRates
        }
      ]
    });
  };

  // 日活折线图
  const renderActiveChart = (data) => {
    activeChart = echarts.init(activeChartRef.value);
    activeChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: data.dates },
      yAxis: { type: 'value', name: '活跃人数', minInterval: 1 },
      series: [
        {
          name: '日活跃用户',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          itemStyle: { color: '#36a3f7' },
          lineStyle: { width: 2 },
          areaStyle: { color: 'rgba(54, 163, 247, 0.1)' },
          data: data.activeUsers
        }
      ]
    });
  };

  // 查看30天正确率详情
  const showAccuracyMonthly = async () => {
    accuracyDialogVisible.value = true;
    await nextTick();
    if (monthlyAccuracyChart) monthlyAccuracyChart.dispose();
    try {
      const res = await axios.get('http://localhost:8080/api/admin/dashboard/monthly-accuracy');
      const data = res.data || { dates: [], correctRates: [] };
      monthlyAccuracyChart = echarts.init(monthlyAccuracyChartRef.value);
      monthlyAccuracyChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: data.dates },
        yAxis: { type: 'value', name: '正确率(%)', min: 0, max: 100 },
        series: [{
          name: '平均正确率',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 5,
          itemStyle: { color: '#67C23A' },
          lineStyle: { width: 2 },
          areaStyle: { color: 'rgba(103, 194, 58, 0.1)' },
          data: data.correctRates
        }]
      });
    } catch (e) {
      console.error('获取30天正确率失败：', e);
    }
  };

  // 查看30天日活详情
  const showActiveMonthly = async () => {
    activeDialogVisible.value = true;
    await nextTick();
    if (monthlyActiveChart) monthlyActiveChart.dispose();
    try {
      const res = await axios.get('http://localhost:8080/api/admin/dashboard/monthly-active');
      const data = res.data || { dates: [], activeUsers: [] };
      monthlyActiveChart = echarts.init(monthlyActiveChartRef.value);
      monthlyActiveChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: data.dates },
        yAxis: { type: 'value', name: '活跃人数', minInterval: 1 },
        series: [{
          name: '日活跃用户',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 5,
          itemStyle: { color: '#36a3f7' },
          lineStyle: { width: 2 },
          areaStyle: { color: 'rgba(54, 163, 247, 0.1)' },
          data: data.activeUsers
        }]
      });
    } catch (e) {
      console.error('获取30天日活失败：', e);
    }
  };

  // 关闭弹窗时销毁图表
  watch(accuracyDialogVisible, (newVal) => {
    if (!newVal && monthlyAccuracyChart) {
      monthlyAccuracyChart.dispose();
      monthlyAccuracyChart = null;
    }
  });
  watch(activeDialogVisible, (newVal) => {
    if (!newVal && monthlyActiveChart) {
      monthlyActiveChart.dispose();
      monthlyActiveChart = null;
    }
  });

  // 窗口大小自适应
  const handleResize = () => {
    pieChart?.resize();
    accuracyChart?.resize();
    activeChart?.resize();
    monthlyAccuracyChart?.resize();
    monthlyActiveChart?.resize();
  };

  onMounted(() => {
    fetchDashboardData();
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    pieChart?.dispose();
    accuracyChart?.dispose();
    activeChart?.dispose();
    monthlyAccuracyChart?.dispose();
    monthlyActiveChart?.dispose();
  });
</script>

<style scoped>
  .dashboard-wrapper {
    padding: 10px;
  }

  .panel-group {
    margin-bottom: 20px;
  }

  .data-card {
    height: 120px;
    cursor: pointer;
  }

  :deep(.el-card__body) {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: left;
    padding: 20px;
  }

  .card-icon {
    width: 70px;
    height: 70px;
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32px;
    margin-right: 30px;
  }

  .card-info {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 120px;
  }

  .card-title {
    font-size: 15px;
    color: #8c8c8c;
    margin-bottom: 10px;
  }

  .card-num {
    font-size: 28px;
    font-weight: bold;
    color: #333;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chart-group {
    margin-top: 20px;
  }
</style>
