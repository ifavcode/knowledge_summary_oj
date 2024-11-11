import Axios, { AxiosRequestConfig } from "axios";
import { MessageApiInjection } from "naive-ui/lib/message/src/MessageProvider";
//@ts-ignore
import Cookies from "js-cookie";
import { useGlobalStore } from "~/store/globalStore";
import { createDiscreteApi } from "naive-ui";

declare global {
  interface Window {
    $message: MessageApiInjection;
  }
}

const anonymousPath: string[] = [
  "/user/register",
  "/user/login",
  "/articles/all",
  "/articles/comment",
  "/tag/all",
  "/search",
  "/articles/recommend/all",
];
const pathReg = [/\/article\/\d+/];

const { loadingBar } = createDiscreteApi(["loadingBar"], {
  loadingBarProviderProps: {
    loadingBarStyle: {
      loading: {
        color: "#3f95fd",
        backgroundColor: "#3f95fd",
      },
    },
  },
});

const client = Axios.create({
  baseURL: import.meta.env.VITE_API_PREFIX,
  timeout: 120000,
});

client.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    const globalStore = useGlobalStore();
    let needAuth = true;
    for (let i = 0; i < pathReg.length; i++) {
      if (pathReg[i].test(config.url as string)) {
        needAuth = false;
        break;
      }
    }
    if (!(!needAuth || anonymousPath.includes(config.url as string))) {
      config.headers.Authorization =
        globalStore.authorization || Cookies.get("authorization");
    }

    return config;
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

client.interceptors.response.use(
  function (response) {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    return response;
  },
  (error) => {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    // window.$message.error(error.message)
    return Promise.reject(error);
  }
);

//anonymous

export async function requestAnonymous(
  url: string,
  config?: AxiosRequestConfig
) {
  let response: any;
  let result: any;
  loadingBar.start();
  try {
    response = await client.request({
      url,
      ...config,
    });
  } catch (error: any) {
    loadingBar.finish();
  } finally {
    loadingBar.finish();
  }
  if (response) {
    result = response.data;
  }
  // 你的业务判断逻辑
  return result;
}

export async function request(url: string, config?: AxiosRequestConfig) {
  let response: any;
  let result: any;
  // loadingBar.start();
  try {
    response = await client.request({
      url,
      ...config,
    });
  } catch (error: any) {
    // loadingBar.finish();
  } finally {
    // loadingBar.finish();
  }
  if (response) {
    result = response.data;
  }
  // 你的业务判断逻辑
  return result;
}

export { client };
