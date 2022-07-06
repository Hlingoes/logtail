package com.eit.hoppy.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * description:
 *
 * @author Hlingoes
 * @date 2022/6/6 22:14
 */
public class SerializationUtils {
    public SerializationUtils() {
    }

    /**
     * description: 将对象序列化后，写入文件
     *
     * @param fileName
     * @param object
     * @return void
     * @author Hlingoes 2022/7/6
     */
    public static void writeSerializeObject(String fileName, Object object) throws IOException {
        String tmpFileName = fileName + ".tmp";
        File tmpFile = new File(tmpFileName);
        Files.write(Paths.get(tmpFileName), serialize(object));
        tmpFile.renameTo(new File(fileName));
    }

    /**
     * description: 读取文件，序列化为对象
     *
     * @param fileName
     * @return java.lang.Object
     * @author Hlingoes 2022/7/6
     */
    public static Object readDeserialize(String fileName) throws IOException {
        return deserialize(Files.readAllBytes(Paths.get(fileName)));
    }

    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                oos.flush();
            } catch (IOException ex) {
                throw new IllegalArgumentException("Failed to serialize object of type: " + object.getClass(), ex);
            }
            return baos.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                return ois.readObject();
            } catch (IOException ex) {
                throw new IllegalArgumentException("Failed to deserialize object", ex);
            } catch (ClassNotFoundException ex2) {
                throw new IllegalStateException("Failed to deserialize object type", ex2);
            }
        }
    }
}
