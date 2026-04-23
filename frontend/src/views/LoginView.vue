<template xmlns="">
  <div class="login-container">
    <div class="login-box">
      <div class="header">
        <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>
      </div>

      <el-form :model="form" label-position="top">
        <el-form-item label="数字账号">
          <el-input
            v-model.number="form.username"
            type="text"
            maxlength="8"
            show-word-limit
            placeholder="请输入8位账号"
            @input="handleUsernameInput"
          />
        </el-form-item>

        <el-form-item label="登录密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>

        <div v-if="!isLogin">
          <el-form-item label="用户昵称">
            <el-input v-model="form.nickname" placeholder="请输入您的昵称" />
          </el-form-item>
        </div>

        <div class="actions">
          <el-button type="primary" @click="handleSubmit" class="main-btn">
            {{ isLogin ? '登录' : '完成注册' }}
          </el-button>
          <el-link type="info" @click="isLogin = !isLogin">
            {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
          </el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
  import { ref, reactive, defineEmits } from 'vue'
  import axios from 'axios'

  // 手动引入 Element Plus 组件
  import {
    ElForm, ElFormItem, ElInput,
    ElSelect, ElOption, ElButton, ElLink
  } from 'element-plus'
  import 'element-plus/dist/index.css'

  const emit = defineEmits(['login-success']);
  const isLogin = ref(true)

  const form = reactive({
    username: null,
    password: '',
    nickname: ''
  })

const handleUsernameInput = (value) => {
  form.username = value.replace(/[^\d]/g, ''); // 仅允许输入数字
};
const handleSubmit = async () => {
  // 长度校验
  if (String(form.username).length !== 8) {
    alert("账号必须是8位数字");
    return;
  }

  const payload = {
    username: Number(form.username), // 这里的名字必须和后端 User.java 一致
    password: form.password,
    nickname: isLogin.value ? undefined : form.nickname
  };

  // 3. 打印一下，你可以在控制台看到这个“载荷”到底长啥样
  console.log("即将发送给后端的数据包:", payload);

  try {
    if (isLogin.value) {
      // 登录请求
      const res = await axios.post('http://localhost:8080/api/users/login', payload);
      if (res.data && typeof res.data === 'object') {
        emit('login-success', res.data);
      } else {
        alert("账号或密码错误");
      }
    } else {
      // 注册请求
      const res = await axios.post('http://localhost:8080/api/users/register', payload);
      if (res.data === "Success") {
        alert("注册成功！请登录");
        isLogin.value = true;
      } else {
        alert("注册失败：" + res.data);
      }
    }
  } catch (error) {
    console.error("连接失败:", error);
    alert("服务器连接失败，请检查后端服务");
  }
};
</script>

<style scoped>

  .login-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  }

  .login-box {
    width: 400px;
    padding: 40px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }

  .header {
    text-align: center;
    margin-bottom: 30px;
  }

  .header h2 {
    color: #333;
    margin-bottom: 10px;
  }

  .header p {
    color: #888;
    font-size: 14px;
  }

  .actions {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-top: 20px;
  }

  .main-btn {
    width: 100%;
    height: 40px;
  }

  .switch-link {
    font-size: 13px;
  }
</style>
