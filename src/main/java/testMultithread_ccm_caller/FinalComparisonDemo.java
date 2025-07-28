package testMultithread_ccm_caller;

import java.util.*;
import java.util.concurrent.*;

public class FinalComparisonDemo {

    final static Map<String, Map<String, List<String>>> R2T_MATCH_PAIRS = new ConcurrentHashMap<>();


    static {

        R2T_MATCH_PAIRS.put("R_TABLE_1",Collections.singletonMap("T_USERS",Arrays.asList("ADMIN","EDITOR")));
        R2T_MATCH_PAIRS.put("R_TABLE_2",Collections.singletonMap("T_USERS",Arrays.asList("VIEWER","GUEST")));
}
    // 这是一个模拟的服务类，其中包含您最初的方法
    static class CsdcTaskExecuteService {
        /**
         * 这是您最初的、未修改的方法。它本身没有并发问题，
         * 但它返回的结果在并发的“上层调用”中使用是“危险的”。
         */
        public Map<String, List<String>> getTLevelTableAndFilterModes_UNSAFE(
                List<String> tableCodes) {

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
    }

    public static void main(String[] args) throws InterruptedException {

        // --- 1. 准备共享数据和环境 ---
        final CsdcTaskExecuteService service = new CsdcTaskExecuteService();

        final int taskCount = 500; // 增加任务数量以更容易地看到冲突
        final ExecutorService executor = Executors.newFixedThreadPool(20);
        
        System.out.println("### 模拟开始：上层调用将并行执行 " + taskCount + " 次任务 ###");

        // =================================================================================
        // 2.【不安全】的并发合并 (UNSAFE Concurrent Aggregation)
        // =================================================================================
        System.out.println("\n--- Part 1: 执行【不安全】的合并逻辑 ---");
        final Map<String, List<String>> unsafeFinalReport = new ConcurrentHashMap<>();
        CountDownLatch unsafeLatch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                // 每个线程都调用您原始的方法，获取局部结果
                Map<String, List<String>> localResult = service.getTLevelTableAndFilterModes_UNSAFE(
                        new ArrayList<>(R2T_MATCH_PAIRS.keySet()));

                // 【冲突点】将局部结果合并到共享报告中，但没有加锁保护！
                for (Map.Entry<String, List<String>> entry : localResult.entrySet()) {
                    List<String> destinationList = unsafeFinalReport.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    for (String s : entry.getValue()) {
                        // 这里的 "check-then-act" 是非原子的，竞态条件会在这里发生
                        if (!destinationList.contains(s)) {
                            destinationList.add(s);
                        }
                    }
                }
                unsafeLatch.countDown();
            });
        }
        unsafeLatch.await();
        
        // --- 打印不安全的结果 ---
        List<String> unsafeList = unsafeFinalReport.get("T_USERS");
        System.out.println("【不安全结果】期望 'T_USERS' 的大小为 4。");
        System.out.println("【不安全结果】实际 'T_USERS' 的大小为: " + (unsafeList != null ? unsafeList.size() : 0) + "  <--- ❌ 数据已损坏！");


      //  System.out.println("\n" + "=".repeat(80) + "\n");
        

        // =================================================================================
        // 3.【安全】的并发合并 (SAFE Concurrent Aggregation)
        // =================================================================================
        System.out.println("--- Part 2: 执行【安全】的合并逻辑 ---");
        final Map<String, List<String>> safeFinalReport = new ConcurrentHashMap<>();
        CountDownLatch safeLatch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            executor.submit(() -> {
                // 同样，每个线程都调用您原始的方法
                Map<String, List<String>> localResult = service.getTLevelTableAndFilterModes_UNSAFE(
                        new ArrayList<>(R2T_MATCH_PAIRS.keySet()));

                // 【修复点】将局部结果合并到共享报告中，并使用 synchronized 块加锁保护
                for (Map.Entry<String, List<String>> entry : localResult.entrySet()) {
                    List<String> destinationList = safeFinalReport.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    
                    // 通过对共享的 List 对象加锁，我们保证了操作的原子性
                    synchronized (destinationList) {
                        for (String s : entry.getValue()) {
                            if (!destinationList.contains(s)) {
                                destinationList.add(s);
                            }
                        }
                    }
                }
                safeLatch.countDown();
            });
        }
        safeLatch.await();

        // --- 打印安全的结果 ---
        List<String> safeList = safeFinalReport.get("T_USERS");
        System.out.println("【安全结果】期望 'T_USERS' 的大小为 4。");
        System.out.println("【安全结果】实际 'T_USERS' 的大小为: " + (safeList != null ? safeList.size() : 0) + "    <--- ✅ 数据正确！");

        executor.shutdown();
    }
}