// 文件：FormatComparisonTest.java

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;                  // Logback 专用 Logger
import ch.qos.logback.classic.LoggerContext;           // LoggerFactory 返回类型需转型
import ch.qos.logback.classic.spi.ILoggingEvent;       // 日志事件接口
import ch.qos.logback.core.read.ListAppender;          // 内存 Appender

public class FormatComparisonTest {

    // ------- 1. 示例代码（原始 vs 重构） -------
    public static class Examples {
        // 原始 String.format 调用（无占位符）
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

        // 重构后的 String.format 调用（正确使用占位符）
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

        // 原始日志调用（字符串拼接 + 占位符）
        private static final Logger RAW_LOGGER = (Logger) LoggerFactory.getLogger("RAW");
        public static void origLog1(boolean external, String part) {
            RAW_LOGGER.debug((external ? "externalData tag" : "DDE ") + " has been spotted {}", part);
        }
        public static void origLog2(String pt, String initV, String sanitizedV) {
            RAW_LOGGER.error(" trigger pattern: " + pt + "  , illegal danger initParam: {}, sanitizered param: {}", initV, sanitizedV);
        }

        // 重构后的参数化日志调用
        private static final Logger REF_LOGGER = (Logger) LoggerFactory.getLogger("REF");
        public static void refactLog1(boolean external, String part) {
            REF_LOGGER.debug("{} has been spotted", external ? "externalData tag" : "DDE ", part);
        }
        public static void refactLog2(String pt, String initV, String sanitizedV) {
            REF_LOGGER.error("trigger pattern: {}, illegal danger initParam: {}, sanitizered param: {}", pt, initV, sanitizedV);
        }
    }

    // ------- 2. 日志捕获器 -------
    private ListAppender<ILoggingEvent> rawAppender;
    private ListAppender<ILoggingEvent> refAppender;

    @Before
    public void setup() {
        // 1) 获取 Logback 的 LoggerContext
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // 2) 配置 RAW Logger
        Logger rawLogger = lc.getLogger("RAW");
        rawAppender = new ListAppender<>();
        rawAppender.start();
        rawLogger.addAppender(rawAppender);
        // 3) 配置 REF Logger
        Logger refLogger = lc.getLogger("REF");
        refAppender = new ListAppender<>();
        refAppender.start();
        refLogger.addAppender(refAppender);
    }

    // ------- 3. 测试：String.format 比较 -------
    @Test
    public void testFormatComparison() {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("accuracy", 5);
        long pid = 123L;
        String ts = "2025-06-13 15:00:00";

        // 原始调用结果：原样返回
        assertEquals("AS DECIMAL(18,0)) AS VARCHAR)",
                Examples.origFormat1(paramMap.get("accuracy")));
        assertEquals("cast(round(",
                Examples.origFormat2());
        assertEquals("and child.pid is not null",
                Examples.origFormat3(pid));
        assertEquals("as bigint), '####-##-## ##:##:##'), 120)",
                Examples.origFormat4(ts));

        // 重构调用结果：实际替换占位符
        assertEquals("AS DECIMAL(5,0) AS VARCHAR",
                Examples.refactFormat1(5));
        assertEquals("cast(round(5)",
                Examples.refactFormat2(5));
        assertEquals("and child.pid is not null # 123",
                Examples.refactFormat3(pid));
        assertEquals("as bigint), '2025-06-13 15:00:00', 120)",
                Examples.refactFormat4(ts));
    }

    // ------- 4. 测试：日志输出比较 -------
    @Test
    public void testLoggingComparison() {
        // 原始日志调用
        Examples.origLog1(true, "PART-001");
        Examples.origLog2("PATTERN-X", "V1", "V2");
        assertEquals(2, rawAppender.list.size());
        assertEquals("externalData tag has been spotted PART-001",
                rawAppender.list.get(0).getFormattedMessage());
        assertEquals("trigger pattern: PATTERN-X  , illegal danger initParam: V1, sanitizered param: V2",
                rawAppender.list.get(1).getFormattedMessage());

        // 重构日志调用
        Examples.refactLog1(true, "PART-001");
        Examples.refactLog2("PATTERN-X", "V1", "V2");
        assertEquals(2, refAppender.list.size());
        assertEquals("externalData tag has been spotted PART-001",
                refAppender.list.get(0).getFormattedMessage());
        assertEquals("trigger pattern: PATTERN-X, illegal danger initParam: V1, sanitizered param: V2",
                refAppender.list.get(1).getFormattedMessage());
    }
}
