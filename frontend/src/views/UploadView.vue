<template>
  <div>
    <h2>Upload CSV</h2>
    <input type="file" ref="fileInput" @change="onFileChange" />
    <button @click="uploadFile">Upload</button>
    <p v-if="message">{{ message }}</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      file: null,
      message: '',
    };
  },
  methods: {
    onFileChange(e) {
      this.file = e.target.files[0];
    },
    async uploadFile() {
      if (!this.file) {
        this.message = 'No file selected.';
        return;
      }
      try {
        const formData = new FormData();
        formData.append('file', this.file);
        const res = await axios.post('/api/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        this.message = `Success: ${res.data}`;
      } catch (err) {
        this.message = `Upload failed: ${err.response?.data || err.message}`;
      }
    },
  },
};
</script>
