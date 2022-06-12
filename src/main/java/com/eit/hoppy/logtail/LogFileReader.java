package com.eit.hoppy.logtail;

/**
 * description: 读取日志
 *
 * @author Hlingoes
 * @date 2022/6/11 21:58
 */
public class LogFileReader {
    /**
     * 用于标识该文件是否被删除
     */
    private Boolean deleteFlag;
    /**
     * 文件指针,getFilePointer 获取RandomAccessFile流中的当前指针
     */
    private long filePtr;
    /**
     * 当前日志解析进度
     */
    private long readOffset;
    /**
     * 发送日志内容
     */
    private Sender sender;

}
