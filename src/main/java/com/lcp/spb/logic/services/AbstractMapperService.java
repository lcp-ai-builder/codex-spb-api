package com.lcp.spb.logic.services;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * 抽象映射服务基类
 * 
 * <p>提供分页参数标准化功能，用于需要分页查询的服务类。
 * 该类主要用于处理分页参数的校验和计算，确保分页参数的有效性。
 * 
 * <p>功能特性：
 * <ul>
 *   <li>分页参数标准化：确保页码和每页大小都是有效值（至少为1）</li>
 *   <li>偏移量计算：根据页码和每页大小自动计算查询偏移量</li>
 *   <li>分页边界封装：通过内部类 PageBounds 封装分页相关信息</li>
 * </ul>
 * 
 * <p>使用场景：
 * <ul>
 *   <li>需要分页查询的服务类可以继承此类</li>
 *   <li>通过 {@link #normalizePage(long, long)} 方法标准化分页参数</li>
 *   <li>使用返回的 PageBounds 对象进行数据库或 Elasticsearch 查询</li>
 * </ul>
 * 
 * @author lcp
 */
public abstract class AbstractMapperService extends BaseService {

    /**
     * 标准化分页参数
     * 
     * <p>对传入的分页参数进行校验和标准化处理：
     * <ul>
     *   <li>页码（page）：如果小于1，则设置为1</li>
     *   <li>每页大小（pageSize）：如果小于1，则设置为1</li>
     *   <li>偏移量（offset）：根据标准化后的页码和每页大小自动计算</li>
     * </ul>
     * 
     * <p>计算公式：
     * <pre>
     * offset = (page - 1) * pageSize
     * </pre>
     * 
     * @param page 页码，从1开始，如果小于1则自动设置为1
     * @param pageSize 每页记录数，如果小于1则自动设置为1
     * @return PageBounds 对象，包含标准化后的页码、每页大小和偏移量
     */
    protected PageBounds normalizePage (long page, long pageSize) {
        var safePage = Math.max(page, NumberUtils.LONG_ONE);
        var safePageSize = Math.max(pageSize, NumberUtils.LONG_ONE);
        var offset = (safePage - NumberUtils.LONG_ONE) * safePageSize;
        return new PageBounds(safePage, safePageSize, offset);
    }

    /**
     * 分页边界信息封装类
     * 
     * <p>用于封装分页查询所需的所有参数，包括页码、每页大小和偏移量。
     * 这是一个不可变的内部类，确保分页参数的一致性。
     */
    protected static final class PageBounds {
        /** 页码，从1开始 */
        private final long page;
        /** 每页记录数 */
        private final long pageSize;
        /** 查询偏移量，用于跳过前面的记录 */
        private final long offset;

        /**
         * 构造函数
         * 
         * @param page 页码，从1开始
         * @param pageSize 每页记录数
         * @param offset 查询偏移量
         */
        PageBounds(long page, long pageSize, long offset) {
            this.page = page;
            this.pageSize = pageSize;
            this.offset = offset;
        }

        /**
         * 获取页码
         * 
         * @return 页码，从1开始
         */
        public long getPage () {
            return page;
        }

        /**
         * 获取每页记录数
         * 
         * @return 每页记录数
         */
        public long getPageSize () {
            return pageSize;
        }

        /**
         * 获取查询偏移量
         * 
         * @return 查询偏移量，表示需要跳过的记录数
         */
        public long getOffset () {
            return offset;
        }
    }
}
