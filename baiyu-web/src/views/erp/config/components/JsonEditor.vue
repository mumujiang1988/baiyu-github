/**
 * JSON 编辑器组件 - 基于 CodeMirror
 * 纯 UI 组件，无业务逻辑
 */

<script setup lang="ts">
import { computed } from 'vue'
import { Codemirror } from 'vue-codemirror'
import { json } from '@codemirror/lang-json'

interface Props {
  modelValue: string
  readonly?: boolean
  height?: string
  placeholder?: string
  autofocus?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  height: '400px',
  placeholder: '请输入 JSON'
})

const emit = defineEmits<Emits>()

// CodeMirror 扩展
const extensions = [json()]

// 双向绑定
const content = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
</script>

<template>
  <div class="json-editor">
    <codemirror
      v-model="content"
      :extensions="extensions"
      :style="{ height }"
      :readonly="readonly"
      :autofocus="autofocus"
      :placeholder="placeholder"
      :indent-with-tab="true"
      :tab-size="2"
    />
  </div>
</template>

<style scoped lang="scss">
.json-editor {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
  
  :deep(.cm-editor) {
    height: 100%;
    font-size: 13px;
    
    &.cm-focused .cm-scroller {
      outline: none;
    }
    
    .cm-scroller {
      overflow: auto;
    }
    
    .cm-content {
      padding: 8px 0;
    }
    
    .cm-line {
      padding: 0 8px;
    }
  }
}
</style>
