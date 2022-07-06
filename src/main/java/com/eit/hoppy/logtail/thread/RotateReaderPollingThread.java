package com.eit.hoppy.logtail.thread;

import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.LogFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * description:
 *
 * @author Hlingoes
 * @date 2022/6/19 19:18
 */
public class RotateReaderPollingThread extends AbstractPollingThread {
    private static Logger logger = LoggerFactory.getLogger(RotateReaderPollingThread.class);

    public RotateReaderPollingThread() {
        super(RotateReaderPollingThread.class.getSimpleName(), 5 * 60 * 1000L);
    }

    public RotateReaderPollingThread(long period) {
        super(RotateReaderPollingThread.class.getSimpleName(), period);
    }

    @Override
    void polling() {
        Map<String, LogFileReader> rotateLogFileReaderMap = CacheManager.getRotateLogFileReaderMap();
        Iterator<Map.Entry<String, LogFileReader>> entries = rotateLogFileReaderMap.entrySet().iterator();
        LogFileReader logFileReader;
        while (entries.hasNext()) {
            logFileReader = entries.next().getValue();
            // 一定时间(period)内该Reader没有处理过Modify事件且日志解析完毕则删除该Reader
            if (System.currentTimeMillis() > (super.getPeriod() + logFileReader.getLogMeta().getLastUpdateTime())
                    && logFileReader.finishReading()) {
                entries.remove();
            }
        }
    }

}
