<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check, CircleCheck, DocumentChecked, Download, InfoFilled,
  Link, Lock, RefreshLeft, School, WarningFilled
} from '@element-plus/icons-vue'
import { calculateReadiness, competitionChecks } from '@/utils/competitionReadiness'

const STORAGE_KEY = 'chainpass_competition_readiness_v1'
const officialNotice = 'http://www.moe.gov.cn/srcsite/A08/s5672/202607/t20260731_1445670.html'

type Profile = {
  projectName: string
  schoolName: string
  track: string
  category: string
  group: string
  evidenceIndex: string
}

const profile = ref<Profile>({
  projectName: 'ChainPass 跨境数字身份与合规支付解决方案',
  schoolName: '大连理工大学',
  track: '高教主赛道',
  category: '新文科类（金融科技 / 数字经济）',
  group: '',
  evidenceIndex: '建议填写：代码提交、测试报告、访谈记录、授权文件、数据来源、成员贡献表等证据的位置。',
})
const checks = ref<Record<string, boolean>>({})
const lastSavedAt = ref('')

const groups = computed(() => [...new Set(competitionChecks.map(item => item.group))])
const readiness = computed(() => calculateReadiness(checks.value))
const statusLabel = computed(() => readiness.value.ready ? '自检项已全部确认' : `仍有 ${readiness.value.blockers.length} 个阻断项`)

const productFacts = [
  '当前支付功能是内部多币种沙盒账本，不连接银行、卡组织或真实资金通道。',
  '当前 KYC 是本系统授权审核员的审核结论，不等同于持牌机构或政府部门认证。',
  'did:chainpass 是本项目本地标识方法；实现受 DID Core 概念启发，但不宣称通过标准认证。',
  '已实跑注册、DID、KYC、凭证、合规预检、原子记账和交易历史，并保留截图与测试证据。',
]

function save() {
  const savedAt = new Date().toISOString()
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ profile: profile.value, checks: checks.value, savedAt }))
  lastSavedAt.value = savedAt
}

function toggle(id: string) {
  checks.value[id] = !checks.value[id]
}

async function reset() {
  await ElMessageBox.confirm('将清空本机保存的所有自检勾选，是否继续？', '重置自检', {
    confirmButtonText: '确认重置',
    cancelButtonText: '取消',
    type: 'warning',
  })
  checks.value = {}
  save()
  ElMessage.success('已重置自检状态')
}

function exportEvidence() {
  const payload = {
    title: 'ChainPass 中国国际大学生创新大赛（2026）申报自检记录',
    notice: officialNotice,
    generatedAt: new Date().toISOString(),
    warning: '本记录是团队内部自检，不代表学校、省级教育行政部门或大赛组委会审核结论。',
    project: profile.value,
    productFacts,
    readiness: {
      percent: readiness.value.percent,
      ready: readiness.value.ready,
      remainingCriticalItems: readiness.value.blockers.map(item => item.title),
    },
    declarations: competitionChecks.map(item => ({
      ...item,
      confirmed: Boolean(checks.value[item.id]),
    })),
  }
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `chainpass-competition-readiness-${new Date().toISOString().slice(0, 10)}.json`
  anchor.click()
  URL.revokeObjectURL(url)
  ElMessage.success('自检证据清单已导出')
}

