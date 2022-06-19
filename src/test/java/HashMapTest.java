import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * description: 测试多线程下的HashMap表现
 *
 * @author Hlingoes
 * @date 2022/6/19 12:41
 */
public class HashMapTest {

    private static final Logger logger = LoggerFactory.getLogger(HashMapTest.class);

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * description: 测试ConcurrentHashMap的多线程get，set
     * 结果说明：这个操作是线程不安全的
     */
    @Test
    public void TestConcurrentHashMap() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("key", 1);
        for (int i = 0; i < 10000; i++) {
            executorService.submit(() -> {
                Integer value = map.get("key");
                map.put("key", value + 1);
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                // 预期结果10001，实际结果是: 9756 (一个随机的值)
                logger.info("val: {}", map.get("key"));
                break;
            }
        }
    }

    /**
     * description: 测试HashMap的多线程get，set
     * 结果说明：对比用例，肯定是线程不安全的
     */
    @Test
    public void TestHashMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("key", 1);
        for (int i = 0; i < 10000; i++) {
            executorService.submit(() -> {
                Integer value = map.get("key");
                map.put("key", value + 1);
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                // 预期结果10001，实际结果是: 8055 (一个随机的值)
                logger.info("val: {}", map.get("key"));
                break;
            }
        }
    }

    /**
     * description: 测试ConcurrentHashMap的多线程set
     * 结果说明：这个操作是线程安全的
     *
     * @param
     * @return void
     * @author Hlingoes 2022/6/19
     */
    @Test
    public void TestConcurrentHashMapPut() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 100000; i++) {
            String key = String.valueOf(i);
            int val = i;
            executorService.submit(() -> {
                map.put(key, val);
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                // 预期结果100000，实际结果是: 100000
                logger.info("size: {}", map.size());
                break;
            }
        }
    }

    /**
     * description: 测试HashMap的多线程set
     * 结果说明：这个操作是线程不安全的
     *
     * @param
     * @return void
     * @author Hlingoes 2022/6/19
     */
    @Test
    public void TestHashMapPut() {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String key = String.valueOf(i);
            int val = i;
            executorService.submit(() -> {
                map.put(key, val);
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                // 预期结果100000，实际结果是: 99972 (随机的)
                logger.info("size: {}", map.size());
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
