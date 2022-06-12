package com.eit.hoppy.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * description:
 *
 * @author Hlingoes
 * @date 2022/6/12 14:33
 * @citation https://www.jianshu.com/p/af4b3264bc5d
 */
public class StreamGobbler extends Thread {
    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

    private InputStream inputStream;
    private String streamType;
    private StringBuilder buf;
    private int len = 0;

    private static final int BUFFER_SIZE = 1024;

    /**
     * @param inputStream the InputStream to be consumed
     * @param streamType  the stream type (should be OUTPUT or ERROR)
     */
    public StreamGobbler(final InputStream inputStream, final String streamType) {
        this.inputStream = inputStream;
        this.streamType = streamType;
        this.buf = new StringBuilder();
    }

    /**
     * Consumes the output from the input stream and displays the lines consumed
     * if configured to do so.
     */
    @Override
    public void run() {
        try {
            // 默认编码为UTF-8，这里设置编码为GBK，因为WINDOWS的编码为GBK
            byte[] bytes = new byte[BUFFER_SIZE];
            while ((len = this.inputStream.read(bytes)) != -1) {
                this.buf.append(new String(bytes, 0, len, Charset.forName("GBK")));
            }
        } catch (IOException ex) {
            logger.trace("Failed to successfully consume and display the input stream of type {}", streamType, ex);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("close inputStream error", e);
                }
            }
        }
    }

    /**
     * description: 读取流的内容
     *
     * @param
     * @return java.lang.String
     * @author Hlingoes 2022/6/12
     */
    public String readContent() {
        return this.buf.toString().trim();
    }

}
