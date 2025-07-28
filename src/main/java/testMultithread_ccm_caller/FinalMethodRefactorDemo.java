package testMultithread_ccm_caller;

import java.util.*;
import java.util.concurrent.*;

public class FinalMethodRefactorDemo {

    // 模拟的共享数据源
    final static Map<String, Map<String, List<String>>> R2T_MATCH_PAIRS = new ConcurrentHashMap<>();
    static {
        R2T_MATCH_PAIRS.put("R_TABLE_1", Collections.singletonMap("T_USERS", Arrays.asList("ADMIN", "EDITOR")));
        R2T_MATCH_PAIRS.put("R_TABLE_2", Collections.singletonMap("T_USERS", Arrays.asList("VIEWER", "GUEST")));
    }

    // 这是一个模拟的服务类
    static class CsdcTaskExecuteService {
        
        // --- 1. 不安全的方法 (UNSAFE Method) ---
        /**
		  * 这是您最初的、未修改的方法。它本身没有并发问题，
         * 但它返回的结果在并发的“上层调用”中使用是“危险的”。
         * 它返回一个局部 map，将并发合并的风险完全留给了调用者。
         */
        public Map<String, List<String>> getTLevelTableAndFilterModes_UNSAFE(List<String> tableCodes) {
            Map<String, List<String>> tTableParams = new ConcurrentHashMap<>();
            for (String tableCode : tableCodes) {
                for (Map.Entry<String, List<String>> tmp : R2T_MATCH_PAIRS.get(tableCode).entrySet()) {
                    List<String> val = tTableParams.computeIfAbsent(tmp.getKey(), k -> new ArrayList<>());
                    for (String s : tmp.getValue()) {
                        if (!val.contains(s)) {
                            val.add(s);
                        }
                    }
                }
            }
            return tTableParams;
        }

        // --- 2. 重构后的安全方法 (REFACTORED SAFE Method) ---
        /**
         * 这是重构后的安全方法。它不返回任何东西，而是直接、安全地更新传入的【共享报告】。
         * 它将并发安全的复杂性【封装】在了方法内部，对调用者非常友好。
         * @param tableCodes 要处理的表
         * @param finalReport 需要被安全更新的共享 Map
         */
        public void getTLevelTableAndFilterModes_SAFE(List<String> tableCodes, Map<String, List<String>> finalReport) {
            // 这个方法内部的逻辑，就是我们之前在 main 方法里写的“安全合并逻辑”
            for (String tableCode : tableCodes) {
                for (Map.Entry<String, List<String>> tmp : R2T_MATCH_PAIRS.get(tableCode).entrySet()) {
                    String key = tmp.getKey();
                    List<String> valuesToAdd = tmp.getValue();

                    // 从【共享的】 finalReport 获取 List
                    List<String> destinationList = finalReport.computeIfAbsent(key, k -> new ArrayList<>());
                    
                    // 【关键】在方法内部处理了同步问题！
                    synchronized (destinationList) {
                        for (String s : valuesToAdd) {
                            if (!destinationList.contains(s)) {
                                destinationList.add(s);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        final CsdcTaskExecuteService service = new CsdcTaskExecuteService();
        final int taskCount = 1000;
        final ExecutorService executor = Executors.newFixedThreadPool(20);
        
        System.out.println("### 对比开始：一个“天真”的调用者将如何使用这两个方法 ###");

        // =================================================================================
        // Part 1: 调用【不安全】的方法
        // =================================================================================
        System.out.println("\n--- 场景一：调用【不安全】的方法 ---");
        System.out.println("调用者必须自己写一套复杂的、容易出错的合并逻辑...");

        final Map<String, List<String>> unsafeFinalReport = new ConcurrentHashMap<>();
        CountDownLatch unsafeLatch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                // 1. 调用不安全的方法，得到一个局部结果
                Map<String, List<String>> localResult = service.getTLevelTableAndFilterModes_UNSAFE(new ArrayList<>(R2T_MATCH_PAIRS.keySet()));
                
                // 2. 调用者被迫进行“天真”的合并，因为他不知道 List 是不安全的
                for (Map.Entry<String, List<String>> entry : localResult.entrySet()) {
                    List<String> destinationList = unsafeFinalReport.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    // 这里没有锁，冲突必然发生
                    for (String s : entry.getValue()) {
                        if (!destinationList.contains(s)) {
                            // ==================== 【关键修改】 ====================
                            // 在这里制造一个微小的延迟，来稳定地暴露竞态条件。
                            // 这模拟了真实业务中，检查和执行之间可能存在的任何耗时。
                            try {
                                Thread.sleep(1); // 仅仅暂停1毫秒
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            // =====================================================

                            destinationList.add(s);
                        }
                    }
                }
                unsafeLatch.countDown();
            });
        }
        unsafeLatch.await();
        
        List<String> unsafeList = unsafeFinalReport.get("T_USERS");
        System.out.println("【不安全方法的结果】期望大小为 4。");
        System.out.println("【不安全方法的结果】实际大小为: " + (unsafeList != null ? unsafeList.size() : 0) + "  <--- ❌ 失败！调用者很容易就写出了错误的代码。");


       // System.out.println("\n" + "=".repeat(80) + "\n");
        

        // =================================================================================
        // Part 2: 调用【安全】的方法
        // =================================================================================
        System.out.println("--- 场景二：调用【安全】的方法 ---");
        System.out.println("调用者的代码变得极其简单和清晰！");
        
        final Map<String, List<String>> safeFinalReport = new ConcurrentHashMap<>();
        CountDownLatch safeLatch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                // 调用者只需要调用这一个安全的方法，无需关心任何合并、加锁的细节！
                service.getTLevelTableAndFilterModes_SAFE(new ArrayList<>(R2T_MATCH_PAIRS.keySet()), safeFinalReport);
                safeLatch.countDown();
            });
        }
        safeLatch.await();

        List<String> safeList = safeFinalReport.get("T_USERS");
        System.out.println("【安全方法的结果】期望大小为 4。");
        System.out.println("【安全方法的结果】实际大小为: " + (safeList != null ? safeList.size() : 0) + "    <--- ✅ 成功！代码健壮且易于使用。");

        executor.shutdown();
    }
}