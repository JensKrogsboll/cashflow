# Build stage
FROM node:18-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# ---------- Stage 2: Nginx + custom config ----------
FROM nginx:1.27-alpine

# Remove default config (optional)
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom Nginx config into the container
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# Copy Vue build artifacts from build-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]