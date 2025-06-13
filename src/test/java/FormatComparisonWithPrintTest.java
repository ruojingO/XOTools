import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class FormatComparisonWithPrintTest {

    // 1. 示例代码（原始 vs 重构）
    public static class Examples {
        public static String origFormat1(Object accuracy) {
            return String.format("AS DECIMAL(18,0)) AS VARCHAR)", accuracy);
        }
        public static String origFormat2() {
            return String.format("cast(round(");
        }
        public static String origFormat3(long pid) {
            return String.format("and child.pid is not null", pid);
        }
        public static String origFormat4(String ts) {
            return String.format("as bigint), '####-##-## ##:##:##'), 120)", ts);
        }

        public static String refactFormat1(int accuracy) {
            return String.format("AS DECIMAL(%d,0) AS VARCHAR", accuracy);
        }
        public static String refactFormat2(int accuracy) {
            return String.format("cast(round(%d)", accuracy);
        }
        public static String refactFormat3(long pid) {
            return String.format("and child.pid is not null # %d", pid);
        }
        public static String refactFormat4(String ts) {
            return String.format("as bigint), '%s', 120)", ts);
        }

        private static final Logger RAW_LOGGER =
                (Logger) LoggerFactory.getLogger("RAW");
        public static void origLog1(boolean external, String part) {
            RAW_LOGGER.debug((external ? "externalData tag" : "DDE ") +
                    " has been spotted {}", part);
        }
        public static void origLog2(String pt, String initV, String sanitizedV) {
            RAW_LOGGER.error(
                    "trigger pattern: " + pt +
                            "  , illegal danger initParam: {}, sanitizered param: {}",
                    initV, sanitizedV);
        }

        private static final Logger REF_LOGGER =
                (Logger) LoggerFactory.getLogger("REF");
        public static void refactLog1(boolean external, String part) {
            REF_LOGGER.debug("{} has been spotted",
                    external ? "externalData tag" : "DDE ", part);
        }
        public static void refactLog2(String pt, String initV, String sanitizedV) {
            REF_LOGGER.error(
                    "trigger pattern: {}, illegal danger initParam: {}, sanitizered param: {}",
                    pt, initV, sanitizedV);
        }
    }

    // 2. 日志捕获器
    private ListAppender<ILoggingEvent> rawAppender;
    private ListAppender<ILoggingEvent> refAppender;

    @Before
    public void setup() {
        LoggerContext lc = (LoggerContext) LoggerFactory
                .getILoggerFactory();
        // RAW logger
        Logger rawLogger = lc.getLogger("RAW");
        rawAppender = new ListAppender<>();
        rawAppender.start();
        rawLogger.addAppender(rawAppender);
        // REF logger
        Logger refLogger = lc.getLogger("REF");
        refAppender = new ListAppender<>();
        refAppender.start();
        refLogger.addAppender(refAppender);
    }

    // 3. 字符串格式化对比测试（含打印）
    @Test
    public void testFormatComparisonWithPrint() {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("accuracy", 5);
        long pid = 123L;
        String ts = "2025-06-13 15:00:00";

        // 原始 vs 重构
        String[] origs = {
                Examples.origFormat1(paramMap.get("accuracy")),
                Examples.origFormat2(),
                Examples.origFormat3(pid),
                Examples.origFormat4(ts)
        };
        String[] refs = {
                Examples.refactFormat1(5),
                Examples.refactFormat2(5),
                Examples.refactFormat3(pid),
                Examples.refactFormat4(ts)
        };

        for (int i = 0; i < origs.length; i++) {
            System.out.printf("[Format %d] 原始: \"%s\"%n", i+1, origs[i]);
            System.out.printf("[Format %d] 重构: \"%s\"%n%n", i+1, refs[i]);
        }

        // 断言
        assertEquals("AS DECIMAL(18,0)) AS VARCHAR)", origs[0]);
        assertEquals("cast(round(",                  origs[1]);
        assertEquals("and child.pid is not null",   origs[2]);
        assertEquals("as bigint), '####-##-## ##:##:##'), 120)", origs[3]);

        assertEquals("AS DECIMAL(5,0) AS VARCHAR",       refs[0]);
        assertEquals("cast(round(5)",                    refs[1]);
        assertEquals("and child.pid is not null # 123", refs[2]);
        assertEquals("as bigint), '2025-06-13 15:00:00', 120)", refs[3]);
    }

    // 4. 日志输出对比测试（含打印）
    @Test
    public void testLoggingComparisonWithPrint() {
        // 触发原始日志
        Examples.origLog1(true, "PART-001");
        Examples.origLog2("PATTERN-X", "V1", "V2");

        System.out.println("---- 原始日志 输出 ----");
        rawAppender.list.forEach(evt ->
                System.out.println(evt.getFormattedMessage())
        );
        System.out.println();

        // 触发重构日志
        Examples.refactLog1(true, "PART-001");
        Examples.refactLog2("PATTERN-X", "V1", "V2");

        System.out.println("---- 重构日志 输出 ----");
        refAppender.list.forEach(evt ->
                System.out.println(evt.getFormattedMessage())
        );
        System.out.println();

        // 断言数量及内容
        assertEquals(2, rawAppender.list.size());
        assertEquals("externalData tag has been spotted PART-001",
                rawAppender.list.get(0).getFormattedMessage());
        assertEquals("trigger pattern: PATTERN-X  , illegal danger initParam: V1, sanitizered param: V2",
                rawAppender.list.get(1).getFormattedMessage());

        assertEquals(2, refAppender.list.size());
        assertEquals("externalData tag has been spotted",
                refAppender.list.get(0).getFormattedMessage());
        assertEquals("trigger pattern: PATTERN-X, illegal danger initParam: V1, sanitizered param: V2",
                refAppender.list.get(1).getFormattedMessage());
    }
}
