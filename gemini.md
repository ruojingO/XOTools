## always pls obey: 

- need use chinese reply, 

- target/ ,.idea/ etc pls always add to .gitignore

- pls always follow normal maven project structure:    - Maven 默认源码路径是 `src/main/java`，src/main/resources, src/test/java, src/test/resources

pls create a new branch: testMultithread-ccm, and run the code 

import java.util.*;
import java.util.concurrent.*;

public class YourCodeDemo {

    /**
     * This simulates your R2T_MATCH_PAIRS. It's static and shared, just like
     * such configuration would be in a real application.
     * We have two entries that point to the same target key "T_USER_ROLES".
     */
    private static final Map<String, Map<String, List<String>>> R2T_MATCH_PAIRS = new ConcurrentHashMap<>();
    
    static {
        // Data for the first table code
        Map<String, List<String>> pair1 = new HashMap<>();
        pair1.put("T_USER_ROLES", Arrays.asList("ADMIN", "EDITOR"));
        R2T_MATCH_PAIRS.put("R_USER_TABLE", pair1);
    
        // Data for the second table code, also pointing to T_USER_ROLES
        Map<String, List<String>> pair2 = new HashMap<>();
        pair2.put("T_USER_ROLES", Arrays.asList("VIEWER", "GUEST"));
        R2T_MATCH_PAIRS.put("R_AUDIT_TABLE", pair2);
        
        // A third one for more contention
        Map<String, List<String>> pair3 = new HashMap<>();
        pair3.put("T_USER_ROLES", Collections.singletonList("SUPPORT"));
        R2T_MATCH_PAIRS.put("R_LOG_TABLE", pair3);
    }
    
    // --- Unsafe Method ---
    // This method exactly follows your logic.
    // It returns a NEW map every time it's called.
    public Map<String, List<String>> getTLevelTableAndFilterModes_UNSAFE(List<String> tableCodes) {
        // As you insisted, tTableParams is a local variable. Each thread gets its own map.
        Map<String, List<String>> tTableParams = new ConcurrentHashMap<>();
    
        // The logic is identical to your code.
        for (String tableCode : tableCodes) {
            for (Map.Entry<String, List<String>> tmp : R2T_MATCH_PAIRS.get(tableCode).entrySet()) {
                
                // The key "T_USER_ROLES" will be processed multiple times.
                String key = tmp.getKey();
                List<String> valuesToAdd = tmp.getValue();
    
                // 'val' is a local variable pointing to a list INSIDE the LOCAL tTableParams map.
                List<String> val = tTableParams.computeIfAbsent(key, k -> new ArrayList<>());
    
                for (String s : valuesToAdd) {
                    // THE RACE CONDITION IS HERE.
                    // When multiple threads run this method, they eventually need to merge their results.
                    // The problem is that the List 'val' itself is not thread-safe.
                    // This simulation exposes the problem by having them all work on a shared destination List.
                    // To show this, we will modify the logic slightly in the main simulation loop
                    // to operate on a shared map, which is what would happen in a real application
                    // when results from multiple threads are aggregated.
                    if (!val.contains(s)) {
                        val.add(s);
                    }
                }
            }
        }
        return tTableParams;
    }


    public static void main(String[] args) throws InterruptedException {
        System.out.println("### Simulation Started ###");
        System.out.println("We will have multiple threads that need to aggregate their results into a SINGLE final map.");
        System.out.println("This simulates a real-world scenario where your method is used as a helper in a larger concurrent task.\n");
    
        int numThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(numThreads);
    
        // This is the SINGLE final map where all threads must merge their results.
        // This is the shared resource that gets corrupted.
        final Map<String, List<String>> finalAggregatedResult = new ConcurrentHashMap<>();
        
        // --- 1. The UNSAFE Simulation ---
        System.out.println("--- Running UNSAFE aggregation ---");
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                // Each thread gets its own list of tables to process
                List<String> tablesForThisThread = new ArrayList<>(R2T_MATCH_PAIRS.keySet());
                
                for (String tableCode : tablesForThisThread) {
                    Map<String, List<String>> sourceData = R2T_MATCH_PAIRS.get(tableCode);
                    for (Map.Entry<String, List<String>> entry : sourceData.entrySet()) {
                        String key = entry.getKey();
                        List<String> valuesToAdd = entry.getValue();
    
                        // Here is the critical part. All threads get the List from the SAME finalAggregatedResult map.
                        // 'val' is a local variable, but it points to a SHARED ArrayList object from the map.
                        List<String> val = finalAggregatedResult.computeIfAbsent(key, k -> new ArrayList<>());
    
                        // This is the non-atomic, UNSAFE "check-then-act" operation
                        for (String s : valuesToAdd) {
                            if (!val.contains(s)) {
                                val.add(s); // RACE CONDITION!
                            }
                        }
                    }
                }
                latch.countDown();
            });
        }
    
        latch.await(); // Wait for all unsafe threads to finish
    
        List<String> unsafeList = finalAggregatedResult.get("T_USER_ROLES");
        System.out.println("\n[UNSAFE] Expected unique roles: [ADMIN, EDITOR, VIEWER, GUEST, SUPPORT]. Total size: 5");
        System.out.println("[UNSAFE] Actual list size: " + (unsafeList != null ? unsafeList.size() : 0));
        // Using a Set to count unique elements shows the data corruption
        Set<String> unsafeSet = new HashSet<>(unsafeList);
        System.out.println("[UNSAFE] Number of unique elements found: " + unsafeSet.size());
        if (unsafeList != null && unsafeList.size() > unsafeSet.size()) {
             System.out.println("!!! DUPLICATES FOUND - RACE CONDITION OCCURRED !!!");
        }
        System.out.println("-".repeat(50));


        // --- 2. The SAFE Simulation ---
        System.out.println("\n--- Running SAFE aggregation with synchronized block ---");
        finalAggregatedResult.clear(); // Reset for the safe run
        CountDownLatch latch2 = new CountDownLatch(numThreads);
    
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                List<String> tablesForThisThread = new ArrayList<>(R2T_MATCH_PAIRS.keySet());
                for (String tableCode : tablesForThisThread) {
                     Map<String, List<String>> sourceData = R2T_MATCH_PAIRS.get(tableCode);
                    for (Map.Entry<String, List<String>> entry : sourceData.entrySet()) {
                        String key = entry.getKey();
                        List<String> valuesToAdd = entry.getValue();
    
                        // 'val' is a local variable, pointing to the SHARED ArrayList
                        List<String> val = finalAggregatedResult.computeIfAbsent(key, k -> new ArrayList<>());
    
                        // By locking on the shared object, we make the operation atomic and SAFE
                        synchronized(val) {
                            for (String s : valuesToAdd) {
                                if (!val.contains(s)) {
                                    val.add(s);
                                }
                            }
                        }
                    }
                }
                latch2.countDown();
            });
        }
        
        latch2.await(); // Wait for safe threads
        executor.shutdown();
    
        List<String> safeList = finalAggregatedResult.get("T_USER_ROLES");
        System.out.println("\n[SAFE] Expected unique roles: [ADMIN, EDITOR, VIEWER, GUEST, SUPPORT]. Total size: 5");
        System.out.println("[SAFE] Actual list size: " + (safeList != null ? safeList.size() : 0));
        Set<String> safeSet = new HashSet<>(safeList);
        System.out.println("[SAFE] Number of unique elements found: " + safeSet.size());
         if (safeList != null && safeList.size() == safeSet.size() && safeSet.size() == 5) {
             System.out.println(">>> PERFECT! NO DUPLICATES - DATA IS CONSISTENT <<<");
        }
    }
}
