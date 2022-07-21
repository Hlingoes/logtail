package com.eit.hoppy.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * description: windows环境下的命令行执行，可以兼任更多命令行
 *
 * @author Hlingoes
 * @date 2022/6/12 14:45
 * @citation https://www.cnblogs.com/leodaxin/p/8991628.html
 * @citation https://www.jianshu.com/p/af4b3264bc5d
 */
public class WinShellExecutor implements ShellExecutor {
    private static final Logger logger = LoggerFactory.getLogger(WinShellExecutor.class);

    /**
     * 命令行
     */
    private List<String> commands;
    /**
     * 超时时间(ms)
     */
    private int timeout;

    public WinShellExecutor(List<String> commands, int timeout) {
        this.commands = commands;
        this.timeout = timeout;
    }

    @Override
    public ExecuteResult executeShell() {
        Process process = null;
        StreamGobbler outputGobbler = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            InputStream pIn = process.getInputStream();
            outputGobbler = new StreamGobbler(pIn, "OUTPUT");
            outputGobbler.start();
            boolean success = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            if (success) {
                return new ExecuteResult(process.exitValue(), outputGobbler.readContent());
            }
            logger.info("The command [{}] timed out.", String.join(" ", commands));
            return new ExecuteResult(-1, outputGobbler.readContent());
        } catch (IOException ex) {
            String errorMessage = "The commands [" + commands + "] execute failed.";
            logger.error(errorMessage, ex);
            return new ExecuteResult(-1, outputGobbler.readContent());
        } catch (InterruptedException ex) {
            String errorMessage = "The commands [" + commands + "] did not complete due to an interrupted error.";
            logger.error(errorMessage, ex);
            return new ExecuteResult(-1, outputGobbler.readContent());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}
