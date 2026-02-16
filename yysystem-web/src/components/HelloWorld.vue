<script setup lang="ts">
import { ref } from 'vue'

const count = ref(0)
</script>

<template>
  <div class="card">
    <h1>原邑销售管理平台</h1>
    <el-button type="primary" @click="count++">count is {{ count }}</el-button>
  </div>
</template>

<style scoped>
.card {
  padding: 2em;
  text-align: center;
}
</style>
