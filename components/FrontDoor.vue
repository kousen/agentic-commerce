<script setup lang="ts">
// The course spine, as a persistent rail: shown on the slide where each
// Front Door step begins. The numbering is real sequence, not decoration.
const props = defineProps<{ step: number }>()
const steps = [
  'Discovery',
  'Connectivity',
  'Identity',
  'Authorization',
  'Approval',
  'Payment',
  'Evidence',
]
</script>

<template>
  <div class="frontdoor" aria-hidden="true">
    <div
      v-for="(name, i) in steps"
      :key="name"
      class="fd-step"
      :class="{ done: i + 1 < props.step, now: i + 1 === props.step }"
    >
      <span class="fd-num">{{ i + 1 }}</span>
      <span class="fd-name">{{ name }}</span>
    </div>
  </div>
</template>

<style scoped>
.frontdoor {
  position: absolute;
  left: 3.5rem;
  right: 3.5rem;
  bottom: 1.1rem;
  display: flex;
  gap: 0.35rem;
  align-items: stretch;
}
.fd-step {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.45rem;
  border-top: 2px solid var(--ink-3);
  padding-top: 0.4rem;
  color: var(--ink-3);
  font-family: 'Inter', sans-serif;
  font-size: 0.62rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  white-space: nowrap;
}
.fd-step.done {
  border-top-color: var(--petrol-dark);
  color: var(--grey);
}
.fd-step.now {
  border-top-color: var(--petrol);
  color: var(--petrol);
  font-weight: 600;
}
.fd-num {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.7rem;
}
</style>
