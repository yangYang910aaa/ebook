import request from './request'

export interface StatisticResp {
  totalViewCount: number
  totalVoteCount: number
  voteRate: number
  todayViewCount: number
  todayVoteCount: number
  yesterdayViewCount: number
  estimatedTodayView: number
  estimatedGrowth: number
}

export interface DailyStat {
  date: string
  viewIncrease: number
  voteIncrease: number
}

export function getStatistic() {
  return request.get<unknown, StatisticResp>('/ebook-snapshot/getStatistic')
}

export function get30Statistic() {
  return request.get<unknown, DailyStat[]>('/ebook-snapshot/get30Statistic')
}
