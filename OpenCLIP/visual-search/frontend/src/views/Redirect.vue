<template>
  <div></div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 立即重定向到目标页面
const { params } = route
const { path } = params

router.replace({ path: '/' + path })
</script>
