/* 统计 API：首页展示的平台阅读/点赞汇总数据及近 30 天趋势 */
import request from './request'

/** 平台统计汇总数据 */
export interface StatisticResp {
  totalViewCount: number      // 累计阅读量
  totalVoteCount: number      // 累计点赞量
  voteRate: number            // 点赞率（点赞/阅读，百分比）
  todayViewCount: number      // 今日阅读量
  todayVoteCount: number      // 今日点赞量
  yesterdayViewCount: number  // 昨日阅读量（用于推算增长率）
  estimatedTodayView: number  // 预计今日阅读量（按当前进度推算）
  estimatedGrowth: number     // 预计阅读增长率（对比昨日，正负均可）
}

/** 单日统计增量 */
export interface DailyStat {
  date: string         // 日期（YYYY-MM-DD）
  viewIncrease: number // 当日阅读增量
  voteIncrease: number // 当日点赞增量
}

/** 获取平台统计汇总数据 */
export function getStatistic() {
  return request.get<unknown, StatisticResp>('/ebook-snapshot/getStatistic')
}

/** 获取近 30 天每日统计增量（趋势图用） */
export function get30Statistic() {
  return request.get<unknown, DailyStat[]>('/ebook-snapshot/get30Statistic')
}
