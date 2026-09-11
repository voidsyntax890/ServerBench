# ============================================================
# ServerBench Frontend
# Multi-stage Vite build + Nginx runtime
# ============================================================

FROM node:22-alpine AS build

WORKDIR /app

# Install dependencies from the locked dependency tree.
COPY server-frontend/package.json server-frontend/package-lock.json ./

RUN npm ci

# Copy frontend source.
COPY server-frontend/ ./

# Create the production build.
RUN npm run build


FROM nginx:1.29-alpine

# Remove the default Nginx site configuration.
RUN rm /etc/nginx/conf.d/default.conf

# Copy the built React application.
COPY --from=build /app/dist /usr/share/nginx/html

# ServerBench Nginx configuration will be added next.
COPY server-frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]