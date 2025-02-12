<template>
  <div>
    <h2>Monthly Spendings</h2>
    <div>
      <label>Start</label>
      <input type="date" v-model="startDate" />
      <label>End</label>
      <input type="date" v-model="endDate" />
      <button @click="fetchSpendings">Load Spendings</button>
    </div>

    <div v-if="loading">Loading...</div>
    <table v-if="!loading && Object.keys(spendings).length">
      <thead>
      <tr>
        <th>Month</th>
        <th>Amount</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(amount, month) in spendings" :key="month">
        <td>{{ month }}</td>
        <td>{{ amount }}</td>
      </tr>
      </tbody>
    </table>
    <p v-else-if="!loading">No data found.</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      spendings: {},
      startDate: '',
      endDate: '',
      loading: false
    };
  },
  methods: {
    async fetchSpendings() {
      if (!this.startDate || !this.endDate) return;
      this.loading = true;
      try {
        const res = await axios.get('/api/spendings', {
          params: {
            start: this.startDate,
            end: this.endDate
          }
        });
        this.spendings = res.data;
      } catch (err) {
        console.error(err);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
