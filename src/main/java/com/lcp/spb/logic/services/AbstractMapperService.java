package com.lcp.spb.logic.services;

import com.lcp.spb.logic.dao.LoginMapper;
import com.lcp.spb.logic.dao.OperatorMapper;
import com.lcp.spb.logic.dao.RoleMapper;
import com.lcp.spb.logic.dao.UserMapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMapperService extends BaseService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected LoginMapper loginMapper;
    @Autowired
    protected OperatorMapper operatorMapper;
    @Autowired
    protected RoleMapper roleMapper;
    @Autowired
    protected UserMapper userMapper;

    protected PageBounds normalizePage(long page, long pageSize) {
        long safePage = Math.max(page, NumberUtils.LONG_ONE);
        long safePageSize = Math.max(pageSize, NumberUtils.LONG_ONE);
        long offset = (safePage - NumberUtils.LONG_ONE) * safePageSize;
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

        public long getPage() {
            return page;
        }

        public long getPageSize() {
            return pageSize;
        }

        public long getOffset() {
            return offset;
        }
    }

    // 子类直接复用持久层 mapper
}
