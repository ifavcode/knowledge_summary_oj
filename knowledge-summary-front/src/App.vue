<script setup lang="ts">
import { GlobalThemeOverrides, NConfigProvider, zhCN, dateZhCN } from 'naive-ui'
import { useGlobalStore } from '~/store/globalStore'
import { Boot } from '@wangeditor/editor'
import markdownModule from '@wangeditor/plugin-md'
import formulaModule from '@wangeditor/plugin-formula'

Boot.registerModule(markdownModule)
Boot.registerModule(formulaModule)
const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#3F95FD',
  },
  Button: {
    colorHoverPrimary: '#3EBAF8',
    borderHoverPrimary: 'solid 1px #3EBAF8',
    textColorHover: '#3F95FD',
    borderHover: 'solid 1px #3F95FD',
    textColorFocus: '#3F95FD',
    borderFocus: 'solid 1px #3F95FD',
    borderPressed: 'solid 1px #3EBAF8',
    textColorPressed: '#3EBAF8',
    colorFocusPrimary: '#3EBAF8',
    borderFocusPrimary: 'solid 1px #3F95FD',
    colorPressedPrimary: '#3F95FD',
    borderPressedPrimary: 'solid 1px #3F95FD'
  },
  Input: {
    borderHover: 'solid 1px #3F95FD',
    boxShadowFocus: '0 0 0 1px #3F95FD',
    borderFocus: 'solid 1px #3F95FD',
    loadingColor: '#3F95FD',
  },
  Select: {
    borderHover: 'solid 1px #3F95FD',
  }
}

const globalStore = useGlobalStore()
</script>

<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <navbar />
      <NavbarMobile />
      <login v-show="globalStore.isLoginElementShow" />
      <RouterView />
    </n-message-provider>
  </n-config-provider>
</template>

<style lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;

  &::selection {
    background: #dcb6ff;
  }
}

/* 在线链接服务仅供平台体验和调试使用，平台不承诺服务的稳定性，企业客户需下载字体包自行发布使用并做好备份。 */
@font-face {
  font-family: 'iconfont';
  /* Project id 4161894 */
  src: url('//at.alicdn.com/t/c/font_4161894_1vkarwx1a4a.woff2?t=1730950569732') format('woff2'),
    url('//at.alicdn.com/t/c/font_4161894_1vkarwx1a4a.woff?t=1730950569732') format('woff'),
    url('//at.alicdn.com/t/c/font_4161894_1vkarwx1a4a.ttf?t=1730950569732') format('truetype');
}

.iconfont {
  font-family: "iconfont";
  font-style: normal;
}

code {}

#viewer,
.vditor-content,
.wang_editor_wrap {
  font-family: $primary-font-family !important;

  ul {
    margin-left: 0 !important;
    list-style: square !important;
    padding-left: 1em !important;

    li {}
  }

  h1 {
    @apply text-3xl;
  }

  h2 {
    @apply text-2xl;
  }

  h3 {
    @apply text-xl;
  }

  h4 {
    @apply text-lg;
  }

  h5 {
    @apply text-base;
  }

  h6 {
    @apply text-sm;
  }

  h1,
  h2 {
    font-weight: 400 !important;
    line-height: 1 !important;
  }

  h3,
  h4,
  h5,
  h6 {
    font-weight: 300 !important;
    color: #3d3d3d !important;
    line-height: 1.1 !important;
  }

  a {
    text-decoration: none !important;
    color: #5f9fd6 !important;

    &:hover {
      text-decoration: underline !important;
    }
  }

  strong {
    font-weight: 600;
  }

  blockquote {
    position: relative !important;
    color: #999 !important;
    font-weight: 300 !important;
    border-left: 3px solid #ddd !important;
    padding: .5rem 1rem !important;
    margin: .5rem 0 1rem !important;
    font-size: 14px !important;

    strong {
      font-weight: 300;
    }
  }

}
</style>
