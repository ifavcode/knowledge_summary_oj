import mitt from "mitt";
import { useGlobalStore } from "~/store/globalStore";

const emitter = mitt();

function getAssetsImages(name: string) {
  return new URL(`/src/assets/${name}`, import.meta.url).href;
}

function checkLogin(showLoginEle?: boolean): boolean {
  const globalStore = useGlobalStore();
  let res =
    globalStore.authorization != null &&
    globalStore.authorization != "" &&
    globalStore.userInfo != null;
  if (showLoginEle && !res) {
    globalStore.isLoginElementShow = true;
    window.$message.warning("未登录");
  }
  return res;
}

const publishUserisAdmin = (roles: Role[]) => {
  if (!roles) return false;
  for (let role of roles) {
    if (role.roleAuth == "*") return true;
  }
  return false;
};

const base64ToBlob = (imgData: string) => {
  let raw = window.atob(imgData);
  let rawLength = raw.length;
  let uInt8Array = new Uint8Array(rawLength);
  for (let i = 0; i < rawLength; i++) {
    uInt8Array[i] = raw.charCodeAt(i);
  }
  return new Blob([uInt8Array], {
    type: "image/png",
  });
};

function getFileSuffix(fileName: string) {
  let idx = fileName.lastIndexOf(".");
  if (idx == -1) return "";
  return fileName.substring(idx + 1);
}

function formatFileSize(size: number | undefined) {
  if (!size) return '';
  let arr = ["B", "KB", "MB", "GB", "TB"];
  let cur = 0;
  while (cur < arr.length - 1) {
    if (size < 1024) break;
    size /= 1024;
    cur += 1;
  }
  return size.toFixed(2) + arr[cur];
}

function getExecCodeStatus(id: number) {
  if (id == 1) {
    return "In Queue";
  } else if (id == 2) {
    return "Processing";
  } else if (id == 3) {
    return "Accepted";
  } else if (id == 4) {
    return "Wrong Answer";
  } else if (id == 5) {
    return "Time Limit Exceeded";
  } else if (id == 6) {
    return "Compilation Error";
  } else if (id >= 7 && id <= 12) {
    return "Runtime Error";
  } else if (id == 13) {
    return "Internal Error";
  } else if (id == 14) {
    return "Exec Format Error";
  } else {
    return "Error";
  }
}

export {
  getAssetsImages,
  emitter,
  publishUserisAdmin,
  checkLogin,
  base64ToBlob,
  getFileSuffix,
  formatFileSize,
  getExecCodeStatus,
};
