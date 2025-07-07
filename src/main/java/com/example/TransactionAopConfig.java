package com.example;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Properties;

@Aspect
@Configuration
public class TransactionAopConfig {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public TransactionInterceptor txAdvice() {
        Properties properties = new Properties();
        properties.setProperty("get*", "readOnly");
        properties.setProperty("find*", "readOnly");
        properties.setProperty("select*", "readOnly");
        properties.setProperty("*", "PROPAGATION_REQUIRED");

        TransactionInterceptor tsi = new TransactionInterceptor();
        tsi.setTransactionManager(transactionManager);
        tsi.setTransactionAttributes(properties);
        return tsi;
    }

    @Bean
    public Advisor txAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.example.*ServiceImpl.*(..))");
        return new DefaultPointcutAdvisor(pointcut, txAdvice());
    }
}
