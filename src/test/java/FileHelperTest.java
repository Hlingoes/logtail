import com.eit.hoppy.command.CommandExecutor;
import com.eit.hoppy.command.ExecuteResult;
import com.eit.hoppy.command.WinCommandExecutor;
import com.eit.hoppy.logtail.CacheManager;
import com.eit.hoppy.logtail.LogMeta;
import com.eit.hoppy.logtail.LogMetaFactory;
import com.eit.hoppy.util.FileHelper;
import com.eit.hoppy.util.SerializationUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author Hlingoes
 * @date 2022/6/12 12:18
 */
public class FileHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(FileHelperTest.class);

    @Test
    public void testReader() {
        String str1 = FileHelper.readFirstBytes("E:\\hulin_workspace\\logtail\\pom.xml", 128);
        logger.info(str1);
        logger.info(String.valueOf(str1.hashCode()));
    }

    @Test
    public void getFileId() {
        String filePath = "E:\\hulin_workspace\\logtail\\pom.xml";
        logger.info(FileHelper.queryWindowsFileIdOrDefault(filePath));
    }

    @Test
    public void testCommand() {
//        String command = "cmd.exe /C fsutil file queryfileid E:\\hulin_workspace\\logtail\\pom.xml";
//        String command = "cmd /c netstat -an | findstr :80";
        List<String> command = new ArrayList<String>();
        command.add("cmd");
        command.add("/c");
        command.add("netstat");
        command.add("-an");
        command.add("|");
        command.add("findstr");
        command.add(":80");
        WinCommandExecutor commandExecutor = new WinCommandExecutor(command, 1000);
        ExecuteResult result = commandExecutor.executeCommand();
        logger.info("testCommand: {}", result.getContent());
    }

    @Test
    public void testCommandProcess1() {
        List<String> command = new ArrayList<String>();
        command.add("cmd");
        command.add("/c");
        command.add("fsutil");
        command.add("file");
        command.add("queryfileid");
        command.add("E:\\hulin_workspace\\logtail\\pom.xml");
        CommandExecutor commandExecutor = new WinCommandExecutor(command, 500);
        ExecuteResult result = commandExecutor.executeCommand();
        logger.info("{}", result.getContent());
    }

    @Test
    public void testCommandTimeout() {
        List<String> command = new ArrayList<String>();
        command.add("ping");
        command.add("-t");
        command.add("jianshu.com");
        CommandExecutor commandExecutor = new WinCommandExecutor(command, 1000);
        ExecuteResult result = commandExecutor.executeCommand();
        logger.info("{}", result.getContent());
    }

    @Test
    public void testCommandProcess3() {
        List<String> command = new ArrayList<>();
        command.add("javac");
        CommandExecutor commandExecutor = new WinCommandExecutor(command, 500);
        ExecuteResult result = commandExecutor.executeCommand();
        logger.info("{}", result.getContent());
    }

    @Test
    public void testProcessBuilder() {
        List<String> params = new ArrayList<String>();
        // cmd.exe /C fsutil file queryfileid
        params.add("javac");
//        params.add("cmd");
//        params.add("/c");
//        params.add("fsutil");
//        params.add("file");
//        params.add("queryfileid");
//        params.add("E:\\hulin_workspace\\logtail\\pom.xml");

        ProcessBuilder processBuilder = new ProcessBuilder(params);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("exitCode = " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteObject() throws IOException {
        String fileName = "E:\\hulin_workspace\\logtail\\test.dat";
        File testFile = new File("E:\\hulin_workspace\\logtail\\pom.xml");
        LogMeta logMeta = LogMetaFactory.createLogMeta(testFile);
        CacheManager.addFileCreateCache(logMeta);
        SerializationUtils.writeSerializeObject(fileName, CacheManager.getFileCacheMap());
    }

    @Test
    public void testReadObject() throws IOException {
        String fileName = "E:\\hulin_workspace\\logtail\\test.dat";
        Map<String, LogMeta> obj = (Map<String, LogMeta>) SerializationUtils.readDeserialize(fileName);
        logger.info("{}", obj.size());
    }

}
