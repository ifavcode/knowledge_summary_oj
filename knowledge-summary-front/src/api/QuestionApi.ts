import { Page, QuestionFile, Questions, Submissions } from "~/interface/base";
import { request } from "~/utils/request";

export function getQuestionPageApi(
  search: string = ""
): Promise<AjaxResult<Page<Questions>>> {
  return request(`/api/question/list?search=${search}`, {
    method: "GET",
  });
}

export function updateQuestionApi(
  questions: Questions
): Promise<AjaxResult<boolean>> {
  return request(`/api/question/update`, {
    method: "POST",
    data: questions,
  });
}

export function getQuestionTestcaseApi(
  questionCode: string
): Promise<AjaxResult<QuestionFile[]>> {
  return request(`/api/question/testcase?questionCode=${questionCode}`, {
    method: "GET",
  });
}

export function editQuestionTestcaseApi(
  questionFileList: QuestionFile[],
  questionCode: string
): Promise<AjaxResult<QuestionFile[]>> {
  return request(`/api/question/testcase/${questionCode}`, {
    method: "POST",
    data: questionFileList,
  });
}

export function publishQuestionApi(
  question: Questions
): Promise<AjaxResult<Questions>> {
  return request(`/api/question/publish`, {
    method: "POST",
    data: question,
  });
}

export function getQuestionDetailsApi(
  questionCode: string
): Promise<AjaxResult<Questions>> {
  return request(`/api/question/code?questionCode=${questionCode}`, {
    method: "GET",
  });
}

export function getLastAcUploadApi(
  questionCode: string
): Promise<AjaxResult<Questions>> {
  return request(`/api/question/lastAcUpload/${questionCode}`, {
    method: "GET",
  });
}

export function getLastUploadApi(
  questionCode: string
): Promise<AjaxResult<Questions>> {
  return request(`/api/question/lastUpload/${questionCode}`, {
    method: "GET",
  });
}

export function getSubmissionsDbApi(
  token: string
): Promise<AjaxResult<Submissions>> {
  return request(`/api/oj/submissions/db/${token}`, {
    method: "GET",
  });
}

export function saveQuestionTagApi(
  questionId: number,
  tagList: TagInfo[]
): Promise<AjaxResult<Submissions>> {
  return request(`/api/tag/question/add?questionId=${questionId}`, {
    method: "POST",
    data: tagList,
  });
}
