package com.eit.hoppy.logtail;

/**
 * @author Hlingoes 2022/8/20
 */
public enum FileEventEnum {
    /**
     * 新增
     */
    CREATE,
    /**
     * 文件滚动
     */
    CREATE_ROTATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 修改
     */
    MODIFY

}
