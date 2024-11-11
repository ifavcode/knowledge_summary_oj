import { Router, createRouter, createWebHashHistory } from "vue-router";

const router: Router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      redirect: "/home",
    },
    {
      path: "/home",
      component: () => import("../views/home/home.vue"),
    },
    {
      path: "/publish_article",
      component: () => import("../components/article/publishArticle.vue"),
    },
    {
      path: "/_edit_article/:articleId",
      component: () => import("../components/article/updateArticles.vue"),
    },
    {
      path: "/edit_article/:articleId",
      component: () => import("../components/article/updateArticlesMD.vue"),
    },
    {
      path: "/_publish_article",
      component: () => import("../components/article/publishArticleWang.vue"),
    },
    {
      path: "/article/:articleId",
      component: () => import("../components/article/articleContent.vue"),
    },
    {
      path: "/profile",
      children: [
        {
          path: ":userId",
          component: () => import("../views/profile/profile.vue"),
        },
        {
          path: "files",
          component: () => import("../views/profile/files.vue"),
        },
      ],
    },
    {
      path: "/search",
      component: () => import("../views/search/search.vue"),
    },
    {
      path: "/ai_image",
      component: () => import("../views/aiImage/index.vue"),
    },
    {
      path: "/not_found",
      children: [
        {
          path: "user",
          component: () => import("~/views/not_found/userNotFound.vue"),
        },
        {
          path: "not_auth",
          component: () => import("~/views/not_found/userNotAuth.vue"),
        },
      ],
    },
    {
      path: "/xz_ask",
      component: () => import("../views/xz_ask/XzAsk.vue"),
    },
    {
      path: "/questions",
      redirect: "/questions/index",
      children: [
        {
          path: "index",
          component: () => import("../views/qustions/index.vue"),
        },
        {
          path: ":questionCode",
          component: () => import("../views/qustions/questionsDetails.vue"),
        },
        {
          path: "edit/:questionCode",
          component: () => import("../views/qustions/editQuestion.vue"),
        },
        {
          path: "publish",
          component: () => import("../views/qustions/publishQuestion.vue"),
        },
      ],
    },
    {
      path: "/submissions/:token",
      component: () => import("../views/submissions/index.vue"),
    },
  ],
  scrollBehavior(_to, _from, _savedPosition) {
    // return 期望滚动到哪个的位置
  },
});

router.beforeEach((_to, _from, next) => {
  next();
});

router.afterEach(() => {});

export default router;
