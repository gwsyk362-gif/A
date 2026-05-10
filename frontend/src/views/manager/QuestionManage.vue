<template>
  <el-card shadow="never">
    <div class="tool-bar">
      <el-button type="primary" icon="Plus" @click="openDialog()">新增题目</el-button>
      <el-button type="default" icon="Search" @click="toggleSearchBar">搜索</el-button>
    </div>

    <!-- 搜索卡片 -->
    <el-card v-if="showSearch" class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="科目">
          <el-select v-model="searchForm.subId" placeholder="请选择科目" clearable style="width: 180px" @change="onSearchSubChange">
            <el-option
              v-for="sub in subjectList"
              :key="sub.subId"
              :label="sub.subName"
              :value="sub.subId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点">
          <el-select v-model="searchForm.kp" placeholder="请先选择科目" clearable filterable allow-create style="width: 180px" :disabled="!searchForm.subId">
            <el-option
              v-for="kp in searchKpList"
              :key="kp"
              :label="kp"
              :value="kp"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题干">
          <el-input v-model="searchForm.keyword" placeholder="输入题干关键词" clearable style="width: 250px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="questionList" border style="margin-top: 20px" stripe v-loading="loading">
      <el-table-column prop="quesId" label="ID" width="60" />
      <el-table-column label="科目" width="120">
        <template #default="scope">
          {{ getSubjectName(scope.row.quesSubId) }}
        </template>
      </el-table-column>
      <el-table-column prop="quesContent" label="题干" />
      <el-table-column prop="quesOptions" label="选项" />
      <el-table-column prop="quesAnswer" label="答案" width="70" />
      <el-table-column prop="quesKp" label="知识点" width="120" />
      <!-- 操作列，详情改为打开详情弹窗 -->
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="openDetailDialog(scope.row)">详情</el-button>
          <el-button size="small" link type="primary" @click="openDialog(scope.row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
        @size-change="fetchQuestions"
        @current-change="fetchQuestions"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.quesId ? '编辑题目' : '新增题目'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属科目">
              <el-select v-model="form.quesSubId" placeholder="请选择科目" style="width: 100%;">
                <el-option v-for="sub in subjectList" :key="sub.subId" :label="sub.subName" :value="sub.subId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="知识点">
              <el-select
                v-model="form.quesKp"
                placeholder="请先选择科目"
                filterable allow-create default-first-option
                style="width: 100%;"
                :disabled="!form.quesSubId"
              >
                <el-option v-for="kp in currentKpList" :key="kp" :label="kp" :value="kp" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题干">
          <el-input v-model="form.quesContent" type="textarea" rows="3" placeholder="请输入题目描述" />
        </el-form-item>
        <el-form-item label="选项">
          <el-input v-model="form.quesOptions" type="textarea" placeholder="例如：A.xxx; B.xxx;" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="正确答案">
              <el-input v-model="form.quesAnswer" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度分值">
              <el-input-number v-model="form.quesScore" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="解析">
          <el-input v-model="form.quesAnalysis" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveQuestion">提交修改</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- ========= 新增：详情对话框（与编辑框样式一致，但表单只读） ========= -->
    <el-dialog v-model="detailVisible" title="题目详情" width="600px">
      <el-form :model="detailRow" label-width="100px" disabled>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属科目">
              <el-input :value="getSubjectName(detailRow.quesSubId)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="知识点">
              <el-input v-model="detailRow.quesKp" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题干">
          <el-input v-model="detailRow.quesContent" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="选项">
          <el-input v-model="detailRow.quesOptions" type="textarea" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="正确答案">
              <el-input v-model="detailRow.quesAnswer" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度分值">
              <el-input-number v-model="detailRow.quesScore" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="解析">
          <el-input v-model="detailRow.quesAnalysis" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
  import { ref, onMounted, watch, reactive } from 'vue';
  import axios from 'axios';
  import { ElMessage, ElMessageBox } from 'element-plus';

  // 表格与分页
  const questionList = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(10);

  // 搜索控制
  const showSearch = ref(false);
  const searchForm = reactive({
    subId: null,
    kp: '',
    keyword: ''
  });
  const searchKpList = ref([]);

  // 对话框与表单
  const dialogVisible = ref(false);
  const emptyForm = {
    quesId: null,
    quesSubId: null,
    quesContent: '',
    quesOptions: '',
    quesAnswer: '',
    quesAnalysis: '',
    quesKp: '',
    quesScore: 1500
  };
  const form = ref({ ...emptyForm });

  // 科目与知识点
  const subjectList = ref([]);
  const currentKpList = ref([]);

  // 详情对话框相关
  const detailVisible = ref(false);
  const detailRow = ref({ ...emptyForm });

  // 监听科目变化获取知识点（表单用）
  watch(() => form.value.quesSubId, async (newSubId) => {
    if (newSubId) {
      try {
        const res = await axios.get(`http://localhost:8080/getKpBySubject?subId=${newSubId}`);
        currentKpList.value = res.data;
      } catch {
        currentKpList.value = [];
      }
    } else {
      currentKpList.value = [];
    }
  });

  // 搜索卡片中科目变更时拉取知识点
  const onSearchSubChange = async (subId) => {
    searchForm.kp = '';
    if (subId) {
      try {
        const res = await axios.get(`http://localhost:8080/getKpBySubject?subId=${subId}`);
        searchKpList.value = res.data;
      } catch {
        searchKpList.value = [];
      }
    } else {
      searchKpList.value = [];
    }
  };

  const toggleSearchBar = () => {
    showSearch.value = !showSearch.value;
  };

  // 获取题目分页
  const fetchQuestions = async () => {
    loading.value = true;
    try {
      const params = {
        current: currentPage.value,
        size: pageSize.value,
        subId: searchForm.subId || undefined,
        kp: searchForm.kp || undefined,
        keyword: searchForm.keyword || undefined
      };
      const res = await axios.get('http://localhost:8080/questions/page', { params });
      if (res.data.records !== undefined) {
        questionList.value = res.data.records;
        total.value = res.data.total;
      } else {
        questionList.value = res.data;
        total.value = res.data.length;
      }
    } catch {
      ElMessage.error('获取题目列表失败');
    } finally {
      loading.value = false;
    }
  };

  const fetchSubjects = async () => {
    try {
      const res = await axios.get('http://localhost:8080/allSubjects');
      subjectList.value = res.data;
    } catch {
      ElMessage.error('获取科目列表失败');
    }
  };

  const handleSearch = () => {
    currentPage.value = 1;
    fetchQuestions();
  };

  const resetSearch = () => {
    searchForm.subId = null;
    searchForm.kp = '';
    searchForm.keyword = '';
    searchKpList.value = [];
    currentPage.value = 1;
    fetchQuestions();
  };

  const getSubjectName = (subId) => {
    const subject = subjectList.value.find(s => s.subId === subId);
    return subject ? subject.subName : '未知科目';
  };

  const openDialog = (row) => {
    form.value = row ? { ...row } : { ...emptyForm };
    dialogVisible.value = true;
  };

  const saveQuestion = async () => {
    if (!form.value.quesSubId) {
      ElMessage.warning('请选择所属科目');
      return;
    }
    try {
      const url = form.value.quesId ? '/updateQuestion' : '/addQuestion';
      await axios.post(`http://localhost:8080${url}`, form.value);
      ElMessage.success('操作成功');
      dialogVisible.value = false;
      fetchQuestions();
    } catch {
      ElMessage.error('保存失败');
    }
  };

  const handleDelete = (row) => {
    ElMessageBox.confirm('确定要删除这道题吗？', '提示', { type: 'warning' }).then(async () => {
      await axios.delete(`http://localhost:8080/deleteQuestion?id=${row.quesId}`);
      ElMessage.success('删除成功');
      fetchQuestions();
    });
  };

  // 打开详情对话框
  const openDetailDialog = (row) => {
    detailRow.value = { ...row };
    detailVisible.value = true;
  };

  onMounted(() => {
    fetchSubjects();
    fetchQuestions();
  });
</script>

<style scoped>
  .tool-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
  }
  .search-card {
    background-color: #f9f9f9;
    margin-top: 10px;
    padding: 15px;
    border-radius: 4px;
  }
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
</style>
