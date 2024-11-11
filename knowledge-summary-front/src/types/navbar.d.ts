
interface NavBar {
  name: string,
  id: number
}

interface ChooseType {
  icon: string,
  funcFlag: string,
  targetPath: string,
  displayName: string
}

interface LoginBack {
  authorization: string,
  user: User,
  expirationTime: number
}

interface User {
  userId: number,
  userName: string,
  nickName: string,
  password: string,
  userDesc: string,
  userAvatar: string,
  createTime: string,
  finalUpTime: string,
  isDel: string,
  role: Array<Role>
}

interface Role {
  roleId: string,
  roleName: string,
  roleAuth: string,
  isDel: string,
}

interface AjaxResult<T> {
  msg: string,
  code: number,
  data?: T
}

interface GetSubIem {
  startTime?: string,
  endTime?: string,
  userId?: number
  todoThing?: string,
  todoLog?: string,
  stime?: any,
  etime?: any,
  shour?: number,
  ehour?: number
}

interface GetSubInfoRes {
  notOverTime?: GetSubIem[],
  overTime?: GetSubIem[]
}

interface Articles {
  articleId?: number;
  articleContent?: string;
  userId?: number;
  createTime?: string;
  articleTitle?: string;
  articleImage?: string;
  componentAddr?: string;
  iframeAddr?: string;
  publishUser?: User,
  tagInfoList?: TagInfo[],
  articlesData?: ArticlesData
  isLike?: boolean,
  rawText?: string,
  articleType?: 'markdown' | 'richtext'
}

interface TagInfo {
  tagId?: number;
  tagName?: string;
  createTime?: string;
  userId?: number;
}

interface Page<T> {
  records: T,
  total: number,
  size: number,
  current: number,
  orders: any,
  optimizeCountSql: boolean,
  searchCount: boolean,
  maxLimit: any,
  countId: any,
  pages: number
}

interface ArticlesData {
  view: number,
  like: number,
  comment: number
}

interface QdComment {
  commentId: number,
  commentContent: string
  createTime: string
  updateTime: string
  commentImg: string
  userId: number
  oucId: number
  user: User
  childrenComment: QdComment[],
  targetUser: User
}

interface userInfo {
  userName?: string
  nickName?: string
  userDesc?: string
  userAvatar?: string,
  userId?: number,
  userAddr?: string,
  userSex?: string,
  userPhone?: string,
  ipAddr?: string,
  userSchool?: string,
  finalUpTime?: string,
  roles?: Role[]
}

interface UserViews {
  userId: number
  articleId: number
  createTime: string
  articles: Articles
  user: User
}

interface ViewsInfo {
  totalCnt: number,
  views: {
    [props: string]: number
  },
  colors: {
    [key: string]: string
  }
}

interface UserArticlesData {
  totalLike?: number,
  totalViews?: number
}

interface SearchHistory {
  searchId?: number;
  searchContent?: string;
  userId?: number;
  createTime?: string;
  searchCount?: number
}

interface AuthorsListType {
  authorToArticlesNum?: [number: string],
  userList?: userInfo[]
}