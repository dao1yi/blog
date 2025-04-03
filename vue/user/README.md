# 智己书阁

这是一个基于 Vue 3 + TypeScript + Vite 构建的个人博客系统前端项目。

## 技术栈

- Vue 3 - 渐进式 JavaScript 框架
- TypeScript - JavaScript 的超集，提供类型系统
- Vite - 下一代前端构建工具
- Vue Router - Vue.js 官方路由管理器
- Element Plus - 基于 Vue 3 的组件库
- Axios - 基于 Promise 的 HTTP 客户端

## 主要功能

- 用户认证
  - 登录/注册
  - 邮箱验证
  - Token 管理
  
- 文章管理
  - 文章列表展示
  - 文章详情页
  - 文章点赞/收藏
  - 文章评论
  
- 个人中心
  - 个人信息展示
  - 我的点赞/收藏
  - 简历信息展示

## 项目结构

bash
src/
├── api/ # API 接口
├── assets/ # 静态资源
├── components/ # 公共组件
├── router/ # 路由配置
├── types/ # TypeScript 类型定义
├── utils/ # 工具函数
└── views/ # 页面组件

## 开发环境要求

- Node.js >= 18.0.0
- npm >= 8.0.0


## 快速开始

1. 克隆项目

```bash
git clone <project-url>
cd <project-name>
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

4. 构建生产版本
```bash
npm run build
```

## 开发配置

### 开发服务器配置

```typescript
// vite.config.ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080/',
      changeOrigin: true
    }
  },
  host: '0.0.0.0'
}
```

### 环境要求

- Node.js: >= 18.0.0
- npm: >= 8.0.0
- Vue: ^3.3.11
- TypeScript: ^5.2.2
- Vite: ^5.0.8

## IDE 配置

推荐使用 VS Code 进行开发，并安装以下插件：

- [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) - Vue 3 的官方 IDE 支持
- [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin) - 提供 `.vue` 文件的 TypeScript 支持

注意：使用 Volar 时需要禁用 Vetur

## TypeScript 配置优化

如果发现 TypeScript 性能不佳，可以启用 Volar 的 Take Over 模式：

1. 禁用内置的 TypeScript 扩展
   1. 在 VS Code 命令面板中运行 `Extensions: Show Built-in Extensions`
   2. 找到 `TypeScript and JavaScript Language Features`，右键选择 `Disable (Workspace)`
2. 重新加载 VS Code

## 许可证

[MIT](LICENSE)
