import com.eit.hoppy.core.FileHelper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jaren.han on 2020-01-08.
 */
public class FileTailTest {
    Logger logger = LoggerFactory.getLogger(FileTailTest.class);

    @Test
    public void getCurrentLogTest() {
        Path path = Paths.get("E:\\hulin_workspace\\logtail\\pom.xml");
        BasicFileAttributes bfa = null;
        try {
            bfa = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Creation Time      : " + bfa.creationTime());
        System.out.println("Last Access Time   : " + bfa.lastAccessTime());
        System.out.println("Last Modified Time : " + bfa.lastModifiedTime());
        System.out.println("Is Directory       : " + bfa.isDirectory());
        System.out.println("Is Other           : " + bfa.isOther());
        System.out.println("Is Regular File    : " + bfa.isRegularFile());
        System.out.println("Is Symbolic Link   : " + bfa.isSymbolicLink());
        System.out.println("Size               : " + bfa.size());
        Object objectKey = bfa.fileKey();
        System.out.println("Object Key               : " + bfa.fileKey());
    }

    @Test
    public void writeFile() {
//        // File file=new File("D:\\quecspace\\LogTail\\text.txt");
//        try {
//            FileOutputStream fileOutputStream = new FileOutputStream("D:\\quecspace\\LogTail\\666.txt");
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
//            BufferedWriter out = new BufferedWriter(outputStreamWriter);
//            for (int i = 1; i < 60; i++) {
//                out.write(new Date().toString() + " " + UUID.randomUUID().toString());
//                out.newLine();
//                out.flush();
//                Thread.sleep(1000);
//            }
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    @Test
    public void listTest() {
//        LinkedList linkedList=new LinkedList();
//        for(int i=0;i<5;i++){
//            linkedList.add(i);
//        }
//        System.out.print(linkedList.getFirst());
//        System.out.print(linkedList.getFirst());
//        System.out.print(linkedList.getFirst());
//        System.out.print(linkedList.getFirst());
//        System.out.print(linkedList.getFirst());
        logger.info("ttt");

    }

    @Test
    public void resetFile() {
//        File file =new File("D:\\quecspace\\LogTail\\666.txt");
//        try {
//            FileWriter fileWriter =new FileWriter(file);
//            fileWriter.write("");
//            fileWriter.flush();
//            fileWriter.close();
//
//            writeFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void getFile() {
        List<File> files = FileHelper.getFileSort("D:\\opt\\logs");
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
        }
    }

    @Test
    public void ListTest() {
        List<String> list = new ArrayList<>();
        list.add("aa");
        list.add("bb");
        list.add("cc");
        for (String value : list) {
            System.out.println(value);
        }

        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.add("ab");
        linkedList.add("bc");
        linkedList.add("cd");
        Iterator<String> iterator = linkedList.listIterator();
        String value;
        while ((value = iterator.next()) != null) {
            System.out.println(value);
        }
    }
}
