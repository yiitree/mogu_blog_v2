package com.moxi.mogublog.admin;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.oas.annotations.EnableOpenApi;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * mogu-admin 启动类
 *
 * @author 陌溪
 * @date 2020年12月31日21:26:04
 */
@SpringBootApplication
// 启注解事务管理 --- 不加也可以
@EnableTransactionManagement
// 开启Swagger3生成接口文档
@EnableOpenApi
// 服务发现
@EnableDiscoveryClient
// 开启缓存
@EnableCaching
// 开启Rabbit
@EnableRabbit
// 配置FeignClients位置
@EnableFeignClients("com.moxi.mogublog.commons.feign")
// 扫描包位置
@ComponentScan(basePackages = {
        "com.moxi.mogublog.commons.config",
        "com.moxi.mogublog.commons.fallback",
        "com.moxi.mogublog.utils",
        "com.moxi.mogublog.admin",
        "com.moxi.mogublog.xo.utils",
        "com.moxi.mogublog.xo.service"
})
public class AdminApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(AdminApplication.class, args);
    }

    /**
     * 设置时区
     */
    @PostConstruct
    void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
