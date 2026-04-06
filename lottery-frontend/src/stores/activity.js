import { defineStore } from 'pinia'
import { ref } from 'vue'
import { activityApi } from '@/api/modules/activity'

export const useActivityStore = defineStore('activity', () => {
  const activities = ref([])
  const currentActivity = ref(null)
  const total = ref(0)
  const loading = ref(false)

  async function fetchActivities(page = 1, size = 10) {
    loading.value = true
    try {
      const res = await activityApi.getActivityList(page, size)
      activities.value = res.data.records
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function fetchActivityDetail(id) {
    loading.value = true
    try {
      const res = await activityApi.getActivityDetail(id)
      currentActivity.value = res.data
    } finally {
      loading.value = false
    }
  }

  function clearCurrentActivity() {
    currentActivity.value = null
  }

  return {
    activities,
    currentActivity,
    total,
    loading,
    fetchActivities,
    fetchActivityDetail,
    clearCurrentActivity
  }
})
