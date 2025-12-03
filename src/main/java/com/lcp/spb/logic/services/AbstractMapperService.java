package com.lcp.spb.logic.services;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMapperService extends BaseService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected PageBounds normalizePage (long page, long pageSize) {
        var safePage = Math.max(page, NumberUtils.LONG_ONE);
        var safePageSize = Math.max(pageSize, NumberUtils.LONG_ONE);
        var offset = (safePage - NumberUtils.LONG_ONE) * safePageSize;
        return new PageBounds(safePage, safePageSize, offset);
    }

    protected static final class PageBounds {
        private final long page;
        private final long pageSize;
        private final long offset;

        PageBounds(long page, long pageSize, long offset) {
            this.page = page;
            this.pageSize = pageSize;
            this.offset = offset;
        }

        public long getPage () {
            return page;
        }

        public long getPageSize () {
            return pageSize;
        }

        public long getOffset () {
            return offset;
        }
    }

    // 子类直接复用持久层 mapper
}
