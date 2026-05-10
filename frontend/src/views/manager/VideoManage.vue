<template>
  <el-card shadow="never">
    <div class="tool-bar">
      <span class="tip-text">视频热度排行</span>
      <el-button type="primary" icon="VideoCamera" @click="openVideoDialog()">上传视频资源</el-button>
    </div>

    <el-table
      :data="videoList"
      border
      stripe
      style="margin-top: 20px"
      v-loading="loading"
      @sort-change="handleSortChange"
      :default-sort="{ prop: 'viewCount', order: 'descending' }"
    >
      <el-table-column prop="vidId" label="ID" width="80" align="center" />

      <el-table-column label="封面" width="220" align="center">
        <template #default="scope">
          <el-image
            style="width: 180px; height: 102px; border-radius: 4px;"
            :src="'http://localhost:8080' + scope.row.vidCoverUrl"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="vidTitle" label="视频标题" min-width="200"/>
      <el-table-column prop="viewCount" label="观看量" width="120" sortable="custom" align="center" />
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <el-button size="small" link type="primary" @click="openVideoDialog(scope.row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="deleteVideo(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchVideoList"
        @current-change="fetchVideoList"
      />
    </div>

    <el-dialog v-model="videoDialogVisible" :title="videoForm.vidId ? '编辑视频资源' : '上传视频资源'" width="600px">
      <el-form :model="videoForm" label-width="100px">
        <el-form-item label="视频标题">
          <el-input v-model="videoForm.vidTitle" placeholder="输入视频标题" />
        </el-form-item>

        <el-form-item label="所属科目">
          <el-select v-model="videoForm.vidSubId" placeholder="选择科目" style="width: 100%">
            <el-option
              v-for="sub in subjectList"
              :key="sub.subId"
              :label="sub.subName"
              :value="sub.subId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="视频封面">
          <el-upload
            class="cover-uploader"
            action="http://localhost:8080/api/upload/cover"
            :data="{ subjectName: getSubjectPrefix(videoForm.vidSubId) }"
            :before-upload="beforeCoverUpload"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :on-error="handleCoverError"
          >
            <img v-if="videoForm.vidCoverUrl" :src="'http://localhost:8080' + videoForm.vidCoverUrl" class="cover-preview" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="视频链接">
          <el-input
            v-model="videoForm.vidUrl"
            @blur="formatVideoUrl"
            placeholder="输入B站视频分享链接"
          />
        </el-form-item>

        <el-form-item label="视频链接">
          <el-input v-model="videoForm.vidUrl" placeholder="输入视频存储路径" />
        </el-form-item>

        <el-form-item label="视频描述">
          <el-input v-model="videoForm.vidDescription" type="textarea" :rows="3" placeholder="输入视频简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitVideo">确认保存</el-button>
        <el-button @click="videoDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="视频详情" width="600px">
      <el-form :model="detailRow" label-width="100px" disabled>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="视频ID">
              <el-input v-model="detailRow.vidId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="观看量">
              <el-input v-model="detailRow.viewCount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="视频标题">
          <el-input v-model="detailRow.vidTitle" />
        </el-form-item>
        <el-form-item label="所属科目">
          <el-input :value="getSubjectName(detailRow.vidSubId)" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="detailRow.vidCoverUrl" />
          <div v-if="detailRow.vidCoverUrl" style="margin-top: 8px">
            <el-image
              style="width: 160px; height: 90px; border-radius: 4px;"
              :src="'http://localhost:8080' + detailRow.vidCoverUrl"
              fit="cover"
            />
          </div>
        </el-form-item>
        <el-form-item label="视频URL">
          <el-input v-model="detailRow.vidUrl" />
        </el-form-item>
        <el-form-item label="视频描述">
          <el-input v-model="detailRow.vidDescription" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="detailRow.vidCreateTime" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import axios from 'axios';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import { Plus } from '@element-plus/icons-vue';

  const videoList = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(10);
  const subjectList = ref([]);

  const sortField = ref('viewCount');
  const sortOrder = ref('desc');

  const videoDialogVisible = ref(false);
  const emptyForm = {
    vidId: null,
    vidTitle: '',
    vidCoverUrl: '',
    vidUrl: '',
    vidSubId: null,
    vidDescription: ''
  };
  const videoForm = ref({ ...emptyForm });

  const detailVisible = ref(false);
  const detailRow = ref({});

  const fetchVideoList = async () => {
    loading.value = true;
    try {
      const res = await axios.get('http://localhost:8080/api/admin/videos/list', {
        params: {
          page: currentPage.value,
          size: pageSize.value,
          sortField: sortField.value,
          sortOrder: sortOrder.value
        }
      });
      videoList.value = res.data.records || res.data;
      total.value = res.data.total || videoList.value.length;
    } catch (e) {
      ElMessage.error('获取视频列表失败');
    } finally {
      loading.value = false;
    }
  };

  const fetchSubjects = async () => {
    try {
      const res = await axios.get('http://localhost:8080/allSubjects');
      subjectList.value = res.data;
    } catch {
      ElMessage.error('获取科目失败');
    }
  };

  const getSubjectName = (subId) => {
    const subject = subjectList.value.find(s => s.subId === subId);
    return subject ? subject.subName : '未知科目';
  };

  const getSubjectPrefix = (subId) => {
    const subName = getSubjectName(subId);

    // 根据科目名称匹配前缀
    if (subName.includes('数据库原理') || subName.toLowerCase().includes('sql')) {
      return 'sql';
    } else if (subName.toUpperCase().includes('C++程序设计')) {
      return 'C++';
    } else if (subName.includes('数据结构')) {
      return 'ds';
    }

    return 'Unknown';
  };

  const openVideoDialog = (row) => {
    if (row) {
      videoForm.value = {
        vidId: row.vidId,
        vidTitle: row.vidTitle,
        vidCoverUrl: row.vidCoverUrl,
        vidUrl: row.vidUrl,
        vidSubId: row.vidSubId,
        vidDescription: row.vidDescription
      };
    } else {
      videoForm.value = { ...emptyForm };
    }
    videoDialogVisible.value = true;
  };

  const submitVideo = async () => {
    if (!videoForm.value.vidTitle) {
      ElMessage.warning('输入视频标题');
      return;
    }
    if (!videoForm.value.vidSubId) {
      ElMessage.warning('选择所属科目');
      return;
    }
    try {
      await axios.post('http://localhost:8080/api/admin/videos/save', videoForm.value);
      ElMessage.success(videoForm.value.vidId ? '修改成功' : '上传成功');
      videoDialogVisible.value = false;
      fetchVideoList();
    } catch (e) {
      ElMessage.error('操作失败');
    }
  };

  const deleteVideo = (row) => {
    ElMessageBox.confirm(`确定要删除“${row.vidTitle}”吗？`, '提示', { type: 'warning' })
      .then(async () => {
        await axios.delete(`http://localhost:8080/api/admin/videos/${row.vidId}`);
        ElMessage.success('删除成功');
        fetchVideoList();
      });
  };

  const handleSortChange = ({ prop, order }) => {
    sortField.value = prop || 'viewCount';
    sortOrder.value = order === 'ascending' ? 'asc' : 'desc';
    currentPage.value = 1;
    fetchVideoList();
  };

  const openDetail = (row) => {
    detailRow.value = { ...row };
    detailVisible.value = true;
  };

  //视频封面上传
// 成功回调
  const handleCoverSuccess = (res) => {
    videoForm.value.vidCoverUrl = res;
    ElMessage.success('封面上传成功');
  };

  // 失败回调
  const handleCoverError = (err) => {
    console.error("上传失败:", err);
    ElMessage.error('图片上传失败，请重试');
  };

  //B站链接自动转换
  const formatVideoUrl = () => {
    const rawUrl = videoForm.value.vidUrl;
    if (!rawUrl) return;

    // 正则匹配 BV 号
    const bvMatch = rawUrl.match(/(BV[a-zA-Z0-9]+)/);
    if (bvMatch) {
      const bvid = bvMatch[1];

      // 用正则匹配分P，如果没有 ?p= 默认就是第 1 页
      const pMatch = rawUrl.match(/[?&]p=(\d+)/);
      const page = pMatch ? pMatch[1] : 1;

      videoForm.value.vidUrl = `//player.bilibili.com/player.html?bvid=${bvid}&page=${page}`;
      ElMessage.success('已自动识别并转换为 Bilibili 播放器格式');
    }
  };

  // 校验是否已选科目
  const beforeCoverUpload = (file) => {
    if (!videoForm.value.vidSubId) {
      ElMessage.warning('请先在上方选择“所属科目”，然后再上传封面');
      return false; // 拦截上传请求
    }
    return true; // 允许上传
  };

  onMounted(() => {
    fetchVideoList();
    fetchSubjects();
  });
</script>

<style scoped>
  .tool-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
  .tip-text { font-size: 14px; color: #909399; }
  .pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
  .cover-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    width: 160px;
    height: 90px;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
  }
  .cover-preview { width: 100%; height: 100%; object-fit: cover; }
  .uploader-icon { font-size: 28px; color: #8c939d; }
</style>
