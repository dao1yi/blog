import request from '@/utils/request'
import type { Comment } from '@/types/article'
import type { ApiResponse } from '@/types/article'

interface CommentQuery {
  articleId: number
  parentId?: number
  page?: number
  size?: number
}

interface CommentCreateDTO {
  content: string
  articleId: number
  parentId?: number
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 获取评论列表
export function getComments(params: CommentQuery) {
  return request<ApiResponse<PageResult<Comment>>>({
    url: `/articles/${params.articleId}/comments`,
    method: 'get',
    params: {
      parentId: params.parentId,
      page: params.page,
      size: params.size
    }
  })
}

// 发表评论
export function submitComment(data: CommentCreateDTO) {
  return request<ApiResponse<void>>({
    url: `/articles/${data.articleId}/comments`,
    method: 'post',
    data
  })
}

// 删除评论
export function deleteComment(articleId: number, commentId: number) {
  return request<ApiResponse<void>>({
    url: `/articles/${articleId}/comments/${commentId}`,
    method: 'delete'
  })
} 