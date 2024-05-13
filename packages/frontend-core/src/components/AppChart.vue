<script setup lang="ts">
import { AgCharts } from "ag-charts-vue3";
import type { AgChartOptions } from "ag-charts-community";
import { useQuasar } from "quasar";
import { computed } from "vue";

const quasar = useQuasar();

const props = defineProps<{
  readonly options: AgChartOptions;
}>();

const options = computed<AgChartOptions>(() => ({
  ...props.options,
  theme: quasar.dark.isActive ? "ag-default-dark" : "ag-default",
}));
</script>

<template>
  <ag-charts class="app-chart" :options="options"></ag-charts>
</template>

<style scoped lang="scss">
.app-chart {
  min-height: 30vh;
  max-height: 70vh;
  height: 60vh;
  overflow: hidden;
  resize: vertical;
  padding: 6px;
}
</style>
