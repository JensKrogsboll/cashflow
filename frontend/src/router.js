import { createRouter, createWebHistory } from 'vue-router';
import HomeView from './views/HomeView.vue';
import UploadView from './views/UploadView.vue';
import TreeView from './views/TreeView.vue';
import SpendingsView from './views/SpendingsView.vue';

const routes = [
    { path: '/', component: HomeView },
    { path: '/upload', component: UploadView },
    { path: '/tree', component: TreeView },
    { path: '/spendings', component: SpendingsView },
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
