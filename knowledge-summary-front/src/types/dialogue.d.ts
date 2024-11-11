interface XzGroup {
  groupId: number,
  userId: number,
  groupTitle: string,
  createTime: string
}

interface XzDialogue {
  dialogueId: number,
  diaContent: string,
  groupId: number,
  diaRole: string,
  createTime: string
}