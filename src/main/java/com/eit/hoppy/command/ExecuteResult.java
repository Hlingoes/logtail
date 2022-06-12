package com.eit.hoppy.command;

import java.util.StringJoiner;

/**
 * description: 命令行执行的返回值
 *
 * @author Hlingoes
 * @date 2022/6/12 14:31
 * @citation https://www.jianshu.com/p/af4b3264bc5d
 */
public class ExecuteResult {
    private int exitCode;
    private String content;

    public ExecuteResult(int exitCode, String content) {
        this.exitCode = exitCode;
        this.content = content;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ExecuteResult.class.getSimpleName() + "[", "]")
                .add("exitCode=" + exitCode)
                .add("content='" + content + "'")
                .toString();
    }
}
