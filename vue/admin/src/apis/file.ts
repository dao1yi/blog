import axios from '../axios'

export const fileApi = {
  // 上传文件
  upload(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post('/files', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取文件列表
  getFiles(params: {
    page: number
    size: number
    fileName?: string
    fileType?: string
  }) {
    return axios.get('/files', { params })
  },

  // 删除文件
  deleteFile(id: number) {
    return axios.delete(`/files/${id}`)
  }
} 