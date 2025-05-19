FROM n8nio/n8n:latest

WORKDIR /home/node

USER root
# Install TypeScript and Gulp as regular dependencies (not just dev dependencies)
RUN npm install typescript gulp n8n-workflow axios --save && \
    chown -R node:node /usr/local/lib/node_modules

USER node
# Copy your custom n8n nodes into the container
COPY --chown=node:node ./custom/n8n-nodes-rits /home/node/.n8n/custom/n8n-nodes-rits

WORKDIR /home/node/.n8n/custom/n8n-nodes-rits
RUN npm install

# Use npx to run TypeScript compiler and Gulp build
RUN npx tsc && npx gulp build:icons

CMD ["n8n", "start", "--tunnel"]
