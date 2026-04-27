<template>
  <div class="app-container">
    <LoginView v-if="!isLoggedIn" @login-success="onLoginSuccess" />

    <MainPage v-else :user="currentUser" />
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import LoginView from './views/LoginView.vue';
  import MainPage from './views/MainView.vue';

  const isLoggedIn = ref(false);
  const currentUser = ref(null);

  const onLoginSuccess = (user) => {
    currentUser.value = user;
    isLoggedIn.value = true;
  };
</script>

<style>
  //全局背景、字体样式
  body {
    background-color: #ffffff;
    margin: 0;
    font-family: sans-serif;
  }
</style>
