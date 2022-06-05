package com.eit.hoppy.core;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-17.
 */
public interface EventHandle {

    /**
     * description: 处理文件事件
     *
     * @param fileMeta
     * @return void
     * @author Hlingoes 2022/5/31
     */
    void handleFile(FileMeta fileMeta);
}
