<template>
  <div class="resume-manage">
    <div class="header">
      <h3 class="page-title">简介管理</h3>
      <el-button type="primary" @click="handleCreate" v-if="!resume.id">
        <el-icon><Plus /></el-icon>
        创建简介
      </el-button>
    </div>

    <div class="content" v-loading="loading">
      <template v-if="resume?.id != null">
        <el-form ref="formRef" :model="resume" label-width="100px" :rules="rules">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="resume.name" placeholder="请输入姓名" />
          </el-form-item>
          
          <el-form-item label="头像" prop="avatar">
            <el-input v-model="resume.avatar" placeholder="请输入头像URL" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="resume.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="个人介绍" prop="introduction">
            <el-input 
              v-model="resume.introduction" 
              type="textarea" 
              :rows="4"
              placeholder="请输入个人介绍" 
            />
          </el-form-item>
          
          <el-form-item label="求职意向" prop="jobIntention">
            <el-input v-model="resume.jobIntention" placeholder="请输入求职意向" />
          </el-form-item>

          <el-form-item label="状态" prop="status">
            <el-input v-model="resume.status" placeholder="请输入求职状态" />
          </el-form-item>
          
          <el-form-item label="技能" prop="skills">
            <el-select
              v-model="resume.skills"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请输入技能（可多选）"
            >
              <el-option
                v-for="skill in resume.skills"
                :key="skill"
                :label="skill"
                :value="skill"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="荣誉" prop="honors">
            <el-select
              v-model="resume.honors"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请输入荣誉（可多选）"
            >
              <el-option
                v-for="honor in resume.honors"
                :key="honor"
                :label="honor"
                :value="honor"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="简历链接" prop="resumeUrl">
            <el-input v-model="resume.resumeUrl" placeholder="请输入简历链接" />
          </el-form-item>
          
          <el-form-item label="项目链接" prop="projectUrl">
            <el-input v-model="resume.projectUrl" placeholder="请输入项目链接" />
          </el-form-item>
          
          <el-form-item label="学校" prop="schoolName">
            <el-input v-model="resume.schoolName" placeholder="请输入学校名称" />
          </el-form-item>
          
          <el-form-item label="专业" prop="major">
            <el-input v-model="resume.major" placeholder="请输入专业名称" />
          </el-form-item>
          
          <el-form-item label="学历" prop="degree">
            <el-input v-model="resume.degree" placeholder="请输入学历" />
          </el-form-item>
          
          <el-form-item label="入学时间" prop="startDate">
            <el-date-picker
              v-model="resume.startDate"
              type="date"
              placeholder="选择入学时间"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="毕业时间" prop="graduateDate">
            <el-date-picker
              v-model="resume.graduateDate"
              type="date"
              placeholder="选择毕业时间"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存</el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </el-form-item>
        </el-form>
      </template>
      
      <el-empty v-else description="暂无简介信息" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { resumeApi } from '../../apis/resume'

interface Resume {
  id?: number | string,
  name: string
  avatar: string
  email: string
  introduction: string
  jobIntention: string
  status: string 
  skills: string[]
  honors: string[]
  resumeUrl: string
  projectUrl: string
  schoolName: string
  major: string
  degree: string
  startDate: string
  graduateDate: string
  createTime?: string
  updateTime?: string
}

const loading = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const resume = ref<Resume>({
  id: undefined,
  name: '',
  avatar: '',
  email: '',
  introduction: '',
  jobIntention: '',
  skills: [],
  honors: [],
  resumeUrl: '',
  projectUrl: '',
  schoolName: '',
  major: '',
  degree: '',
  startDate: '',
  graduateDate: ''
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  introduction: [{ required: true, message: '请输入个人介绍', trigger: 'blur' }],
  jobIntention: [{ required: true, message: '请输入求职意向', trigger: 'blur' }],
  schoolName: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  degree: [{ required: true, message: '请输入学历', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择入学时间', trigger: 'change' }],
  graduateDate: [{ required: true, message: '请选择毕业时间', trigger: 'change' }]
})

// 获取简介信息
const getResume = async () => {
  loading.value = true
  try {
    const { data } = await resumeApi.getResume()
    if (data) {
      resume.value = data
    } else {
      // 如果没有数据，将 id 设置为 undefined，而不是清空整个表单
      resume.value = {
        id: undefined,
        name: '',
        avatar: '',
        email: '',
        introduction: '',
        jobIntention: '',
        skills: [],
        honors: [],
        resumeUrl: '',
        projectUrl: '',
        schoolName: '',
        major: '',
        degree: '',
        startDate: '',
        graduateDate: ''
      }
    }
  } catch (error) {
    console.error('获取简历失败:', error)
    // 发生错误时，保持当前表单状态，不清空数据
  } finally {
    loading.value = false
  }
}

// 创建简介
const handleCreate = () => {
  resume.value.id=1
}

// 保存简介
const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (resume.value.id) {
          // 更新
          await resumeApi.updateResume(resume.value)
          ElMessage.success('更新成功')
        } else {
          // 创建
          const { data } = await resumeApi.createResume(resume.value)
          resume.value = data // 直接使用返回的数据更新表单
          ElMessage.success('创建成功')
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 删除简介
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除简介吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    loading.value = true
    await resumeApi.deleteResume()
    ElMessage.success('删除成功')
    resume.value = {
      name: '',
      avatar: '',
      email: '',
      introduction: '',
      jobIntention: '',
      skills: [],
      honors: [],
      resumeUrl: '',
      projectUrl: '',
      schoolName: '',
      major: '',
      degree: '',
      startDate: '',
      graduateDate: ''
    }
  } catch (error: any) {
    if (error?.message !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getResume()
})
</script>

<style scoped>
.resume-manage {
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2f3d;
}

.content {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .content {
    background: #1a1a1a;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  }

  .page-title {
    color: #e5e5e5;
  }
}
</style> 