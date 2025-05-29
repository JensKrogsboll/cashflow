# 🔧 Stage 1: Build the Angular app
FROM node:20-alpine as builder

WORKDIR /app

# Copy package.json and lock file first to optimize layer caching
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the code and build it
COPY . .
RUN npm run build --prod

# 🚀 Stage 2: Serve it with Nginx
FROM nginx:alpine

# Copy built Angular output to Nginx's web root
COPY --from=builder /app/dist /usr/share/nginx/html

# Remove default Nginx config and add custom one (optional)
# COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
