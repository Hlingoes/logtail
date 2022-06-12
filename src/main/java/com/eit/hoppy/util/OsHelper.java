package com.eit.hoppy.util;

/**
 * description: 判断操作系统
 *
 * @author Hlingoes
 * @date 2022/6/12 14:16
 */
public class OsHelper {

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

}
