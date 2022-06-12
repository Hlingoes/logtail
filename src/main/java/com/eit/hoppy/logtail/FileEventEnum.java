package com.eit.hoppy.logtail;

/**
 * description: 文件的事件类型
 *
 * @author Hlingoes
 * @date 2022/6/11 20:41
 */
public enum FileEventEnum {
    /**
     * 新建文件
     */
    CREATE,
    /**
     * 文件修改
     */
    MODIFY,
    /**
     * 文件删除
     */
    DELETE,
}
