<template>
  <el-card shadow="never">
    <div class="tool-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文章标题..."
        style="width: 300px"
        clearable
        @clear="fetchArticleList"
        @keyup.enter="fetchArticleList"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="openArticleDialog()">发布图文资源</el-button>
    </div>

    <el-table
      :data="articleList"
      border
      stripe
      style="margin-top: 20px"
      v-loading="loading"
    >
      <el-table-column prop="articleId" label="ID" width="80" align="center" />
      <el-table-column label="封面" width="140" align="center">
        <template #default="scope">
          <el-image
            v-if="scope.row.coverUrl"
            style="width: 120px; height: 68px; border-radius: 4px;"
            :src="scope.row.coverUrl"
            fit="cover"
          />
          <span v-else class="no-cover">无封面</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="文章标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="authorName" label="作者" width="120" align="center" />
      <el-table-column prop="viewCount" label="阅读量" width="100" align="center" sortable />
      <el-table-column prop="favoriteCount" label="收藏数" width="100" align="center" />
      <el-table-column label="创建时间" width="180" align="center">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <el-button size="small" link type="primary" @click="openArticleDialog(scope.row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="deleteArticle(scope.row)">删除</el-button>
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
        @size-change="fetchArticleList"
        @current-change="fetchArticleList"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="articleForm.articleId ? '编辑图文资源' : '发布图文资源'" width="700px" destroy-on-close>
      <el-form :model="articleForm" label-width="100px">
        <el-form-item label="文章标题">
          <el-input v-model="articleForm.title" placeholder="输入文章标题" />
        </el-form-item>
        <el-form-item label="封面图片URL">
          <el-input v-model="articleForm.coverUrl" placeholder="输入封面图片链接" />
        </el-form-item>
        <el-form-item label="文章摘要">
          <el-input v-model="articleForm.summary" type="textarea" :rows="2" placeholder="输入文章摘要" />
        </el-form-item>
        <el-form-item label="内容类型">
          <el-radio-group v-model="articleForm.contentType">
            <el-radio value="markdown">Markdown</el-radio>
            <el-radio value="html">HTML</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正文内容">
          <el-input v-model="articleForm.content" type="textarea" :rows="12" placeholder="输入文章正文（支持 Markdown 或 HTML）" />
        </el-form-item>
        <el-form-item label="作者ID">
          <el-input-number v-model="articleForm.authorId" :min="1" placeholder="输入作者用户ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitArticle">确认保存</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="文章详情" width="700px">
      <el-form :model="detailRow" label-width="100px" disabled>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="文章ID">
              <el-input v-model="detailRow.articleId" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="阅读量">
              <el-input v-model="detailRow.viewCount" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收藏数">
              <el-input v-model="detailRow.favoriteCount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="文章标题">
          <el-input v-model="detailRow.title" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="detailRow.authorName" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="detailRow.coverUrl" />
          <div v-if="detailRow.coverUrl" style="margin-top: 8px">
            <el-image style="width: 200px; height: 112px; border-radius: 4px;" :src="detailRow.coverUrl" fit="cover" />
          </div>
        </el-form-item>
        <el-form-item label="内容类型">
          <el-input v-model="detailRow.contentType" />
        </el-form-item>
        <el-form-item label="文章摘要">
          <el-input v-model="detailRow.summary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="正文内容">
          <el-input v-model="detailRow.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="detailRow.createTime" />
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

const articleList = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchKeyword = ref('');

const dialogVisible = ref(false);
const emptyForm = {
  articleId: null,
  title: '',
  coverUrl: '',
  summary: '',
  content: '',
  contentType: 'markdown',
  authorId: null
};
const articleForm = ref({ ...emptyForm });

const detailVisible = ref(false);
const detailRow = ref({});

const fetchArticleList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8080/api/articles/page', {
      params: {
        current: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value
      }
    });
    articleList.value = res.data.records || res.data;
    total.value = res.data.total || articleList.value.length;
  } catch (e) {
    ElMessage.error('获取文章列表失败');
  } finally {
    loading.value = false;
  }
};

const openArticleDialog = (row) => {
  if (row) {
    articleForm.value = {
      articleId: row.articleId,
      title: row.title,
      coverUrl: row.coverUrl,
      summary: row.summary,
      content: row.content,
      contentType: row.contentType || 'markdown',
      authorId: row.authorId
    };
  } else {
    articleForm.value = { ...emptyForm };
  }
  dialogVisible.value = true;
};

const submitArticle = async () => {
  if (!articleForm.value.title) {
    ElMessage.warning('请输入文章标题');
    return;
  }
  if (!articleForm.value.authorId) {
    ElMessage.warning('请输入作者ID');
    return;
  }
  try {
    if (articleForm.value.articleId) {
      await axios.put(`http://localhost:8080/api/articles/${articleForm.value.articleId}`, articleForm.value);
    } else {
      await axios.post('http://localhost:8080/api/articles/add', articleForm.value);
    }
    ElMessage.success(articleForm.value.articleId ? '修改成功' : '发布成功');
    dialogVisible.value = false;
    fetchArticleList();
  } catch (e) {
    ElMessage.error('操作失败');
  }
};

const deleteArticle = (row) => {
  ElMessageBox.confirm(`确定要删除"${row.title}"吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await axios.delete(`http://localhost:8080/api/articles/${row.articleId}`);
      ElMessage.success('删除成功');
      fetchArticleList();
    })
    .catch(() => {});
};

const openDetail = async (row) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/articles/${row.articleId}`);
    detailRow.value = res.data;
    detailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取文章详情失败');
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString();
};

onMounted(() => {
  fetchArticleList();
});
</script>

<style scoped>
.tool-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.no-cover { color: #c0c4cc; font-size: 13px; }
</style>