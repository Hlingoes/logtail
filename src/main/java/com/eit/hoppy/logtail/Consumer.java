package com.eit.hoppy.logtail;

/**
 * description: 接收，消费数据
 *
 * @author Hlingoes 2022/6/11
 */
public interface Consumer {

    /**
     * description: 接收，消费数据
     *
     * @param data
     * @return boolean
     * @author Hlingoes 2022/6/11
     */
    boolean consumer(Object data);
}
