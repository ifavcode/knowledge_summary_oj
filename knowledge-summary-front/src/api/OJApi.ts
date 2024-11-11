import { Submissions, WebSubmitCode } from "~/interface/base";
import { request } from "~/utils/request";

export function submitCodeApi(data: WebSubmitCode): Promise<{ token: string }> {
  return request(`/api/oj/submissions`, {
    method: "POST",
    data,
  });
}

export function getLanguagesApi(): Promise<AjaxResult<Record<string, any>[]>> {
  return request(`/api/oj/languages`, {
    method: "GET",
  });
}

export function getSubmissionDetailsApi(
  token: string
): Promise<AjaxResult<Record<string, any>[]>> {
  return request(`/api/oj/submissions/${token}`, {
    method: "GET",
  });
}

export function execCodeTestApi(
  data: WebSubmitCode
): Promise<Record<string, any>> {
  return request(`/api/oj/test`, {
    method: "POST",
    data,
  });
}

export function queryTestStatusApi(
  submitToken: string
): Promise<Record<string, any>> {
  return request(`/api/oj/submitToken/${submitToken}`, {
    method: "GET",
  });
}

export function getSubmissionsSelfApi(
  questionCode: string
): Promise<AjaxResult<Submissions[]>> {
  return request(`/api/oj/submissions/self?questionCode=${questionCode}`, {
    method: "GET",
  });
}
