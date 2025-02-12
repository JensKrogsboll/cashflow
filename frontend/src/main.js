import { createApp } from 'vue';
import App from '../App.vue';
import router from './router.js';

import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as labs from 'vuetify/labs/components'; // Include experimental components
import * as directives from 'vuetify/directives';

const vuetify = createVuetify({
    components: { ...components, ...labs },
    directives
});

// Create Vue app instance
const app = createApp(App);
app.use(router);
app.use(vuetify);
app.mount('#app');
