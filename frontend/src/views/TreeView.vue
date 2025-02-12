<script setup>
import { ref, onMounted } from "vue";

const treeData = ref([]); // Store the top-level nodes
const loadingNodes = ref(new Set()); // Track loading state per node
const openNodes = ref([]);

// Fetch top-level nodes
const fetchTopNodes = async () => {
  try {
    const response = await fetch("/api/tree");
    if (!response.ok) throw new Error("Failed to load data");
    const nodes = await response.json();
    treeData.value = transformTree(nodes);
  } catch (error) {
    console.error("Error loading top-level nodes:", error);
  }
};

// Fetch child nodes when a parent expands
const loadChildren = async (node) => {
  if (node.children.length > 0) return; // Prevent duplicate fetch
  loadingNodes.value.add(node.id);

  try {
    const response = await fetch(`/api/tree?parentId=${node.id}`);
    if (!response.ok) throw new Error("Failed to load children");

    const children = await response.json();
    node.children = transformTree(children);
  } catch (error) {
    console.error("Error loading child nodes:", error);
  } finally {
    loadingNodes.value.delete(node.id);
  }
};

const transformTree = (nodes) => {
  return nodes.map(node => ({
    id: node.id,
    name: node.name,
    children: node.children || [], // Ensure children array exists
    postings: node.postings || []  // Ensure postings array exists
  }));
};

// Load top-level nodes on mount
onMounted(fetchTopNodes);
</script>

<template>
  <v-container>
    <v-treeview
        :items="treeData"
        item-value="id"
        item-title="name"
        return-object
        v-model:open="openNodes"
        @update:open="(newOpenNodes) => {
        const newlyOpened = newOpenNodes.find(id => !openNodes.includes(id));
        if (newlyOpened) {
          const node = treeData.find(n => n.id === newlyOpened);
          if (node) loadChildren(node);
        }
      }"
    >
      <template v-slot:prepend="{ item }">
        <v-progress-circular v-if="loadingNodes.has(item.id)" indeterminate size="16"></v-progress-circular>
        <v-icon v-else-if="item.children.length">mdi-folder</v-icon>
        <v-icon v-else>mdi-file-document</v-icon>
      </template>
    </v-treeview>
  </v-container>
</template>

<style scoped>
.tree-label {
  display: flex;
  flex-direction: column;
}
</style>
