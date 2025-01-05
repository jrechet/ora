FROM node:18-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install cors
RUN npm install express

COPY server.js .

EXPOSE 3000

CMD ["npm", "start"]