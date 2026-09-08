/* 通用类型定义：分页结果等跨模块复用类型 */

/** 分页查询通用返回结构 */
export interface PageResult<T> {
  total: number  // 总记录数
  list: T[]      // 当前页数据列表
}
