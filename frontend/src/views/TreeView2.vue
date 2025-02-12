<template>
  <div>
    <h2>Tree Structure</h2>
    <div v-if="loading">Loading tree...</div>
    <ul v-if="!loading">
      <li v-for="node in tree" :key="node.id">
        <strong>{{ node.name }}</strong> (ID: {{ node.id }})
        <div>
          Labels:
          <span v-for="label in node.labels || []" :key="label.id">
            {{ label.name }}
            <button @click="removeLabel(node.id, label.name)">x</button>
          </span>
          <input v-model="labelName" placeholder="Add label"/>
          <button @click="addLabel(node.id)">Add</button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      tree: [],
      loading: true,
      labelName: ''
    };
  },
  async created() {
    await this.fetchTree();
  },
  methods: {
    async fetchTree() {
      this.loading = true;
      try {
        const res = await axios.get('/api/tree');
        this.tree = res.data;
      } catch (err) {
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    async addLabel(nodeId) {
      if (!this.labelName) return;
      try {
        await axios.post(`/api/tree/${nodeId}/labels`, {
          labelName: this.labelName
        });
        this.labelName = '';
        await this.fetchTree();
      } catch (err) {
        console.error(err);
      }
    },
    async removeLabel(nodeId, labelName) {
      try {
        await axios.delete(`/api/tree/${nodeId}/labels/${labelName}`);
        await this.fetchTree();
      } catch (err) {
        console.error(err);
      }
    }
  }
};
</script>
