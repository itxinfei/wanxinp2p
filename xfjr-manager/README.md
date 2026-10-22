# 心飞金融管理后台（xfjr-manager）

> 心飞金融 P2P 平台统一运营管理前端。

## 项目信息

- 所属项目：心飞金融 P2P 平台（xfjr）
- 技术栈：Vue 3 + Element Plus + Vite（由 vue-element-admin / Vue 2 + element-ui 升级而来）
- 接口规范：统一 `/api/v1/...`

## 技术栈

- 构建：Vite 8 + `@vitejs/plugin-vue`
- 框架：Vue 3.5 + vue-router 5 + vuex 4
- 组件库：Element Plus 2.9
- 请求：Axios 1.7（全局拦截错误并统一提示）
- 样式：Sass + normalize.css + nprogress

## 图示
 ![image](demo.png)
## 项目运行

```bash
# clone the project
git clone http://git.itcast.cn/development/project-p2p-cmm-java-react.git

# install dependency
npm install

# develop
npm run dev

# build for test environment
npm run build:stage

# build for production environment
npm run build:prod
```

This will automatically open http://localhost:9528

## 项目结构
├── build                      # 构建相关 <br/>
├── mock                       # 项目mock 模拟数据<br/>
├── plop-templates             # 基本模板<br/>
├── public                     # 静态资源<br/>
│   │── favicon.ico            # favicon图标<br/>
│   └── index.html             # html模板<br/>
├── src                        # 源代码<br/>
│   ├── api                    # 所有请求<br/>
│   ├── assets                 # 主题 字体等静态资源<br/>
│   ├── components             # 全局公用组件<br/>
│   ├── directive              # 全局指令<br/>
│   ├── filters                # 全局 filter<br/>
│   ├── icons                  # 项目所有 svg icons<br/>
│   ├── lang                   # 国际化 language<br/>
│   ├── layout                 # 全局 layout<br/>
│   ├── router                 # 路由<br/>
│   ├── store                  # 全局 store管理<br/>
│   ├── styles                 # 全局样式<br/>
│   ├── utils                  # 全局公用方法<br/>
│   ├── vendor                 # 公用vendor<br/>
│   ├── views                  # views 所有页面<br/>
│   ├── App.vue                # 入口页面<br/>
│   ├── main.js                # 入口文件 加载组件 初始化等<br/>
│   └── permission.js          # 权限管理<br/>
├── tests                      # 测试<br/>
├── .env.xxx                   # 环境变量配置<br/>
├── .eslintrc.js               # eslint 配置项<br/>
├── .babelrc                   # babel-loader 配置<br/>
├── .travis.yml                # 自动化CI配置<br/>
├── vue.config.js              # vue-cli 配置<br/>
├── postcss.config.js          # postcss 配置<br/>
└── package.json               # package.json<br/>

## 项目文档

- 文档： https://panjiachen.gitee.io/vue-element-admin-site/zh/guide/
- 组件：https://element.eleme.cn/#/zh-CN/component/installation
- 实例项目参考： https://github.com/PanJiaChen/vue-element-admin


