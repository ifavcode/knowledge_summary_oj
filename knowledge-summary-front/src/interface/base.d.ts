export interface GenerateImage {
  prompt?: string;

  size?: string;

  negativePrompt?: string;

  n?: number;

  steps?: number;

  samplerIndex?: string;

  userId?: number;
}

export interface CloudFile {
  id?: number;

  fileName?: string;

  fileSize?: number;

  fileUrl?: string;

  createTime?: any;

  delFlag?: "0" | "1";

  userId?: number;

  filePath?: string;

  dirFlag?: "0" | "1";
}

export interface AjaxResult<T> {
  /**
   * 状态码
   */
  code: number;

  /**
   * 返回内容
   */
  msg: string;

  /**
   * 数据对象
   */
  data?: T;
}

export interface Page<T> {
  total: number;
  size: number;
  current: number;
  records: T[];
}

export type RS = Record<string, any>;

export interface Questions {
  id?: number;

  title: string;

  questionCode: string;

  questionContent: string;

  diffLevel: string;

  userId?: number;

  createTime?: string;

  delFlag?: string;

  questionDesc?: string;

  acFlag?: boolean;

  token?: string;

  totalAc?: number;

  totalTry?: number;

  tagList?: TagInfo[];
}

export interface WebSubmitCode {
  sourceCode: string;
  languageId: number;
  questionCode: string;
  stdin?: string;
}

export interface Submissions {
  id?: number;

  sourceCode?: string;

  token?: string;

  submitTokens?: string;

  userId?: number;

  createTime?: any;

  delFlag?: string;

  stdinFileIdsStr?: string;

  stdoutFileIdsStr?: string;

  questionCode?: string;

  execResults?: Record<string, any>;

  user?: User;

  languages?: {
    id: number;
    languageName: string;
  };
}

export interface CloudFile {
  id?: number;

  fileName?: string;

  fileSize?: number;

  fileUrl?: string;

  createTime?: any;

  delFlag?: string;

  userId?: number;

  filePath?: string;

  dirFlag?: string;

  serialVersionUID?: number;
}

export interface QuestionFile {
  questionCode?: string;

  stdinFileId?: number;

  stdoutFileId?: number;

  inFile?: CloudFile;

  outFile?: CloudFile;
}

export interface TagInfo {
  tagId: number;

  tagName?: string;

  createTime?: any;

  userId: number;
}