onMounted(() => {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (!stored) return
  try {
    const parsed = JSON.parse(stored)
    profile.value = { ...profile.value, ...(parsed.profile || {}) }
    checks.value = parsed.checks || {}
    lastSavedAt.value = parsed.savedAt || ''
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
})

watch([profile, checks], save, { deep: true })
</script>

<template>
  <div class="readiness-page">
    <section class="policy-hero">
      <div class="hero-copy">
        <div class="hero-mark"><DocumentChecked /></div>
        <div>
          <p class="context-label">中国国际大学生创新大赛（2026）</p>
          <h1>参赛真实性与申报合规自检</h1>
          <p class="hero-description">
            将教育部通知、高教主赛道方案、赛事工作规范和参赛学生“十不准”转化为可保存、可导出的项目自检清单。
          </p>
        </div>
      </div>
      <a class="official-link" :href="officialNotice" target="_blank" rel="noopener noreferrer">
        <Link /> 查看教育部原文
      </a>
    </section>

    <div class="notice-banner" role="note">
      <InfoFilled />
      <span>这是团队内部预检工具，不代替学校审核、省级复核或大赛组委会认定；勾选前必须有可追溯证据。</span>
    </div>

    <section class="summary-grid">
      <article class="summary-card readiness-card">
        <div class="summary-title">
          <span>申报就绪度</span>
          <strong>{{ readiness.percent }}%</strong>
        </div>
        <el-progress :percentage="readiness.percent" :stroke-width="8" :show-text="false" />
        <p :class="['readiness-status', { ready: readiness.ready }]">
          <CircleCheck v-if="readiness.ready" />
          <WarningFilled v-else />
          {{ statusLabel }}
        </p>
      </article>
      <article class="summary-card">
        <span class="summary-kicker">当前建议定位</span>
        <strong>高教主赛道 · 新文科类</strong>
        <p>金融科技与数字经济方向。若团队以底层区块链/网络安全技术为核心，可改选新工科，但正式报名只选一个类别。</p>
      </article>
      <article class="summary-card">
        <span class="summary-kicker">保存方式</span>
        <strong>本机浏览器自动保存</strong>
        <p>不把额外的学籍、年龄和申报材料上传到服务器。正式申报前导出记录并由团队、指导教师和学校复核。</p>
      </article>
    </section>

    <section class="content-card profile-card">
      <div class="section-heading">
        <div>
          <span class="section-icon"><School /></span>
          <div>
            <h2>项目申报定位</h2>
            <p>先固定唯一赛道、类别和组别，避免跨口径包装。</p>
          </div>
        </div>
      </div>
      <div class="profile-grid">
        <label>
          <span>项目名称</span>
          <el-input v-model="profile.projectName" />
        </label>
        <label>
          <span>代表学校</span>
          <el-input v-model="profile.schoolName" />
        </label>
        <label>
          <span>赛道</span>
          <el-select v-model="profile.track">
            <el-option label="高教主赛道" value="高教主赛道" />
          </el-select>
        </label>
        <label>
          <span>项目类别</span>
          <el-select v-model="profile.category">
            <el-option label="新文科类（金融科技 / 数字经济）" value="新文科类（金融科技 / 数字经济）" />
            <el-option label="新工科类（区块链 / 网络空间安全）" value="新工科类（区块链 / 网络空间安全）" />
            <el-option label="人工智能+" value="人工智能+" />
          </el-select>
        </label>
        <label>
          <span>组别（必须据实选择）</span>
          <el-select v-model="profile.group" placeholder="请按学籍与工商登记状态选择">
            <el-option label="本科生创意组" value="本科生创意组" />
            <el-option label="本科生创业组" value="本科生创业组" />
            <el-option label="研究生创意组" value="研究生创意组" />
            <el-option label="研究生创业组" value="研究生创业组" />
          </el-select>
        </label>
      </div>
    </section>

    <section class="content-card fact-card">
      <div class="section-heading compact">
        <div>
          <span class="section-icon"><Lock /></span>
          <div>
            <h2>必须坚持的产品事实边界</h2>
            <p>这些边界应原样进入项目书、路演和答辩风险说明。</p>
          </div>
        </div>
      </div>
      <ul class="fact-list">
        <li v-for="fact in productFacts" :key="fact"><Check />{{ fact }}</li>
      </ul>
    </section>

    <section class="checklist-layout">
      <div v-for="group in groups" :key="group" class="content-card checklist-group">
        <div class="section-heading compact">
          <div><h2>{{ group }}</h2></div>
          <span>{{ competitionChecks.filter(item => item.group === group && checks[item.id]).length }} / {{ competitionChecks.filter(item => item.group === group).length }}</span>
        </div>
        <button
          v-for="item in competitionChecks.filter(check => check.group === group)"
          :key="item.id"
          type="button"
          :class="['check-item', { confirmed: checks[item.id] }]"
          :aria-pressed="Boolean(checks[item.id])"
          @click="toggle(item.id)"
        >
          <span class="check-box"><Check v-if="checks[item.id]" /></span>
          <span class="check-copy">
            <span class="check-title">{{ item.title }} <em>一票否决项</em></span>
            <span class="check-description">{{ item.description }}</span>
            <span class="check-source">依据：{{ item.source }}</span>
          </span>
        </button>
      </div>
    </section>

    <section class="content-card evidence-card">
      <div class="section-heading compact">
        <div>
          <h2>证据索引与未完成项</h2>
          <p>不要只写“已完成”，应写明文件、提交记录、测试命令、访谈编号或授权证明的位置。</p>
        </div>
      </div>
      <el-input v-model="profile.evidenceIndex" type="textarea" :rows="6" maxlength="3000" show-word-limit />
    </section>

    <div class="action-bar">
      <div>
        <strong>{{ statusLabel }}</strong>
        <span v-if="lastSavedAt">最近保存：{{ new Date(lastSavedAt).toLocaleString('zh-CN') }}</span>
      </div>
      <div class="action-buttons">
        <el-button @click="reset"><RefreshLeft />重置</el-button>
        <el-button type="primary" @click="exportEvidence"><Download />导出自检记录</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.readiness-page {
  width: min(1240px, 100%);
  margin: 0 auto;
  color: #0a2540;
}

.policy-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 32px;
  padding: 36px;
  border: 1px solid rgba(99, 91, 255, 0.16);
  border-radius: 16px;
  background-color: #fff;
  background-image: linear-gradient(to right, rgba(99, 91, 255, 0.08) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(99, 91, 255, 0.08) 1px, transparent 1px);
  background-size: 40px 40px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04), 0 8px 16px rgba(0, 0, 0, 0.08);
}

