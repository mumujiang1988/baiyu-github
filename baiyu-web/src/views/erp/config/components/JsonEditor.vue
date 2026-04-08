/**
 * JSON Editor Component - Based on CodeMirror
 * Pure UI component, no business logic
 */

<script setup>
import { computed } from 'vue'
import { Codemirror } from 'vue-codemirror'
import { json } from '@codemirror/lang-json'

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  readonly: {
    type: Boolean,
    default: false
  },
  height: {
    type: String,
    default: '400px'
  },
  placeholder: {
    type: String,
    default: '请输入 JSON'
  },
  autofocus: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const extensions = [json()]

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
