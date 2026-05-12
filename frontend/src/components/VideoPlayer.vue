<template>
  <div v-if="visible" class="vp-modal-overlay" @click="close">
    <div class="vp-modal-container" @click.stop>
      <div class="vp-modal-header">
        <h3>{{ video?.vidTitle }}</h3>
        <button class="vp-close-btn" @click="close">✕</button>
      </div>
      <div class="vp-modal-body">
        <div class="vp-video-wrapper">
          <video
            v-if="isMp4Src"
            ref="videoPlayerRef"
            :src="video.vidUrl"
            controls
            @loadedmetadata="onVideoLoaded"
          ></video>
          <iframe
            v-else-if="video?.vidUrl"
            :src="iframeSrc"
            frameborder="0"
            allowfullscreen
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          ></iframe>
          <div v-else class="vp-no-video">视频链接暂不可用</div>
        </div>
        <p class="vp-video-description">{{ video?.vidDescription }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue';
import { isMp4 } from '../utils/common.js';

const props = defineProps({
  video: { type: Object, default: null },
  visible: { type: Boolean, default: false },
  startPosition: { type: Number, default: 0 },
});

const emit = defineEmits(['close', 'progress']);

const videoPlayerRef = ref(null);
const currentPosition = ref(0);
let watchTimer = null;

const isMp4Src = computed(() => isMp4(props.video?.vidUrl));

const iframeSrc = computed(() => {
  if (!props.video?.vidUrl) return '';
  if (isMp4Src.value) return '';
  if (props.startPosition > 0) {
    const sep = props.video.vidUrl.includes('?') ? '&' : '?';
    return `${props.video.vidUrl}${sep}t=${props.startPosition}`;
  }
  return props.video.vidUrl;
});

watch(() => props.visible, (val) => {
  if (val) {
    currentPosition.value = props.startPosition;
    document.body.style.overflow = 'hidden';
    startWatchTimer();
  } else {
    stopWatchTimer();
    document.body.style.overflow = '';
  }
});

function onVideoLoaded() {
  if (videoPlayerRef.value && currentPosition.value > 0) {
    videoPlayerRef.value.currentTime = currentPosition.value;
  }
}

function startWatchTimer() {
  if (watchTimer) clearInterval(watchTimer);
  watchTimer = setInterval(() => {
    currentPosition.value += 1;
  }, 1000);
}

function stopWatchTimer() {
  if (watchTimer) {
    clearInterval(watchTimer);
    watchTimer = null;
  }
}

function close() {
  emit('progress', {
    vidId: props.video?.vidId,
    position: currentPosition.value,
  });
  stopWatchTimer();
  document.body.style.overflow = '';
  emit('close');
}

onBeforeUnmount(() => {
  stopWatchTimer();
  document.body.style.overflow = '';
});
</script>

<style scoped>
.vp-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.vp-modal-container {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 880px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 35px rgba(0, 0, 0, 0.2);
  animation: vpModalFadeIn 0.2s ease;
}

.vp-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
}

.vp-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #18191c;
}

.vp-close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #9499a0;
  transition: color 0.2s;
  line-height: 1;
  padding: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.vp-close-btn:hover {
  color: #fb7299;
  background: #f1f2f3;
}

.vp-modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.vp-video-wrapper {
  position: relative;
  padding-bottom: 56.25%;
  height: 0;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
}

.vp-video-wrapper iframe,
.vp-video-wrapper video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
}

.vp-video-description {
  margin-top: 16px;
  font-size: 14px;
  color: #4e555e;
  line-height: 1.5;
}

.vp-no-video {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ccc;
  font-size: 14px;
}

@keyframes vpModalFadeIn {
  from { opacity: 0; transform: scale(0.96); }
  to { opacity: 1; transform: scale(1); }
}

@media (max-width: 768px) {
  .vp-modal-container { width: 95%; max-height: 90vh; }
  .vp-modal-header h3 { font-size: 16px; }
}
</style>