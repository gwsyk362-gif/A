<template>
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
            :maxlength="isLogin ? 20 : 8"
            :show-word-limit="!isLogin"
            :placeholder="isLogin ? '请输入数字账号' : '请输入8位数字账号'"
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
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import axios from 'axios'

  const router = useRouter()
  const isLogin = ref(true)

  const form = reactive({
    username: null,
    password: '',
    nickname: ''
  })

  // 只能输入数字
  const handleUsernameInput = (value) => {
    form.username = value.replace(/[^\d]/g, '');
  };

  const handleSubmit = async () => {
    // 注册时，强制要求必须是8位数字
    if (!isLogin.value && String(form.username).length !== 8) {
      alert("注册时账号必须是8位数字");
      return;
    }

    // 登录
    if (isLogin.value && !form.username) {
      alert("请输入账号");
      return;
    }

    const payload = {
      username: Number(form.username),
      password: form.password,
      nickname: isLogin.value ? undefined : form.nickname
    };

    try {
        if (isLogin.value) {
          const res = await axios.post('http://localhost:8080/api/users/login', payload);

          // 检查返回数据是否为用户对象
          if (res.data && typeof res.data === 'object') {
            if (res.data.status === 0) {
              alert('该账号已被封禁，无法登录');
              return;
            }

            // 登录成功，正常存储并跳转
            localStorage.setItem('currentUser', JSON.stringify(res.data));

            if (res.data.role === 'admin') {
              router.push('/manager');
            } else {
              router.push('/main');
            }
          } else {
            alert('账号或密码错误');
          }
        }else {
        const res = await axios.post('http://localhost:8080/api/users/register', payload);
        if (res.data === "Success") {
          alert("注册成功！请登录");
          form.password = '';
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
</style>
