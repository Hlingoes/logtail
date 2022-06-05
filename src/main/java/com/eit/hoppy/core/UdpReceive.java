package com.eit.hoppy.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 *
 * @author Hlingoes 2022/6/5
 * @citation Created by jaren.han on 2020-01-10.
 */
public class UdpReceive implements ReceiveDataChannel {

    static Logger logger = LoggerFactory.getLogger("receiveLog");
    private String content;

    @Override
    public void receiveData(String content) {
        this.content = content;
        logger.info("receive:{}", content);
    }
}
