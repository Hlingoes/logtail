package com.eit.hoppy.core;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5.
 * @citation Created by jaren.han on 2020-01-17.
 */
public interface FileEventReceive {

    /**
     * description: 轮询方式获取文件信息
     *
     * @param fileMeta
     * @return void
     * @author Hlingoes 2022/6/5
     */
    void receiveWatch(FileMeta fileMeta);

    /**
     * description: inotify事件机制的方式获取文件信息
     *
     * @param fileMeta
     * @return void
     * @author Hlingoes 2022/6/5
     */
    void receiveInotify(FileMeta fileMeta);
}
