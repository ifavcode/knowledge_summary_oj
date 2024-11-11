import { CloudFile, RS } from "~/interface/base";
import { getFileSuffix } from "~/utils";
import { request } from "~/utils/request";
import useDownload from "~/utils/useDownload";

export function getCloudFileListApi(
  filePath: string,
  ct: AbortController,
  fileName?: string
): Promise<AjaxResult<CloudFile[]>> {
  return request(
    `/api/cloud/file/list?filePath=${filePath}&fileName=${fileName || ""}`,
    {
      signal: ct.signal,
    }
  );
}

export function mkDirApi(
  filePath: string,
  dirName: string
): Promise<AjaxResult<null>> {
  return request(
    `/api/cloud/file/mkdir?filePath=${filePath}&dirName=${dirName}`,
    { method: "POST" }
  );
}

export function downloadFileApi(record: CloudFile) {
  let download = useDownload();
  download.downloadFile(
    import.meta.env.VITE_API_PREFIX +
    `/api/cloud/file/download?id=${record.id}`,
    record.fileName,
    record.dirFlag === "0" ? getFileSuffix(record.fileName as string) : "zip"
  );
  return download;
}

export function deleteFilePathApi(ids: string): Promise<AjaxResult<null>> {
  return request(`/api/cloud/file/delete/${ids}`, {
    method: "POST",
  });
}

export function getOnlineFileContentApi(fileUrl: string) {
  let flag = fileUrl.includes(import.meta.env.VITE_SERVER_DOMAIN_NAME);
  fileUrl = fileUrl.replace(import.meta.env.VITE_SERVER_DOMAIN_NAME, "");
  fileUrl = fileUrl.replace(import.meta.env.VITE_DOMAIN_NAME, "");
  return request((flag ? "/onlineFile" : "/ossFile") + fileUrl, {
    method: "GET",
    headers: {
      responseType: "text",
    },
  });
}
