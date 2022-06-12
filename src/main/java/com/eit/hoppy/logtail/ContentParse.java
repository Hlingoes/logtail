package com.eit.hoppy.logtail;

/**
 * description: 日子内容解析
 *
 * @author Hlingoes 2022/6/11
 */
public interface ContentParse<T> {

    /**
     * description: 日志解析
     *
     * @param content
     * @return T
     * @author Hlingoes 2022/6/11
     */
    T parse(String content);
}
