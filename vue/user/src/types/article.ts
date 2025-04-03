export interface Article {
  id: number
  title: string
  content: string
  summary: string
  coverImage: string
  viewCount: number
  commentCount: number
  likeCount: number
  collectionCount: number
  status: number
  authorId: number
  createdTime: string
  updatedTime: string
  isDeleted: number
}

export interface ArticleHotVO {
  id: number
  title: string
  summary: string
  coverImage: string
  viewCount: number
  likeCount: number
  commentCount: number
  collectionCount: number
  periodCount: number
  authorId: number
  authorName: string
  createdTime: string
}

export interface ArticleQueryDTO {
  page?: number
  size?: number
  keyword?: string
  status?: number
}

export interface HotArticleParams {
  type: 'views' | 'likes'  // 修改为只有 views 和 likes
  period: string
  limit?: number
}

export interface Comment {
  id: number
  articleId: number
  content: string
  userId: number
  username: string
  userAvatar?: string
  parentId?: number
  replyTo?: string
  createdTime: string
  replies?: Comment[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
} 