.hero-copy { display: flex; gap: 18px; max-width: 780px; }
.hero-mark, .section-icon {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 48px;
  height: 48px;
  color: #fff;
  background: #635bff;
  border-radius: 12px;
  box-shadow: 0 2px 5px rgba(99, 91, 255, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.2);
}
.context-label { margin-bottom: 7px; color: #635bff; font-weight: 700; }
h1 { margin: 0; font-size: clamp(28px, 4vw, 42px); line-height: 1.15; letter-spacing: -0.03em; }
.hero-description { max-width: 70ch; margin-top: 14px; color: #425466; line-height: 1.75; }
.official-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  color: #fff;
  background: #635bff;
  border-radius: 8px;
  font-weight: 650;
  white-space: nowrap;
  box-shadow: 0 2px 5px rgba(99, 91, 255, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: transform 300ms ease-out, box-shadow 300ms ease-out;
}
.official-link:hover { color: #fff; transform: translateY(-2px); }
.official-link:active { transform: scale(.98); box-shadow: inset 0 2px 4px rgba(0, 0, 0, .2); }

.notice-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 18px 0;
  padding: 13px 16px;
  color: #3c4257;
  background: #f7f5ff;
  border: 1px solid #d9d5ff;
  border-radius: 10px;
  font-size: 14px;
}
.notice-banner svg { flex: 0 0 18px; color: #635bff; }

.summary-grid { display: grid; grid-template-columns: 1.05fr 1fr 1fr; gap: 18px; margin-bottom: 18px; }
.summary-card, .content-card {
  background: #fff;
  border: 1px solid #e6ebf1;
  border-radius: 14px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .04), 0 8px 16px rgba(0, 0, 0, .06);
}
.summary-card { padding: 22px; transition: transform 400ms ease-out, box-shadow 400ms ease-out; }
.summary-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(10, 37, 64, .08); }
.summary-card strong { display: block; margin: 7px 0; font-size: 18px; }
.summary-card p { color: #526579; font-size: 13px; line-height: 1.65; }
.summary-title { display: flex; justify-content: space-between; margin-bottom: 13px; }
.summary-title strong { margin: 0; color: #635bff; font-size: 26px; }
.summary-kicker { color: #697386; font-size: 12px; font-weight: 700; }
.readiness-status { display: flex; align-items: center; gap: 7px; margin-top: 12px !important; color: #9b3d31 !important; font-weight: 650; }
.readiness-status.ready { color: #147d64 !important; }
.readiness-status svg { width: 17px; }

.content-card { margin-bottom: 18px; padding: 26px; }
.section-heading, .section-heading > div { display: flex; align-items: center; gap: 14px; }
.section-heading { justify-content: space-between; margin-bottom: 22px; }
.section-heading.compact { margin-bottom: 16px; }
.section-heading h2 { margin: 0; font-size: 19px; }
.section-heading p { margin-top: 4px; color: #697386; font-size: 13px; }
.section-heading > span { color: #635bff; font-weight: 750; }
.section-icon { width: 42px; height: 42px; }
.profile-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.profile-grid label { display: grid; gap: 8px; color: #34465a; font-size: 13px; font-weight: 650; }
.profile-grid label:last-child { grid-column: span 2; }

.fact-card { background: #0a2540; color: #fff; border-color: #0a2540; }
.fact-card .section-heading p { color: #c8d6e5; }
.fact-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px 22px; list-style: none; }
.fact-list li { display: flex; align-items: flex-start; gap: 9px; color: #e7eef6; font-size: 14px; line-height: 1.6; }
.fact-list svg { flex: 0 0 17px; margin-top: 3px; color: #80e9ff; }

.checklist-layout { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.checklist-group { margin: 0; }
.check-item {
  display: flex;
  width: 100%;
  gap: 13px;
  padding: 15px 0;
  color: inherit;
  text-align: left;
  background: none;
  border: 0;
  border-top: 1px solid #edf1f5;
  cursor: pointer;
}
.check-item:first-of-type { border-top: 0; }
.check-box {
  display: grid;
  place-items: center;
  flex: 0 0 22px;
  width: 22px;
  height: 22px;
  margin-top: 1px;
  color: #fff;
  border: 1px solid #b8c2ce;
  border-radius: 6px;
  transition: background-color 200ms ease-out, border-color 200ms ease-out;
}
.check-box svg { width: 14px; }
.check-item.confirmed .check-box { background: #635bff; border-color: #635bff; }
.check-copy { display: grid; gap: 5px; }
.check-title { font-weight: 700; }
.check-title em { margin-left: 6px; color: #9b3d31; font-size: 11px; font-style: normal; }
.check-description { color: #526579; font-size: 13px; line-height: 1.55; }
.check-source { color: #8795a5; font-size: 11px; }
.check-item:focus-visible { outline: 3px solid rgba(99, 91, 255, .3); outline-offset: 4px; border-radius: 8px; }
.check-item:hover .check-title { color: #635bff; }

.evidence-card { margin-top: 18px; }
.action-bar {
  position: sticky;
  bottom: 16px;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 18px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, .96);
  border: 1px solid #dfe6ee;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, .06), 0 16px 32px rgba(0, 0, 0, .1);
}
.action-bar > div:first-child { display: grid; gap: 3px; }
.action-bar span { color: #697386; font-size: 12px; }
.action-buttons { display: flex; gap: 10px; }

@media (max-width: 900px) {
  .summary-grid, .checklist-layout { grid-template-columns: 1fr; }
  .policy-hero { flex-direction: column; padding: 26px; }
  .fact-list { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .profile-grid { grid-template-columns: 1fr; }
  .profile-grid label:last-child { grid-column: auto; }
  .hero-copy { flex-direction: column; }
  .content-card { padding: 20px; }
  .action-bar { position: static; align-items: stretch; flex-direction: column; }
  .action-buttons { display: grid; grid-template-columns: 1fr 1fr; }
}
@media (prefers-reduced-motion: reduce) {
  .summary-card, .official-link, .check-box { transition: none; }
  .summary-card:hover, .official-link:hover { transform: none; }
}
</style>
