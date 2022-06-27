import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by jaren.han on 2020-01-08.
 */
public class FileTailTest {
    private static final Logger logger = LoggerFactory.getLogger(FileTailTest.class);

    @Test
    public void getCurrentLogTest() {
        Path path = Paths.get("E:\\hulin_workspace\\logtail\\pom.xml");
        BasicFileAttributes bfa = null;
        try {
            bfa = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Creation Time      : " + bfa.creationTime());
        logger.info("Last Access Time   : " + bfa.lastAccessTime());
        logger.info("Last Modified Time : " + bfa.lastModifiedTime());
        logger.info("Is Directory       : " + bfa.isDirectory());
        logger.info("Is Other           : " + bfa.isOther());
        logger.info("Is Regular File    : " + bfa.isRegularFile());
        logger.info("Is Symbolic Link   : " + bfa.isSymbolicLink());
        logger.info("Size               : " + bfa.size());
        Object objectKey = bfa.fileKey();
        logger.info("Object Key               : " + bfa.fileKey());
    }

    @Test
    public void writeFile() {
        String className = getClass().getCanonicalName();
        for (int i = 1; i < 10000; i++) {
            logger.info(className + className + className + className + i);
        }
    }

    @Test
    public void writeFileTest() {
        Path path = Paths.get("E:\\hulin_workspace\\logtail\\logtail\\testLongTimeRead.txt");
        String className = getClass().getCanonicalName();
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            for (int i = 1; i < 10000; i++) {
                writer.write(className + className + className + className + i + "\n");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ListTest() {
        // 获取classpath路径
        String s = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.out.println("classpath : " + s);

        System.out.println("----> logback start");
        logger.trace("--> Hello trace.");
        logger.debug("--> Hello debug.");
        logger.info("--> Hello info.");
        logger.warn("--> Goodbye warn.");
        logger.error("--> Goodbye error.");

        //打印 Logback 内部状态
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
    }
}
