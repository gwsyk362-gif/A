<template>
  <el-card shadow="never">
    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="输入账号或昵称搜索"
                style="width: 300px; margin-right: 15px"
                clearable @clear="handleSearch" @keyup.enter="handleSearch">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="userList" border stripe style="width: 100%; margin-top: 15px" v-loading="loading">
      <el-table-column prop="userId" label="ID" width="70" />
      <el-table-column prop="username" label="账号" width="130" />
      <el-table-column prop="nickname" label="昵称" />

      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="封禁"
            inline-prompt
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" type="success" link @click="openStatsDrawer(scope.row)">
            查看学情
          </el-button>
          <el-button size="small" type="primary" link @click="openEditDialog(scope.row)">
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchUsers"
        @current-change="fetchUsers"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="修改用户信息" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="editForm.username" type="number" placeholder="请输入8位账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="editForm.password" placeholder="请输入新密码(可选)" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="submitEdit">确 定</el-button>
          <el-button @click="dialogVisible = false">取 消</el-button>
        </span>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="`${currentStudentName} 的学情统计`" size="50%" @opened="initCharts" @closed="destroyCharts">
      <div v-loading="loadingStats" class="stats-drawer-content">
        <el-card shadow="hover" style="margin-bottom: 20px;">
          <template #header>全年学习活跃度</template>
          <div ref="adminHeatmapRef" style="height: 250px; width: 100%;"></div>
        </el-card>

        <div style="display: flex; gap: 20px; margin-bottom: 20px;">
          <el-card shadow="hover" style="flex: 1;">
            <template #header>知识点掌握情况</template>
            <div ref="adminRadarRef" style="height: 300px; width: 100%;"></div>
          </el-card>
          <el-card shadow="hover" style="flex: 1;">
            <template #header>近7日做题统计</template>
            <div ref="adminLineRef" style="height: 300px; width: 100%;"></div>
          </el-card>
        </div>

        <el-card shadow="hover">
          <template #header>各科目知识点覆盖率</template>
          <div ref="adminCoverageRef" style="height: 300px; width: 100%;"></div>
        </el-card>
      </div>
    </el-drawer>

  </el-card>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import axios from 'axios';
  import { ElMessage } from 'element-plus';

  // 数据定义
  const userList = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(10);
  const searchKeyword = ref('');

  const dialogVisible = ref(false);
  const editForm = ref({
    userId: null,
    username: '',
    password: '',
    nickname: ''
  });

  // 获取分页数据
  const fetchUsers = async () => {
    loading.value = true;
    try {
      const res = await axios.get('http://localhost:8080/api/users/page', {
        params: {
          current: currentPage.value,
          size: pageSize.value,
          keyword: searchKeyword.value
        }
      });
      userList.value = res.data.records;
      total.value = res.data.total;
    } catch (error) {
      ElMessage.error('获取用户列表失败');
    } finally {
      loading.value = false;
    }
  };

  // 搜索处理
  const handleSearch = () => {
    currentPage.value = 1;
    fetchUsers();
  };

  // 重置处理
  const resetSearch = () => {
    searchKeyword.value = '';
    currentPage.value = 1;
    fetchUsers();
  };

  const openEditDialog = (row) => {
    editForm.value = { ...row };
    dialogVisible.value = true;
  };

  const submitEdit = async () => {
    if (String(editForm.value.username).length !== 8) {
      ElMessage.warning('账号必须是 8 位数字');
      return;
    }

    try {
      const res = await axios.post('http://localhost:8080/api/users/update', editForm.value);
      if (res.data) {
        ElMessage.success('修改成功');
        dialogVisible.value = false;
        fetchUsers();
      } else {
        ElMessage.error('修改失败');
      }
    } catch (error) {
      ElMessage.error('网络错误，修改未提交');
    }
  };

  const handleStatusChange = async (row) => {
    try {
      const res = await axios.post('http://localhost:8080/api/users/changeStatus', {
        userId: row.userId,
        status: row.status
      });

      if (res.data) {
        ElMessage.success(`账号 ${row.username} 状态已更新`);
      } else {
        ElMessage.error('状态更新失败，请重试');
        row.status = row.status === 1 ? 0 : 1;
      }
    } catch (error) {
      ElMessage.error('网络请求失败');
      row.status = row.status === 1 ? 0 : 1;
    }
  };

  import * as echarts from 'echarts';
  import { nextTick } from 'vue';

  const drawerVisible = ref(false);
  const loadingStats = ref(false);
  const currentStudentName = ref('');
  const currentStudentId = ref(null);

  const adminHeatmapRef = ref(null);
  const adminRadarRef = ref(null);
  const adminLineRef = ref(null);
  const adminCoverageRef = ref(null);

  let heatmapChart = null;
  let radarChart = null;
  let lineChart = null;
  let coverageChart = null;

  const openStatsDrawer = (row) => {
    currentStudentName.value = row.nickname || row.username;
    currentStudentId.value = row.userId;
    drawerVisible.value = true;
  };

  const initCharts = async () => {
    loadingStats.value = true;
    const userId = currentStudentId.value;

    heatmapChart = echarts.init(adminHeatmapRef.value);
    radarChart = echarts.init(adminRadarRef.value);
    lineChart = echarts.init(adminLineRef.value);
    coverageChart = echarts.init(adminCoverageRef.value);

    try {
      const [yearlyRes, radarRes, lineRes, coverageRes] = await Promise.all([
        axios.get(`http://localhost:8080/records/stats/yearly?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/knowledge?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/daily?userId=${userId}`),
        axios.get(`http://localhost:8080/records/stats/coverage?userId=${userId}`)
      ]);

      const heatmapData = yearlyRes.data.map(d => [d.dateStr, d.totalCount]);
      const maxCount = heatmapData.length > 0 ? Math.max(...heatmapData.map(d => d[1])) : 20;
      heatmapChart.setOption({
        tooltip: { formatter: (p) => `${p.data[0]} : ${p.data[1]} 题` },
        visualMap: {
          min: 0, max: maxCount, orient: 'horizontal', left: 'center', bottom: 0,
          inRange: { color: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127'] }
        },
        calendar: {
          top: 20, left: 30, right: 30, cellSize: ['auto', 15], range: new Date().getFullYear(),
          itemStyle: { borderWidth: 0.5, borderColor: '#fff' },
          yearLabel: { show: false }, dayLabel: { nameMap: 'ZH' }, monthLabel: { nameMap: 'ZH' }
        },
        series: [{ type: 'heatmap', coordinateSystem: 'calendar', data: heatmapData }]
      });

      const rawKnowledgeData = radarRes.data;
      let indicator = [{ name: '暂无数据', max: 100 }], values = [0];
      if (rawKnowledgeData.length > 0) {
        const firstSubId = rawKnowledgeData[0].subId;
        const subData = rawKnowledgeData.filter(item => item.subId === firstSubId);
        indicator = subData.map(item => ({ name: item.kpName || '未知', max: 100 }));
        values = subData.map(item => item.correctRate || 0);
      }
      radarChart.setOption({
        tooltip: { trigger: 'item' },
        radar: { indicator: indicator, radius: '60%' },
        series: [{ type: 'radar', areaStyle: { color: 'rgba(64, 158, 255, 0.3)' }, data: [{ value: values, name: '掌握度(%)' }] }]
      });

      const dailyData = lineRes.data;
      lineChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['做题数量', '正确率(%)'] },
        xAxis: { type: 'category', data: dailyData.map(d => d.dateStr) },
        yAxis: [{ type: 'value' }, { type: 'value', max: 100 }],
        series: [
          { name: '做题数量', type: 'bar', data: dailyData.map(d => d.totalCount), itemStyle: { color: '#E6A23C' } },
          { name: '正确率(%)', type: 'line', yAxisIndex: 1, data: dailyData.map(d => d.correctRate), smooth: true, itemStyle: { color: '#67C23A' } }
        ]
      });

      const cvgData = coverageRes.data;
      coverageChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['已练知识点', '未练知识点'] },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: cvgData.map(d => d.subName) },
        series: [
          { name: '已练知识点', type: 'bar', stack: 'total', data: cvgData.map(d => d.practicedKp), itemStyle: { color: '#409EFF' } },
          { name: '未练知识点', type: 'bar', stack: 'total', data: cvgData.map(d => (d.totalKp - d.practicedKp)), itemStyle: { color: '#E4E7ED' } }
        ]
      });

    } catch (error) {
      ElMessage.error("获取该学生的统计数据失败");
    } finally {
      loadingStats.value = false;
    }
  };

  const destroyCharts = () => {
    heatmapChart?.dispose();
    radarChart?.dispose();
    lineChart?.dispose();
    coverageChart?.dispose();
  };

  onMounted(fetchUsers);
</script>

<style scoped>
  .search-bar {
    display: flex;
    align-items: center;
    background: #f9f9f9;
    padding: 15px;
    border-radius: 4px;
  }
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
</style>
