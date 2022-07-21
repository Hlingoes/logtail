package com.eit.hoppy.command;

/**
 * description: 执行命令行
 *
 * @author Hlingoes 2022/6/12
 */
public interface ShellExecutor {

    /**
     * description: 执行命令行
     *
     * @return com.eit.hoppy.command.ExecuteResult
     * @author Hlingoes 2022/6/12
     */
    ExecuteResult executeShell();

}
