package com.eit.hoppy.logtail;

/**
 * description: 发送器，发送解析之后的数据
 *
 * @author Hlingoes 2022/6/11
 */
public interface Sender {

    /**
     * description: 发送数据
     *
     * @param data
     * @return boolean
     * @author Hlingoes 2022/6/11
     */
    boolean send(Object data);
}
