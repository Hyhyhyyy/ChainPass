export type CompetitionCheck = {
  id: string
  group: '参赛资格' | '真实性与贡献' | '知识产权与数据' | '材料与评审纪律'
  title: string
  description: string
  critical: boolean
  source: string
}

export const competitionChecks: CompetitionCheck[] = [
  {
    id: 'age',
    group: '参赛资格',
    title: '全体参赛人员年龄符合要求',
    description: '所有参赛人员均为 1991 年 3 月 1 日及以后出生。',
    critical: true,
    source: '通知第六条第（三）项',
  },
  {
    id: 'team-size',
    group: '参赛资格',
    title: '团队人数为 3-15 人',
    description: '人数包含团队负责人，且所有成员均为实际核心成员。',
    critical: true,
    source: '高教主赛道方案第二条',
  },
  {
    id: 'student-status',
    group: '参赛资格',
    title: '学籍、学历和组别匹配',
    description: '按 2026 年 4 月 30 日学籍或最高学历确定学校与组别，不以在职教育身份申报。',
    critical: true,
    source: '通知第六条第（四）项、高教主赛道方案第三条',
  },
  {
    id: 'teacher-boundary',
    group: '参赛资格',
    title: '教师仅指导、不作为团队成员',
    description: '学校教师不得作为项目团队成员，教师贡献与学生贡献分别如实记录。',
    critical: true,
    source: '通知第六条第（三）项、指导教师“十不准”',
  },
  {
    id: 'single-entry',
    group: '参赛资格',
    title: '只选择一个赛道和一个项目',
    description: '每名学生在一个赛道最多参与一个项目；项目只选择一个符合要求的赛道。',
    critical: true,
    source: '通知第六条第（四）项',
  },
  {
    id: 'not-previous-winner',
    group: '参赛资格',
    title: '不是往年总决赛获奖项目的重复包装',
    description: '若为延续项目，已经形成可证明的实质性创新突破，而非改名或更换负责人。',
    critical: true,
    source: '通知第六条第（四）项、学生“十不准”第一条',
  },
  {
    id: 'real-contribution',
    group: '真实性与贡献',
    title: '成员身份、角色和工作量真实',
    description: '不存在挂名、虚报贡献或买卖参赛席位；每项核心产出可追溯到实际完成人。',
    critical: true,
    source: '学生“十不准”第二、七条',
  },
  {
    id: 'truthful-claims',
    group: '真实性与贡献',
    title: '技术成熟度和商业陈述不夸大',
    description: '明确区分已实现、已验证、待验证和未来计划，并披露重大风险与限制。',
    critical: true,
    source: '通知第六条第（二）项、学生“十不准”第六条',
  },
  {
    id: 'ip-clear',
    group: '知识产权与数据',
    title: '知识产权和第三方资源权属清晰',
    description: '代码、Logo、数据、专利、图片和依赖均为原创、开源许可允许或已获合法授权。',
    critical: true,
    source: '通知第六条第（二）、（五）项、学生“十不准”第三条',
  },
  {
    id: 'evidence-traceable',
    group: '知识产权与数据',
    title: '数据与证明材料真实、准确、可追溯',
    description: '用户数据、技术参数、财务预测、检测报告和荣誉证明均保留来源与验证方法。',
    critical: true,
    source: '学生“十不准”第四条',
  },
  {
    id: 'privacy',
    group: '知识产权与数据',
    title: '涉密与个人敏感信息已脱敏',
    description: '展示材料不包含真实证件号、密钥、口令、生产数据或未经授权的个人信息。',
    critical: true,
    source: '通知第六条第（二）项',
  },
  {
    id: 'student-authored',
    group: '材料与评审纪律',
    title: '核心参赛材料由学生团队独立完成',
    description: '项目书、技术文档、PPT 和演示内容没有商业外包、代写或代做。',
    critical: true,
    source: '工作规范第三条、学生“十不准”第五条',
  },
  {
    id: 'no-award-service',
    group: '材料与评审纪律',
    title: '未购买“保奖”或违规培训服务',
    description: '未接受自称评委、内部人士或中介提供的保奖、违规辅导和参赛资格交易。',
    critical: true,
    source: '学生“十不准”第八、九条',
  },
  {
    id: 'no-interference',
    group: '材料与评审纪律',
    title: '不联系、不请托、不干扰评审',
    description: '不打探专家信息，不宴请、馈赠、利益输送或以任何方式影响独立评审。',
    critical: true,
    source: '学生“十不准”第十条',
  },
]

export function calculateReadiness(checks: Record<string, boolean>) {
  const completed = competitionChecks.filter(item => checks[item.id]).length
  const total = competitionChecks.length
  const blockers = competitionChecks.filter(item => item.critical && !checks[item.id])
  return {
    completed,
    total,
    percent: total === 0 ? 0 : Math.round((completed / total) * 100),
    blockers,
    ready: blockers.length === 0,
  }
}

