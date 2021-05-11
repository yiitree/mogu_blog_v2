package com.moxi.mogublog.admin.annotion.AvoidRepeatableCommit;

import com.moxi.mogublog.utils.IpUtils;
import com.moxi.mogublog.utils.RedisUtil;
import com.moxi.mogublog.utils.ResultUtil;
import com.moxi.mogublog.utils.StringUtils;
import com.moxi.mogublog.xo.global.RedisConf;
import com.moxi.mogublog.xo.global.SysConf;
import com.moxi.mougblog.base.holder.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 避免接口重复提交AOP
 *
 * @author: 陌溪
 * @create: 2020-04-23-12:12
 */
@Aspect
@Component
@Slf4j
public class AvoidRepeatableCommitAspect {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @param point
     */
    @Around("@annotation(com.moxi.mogublog.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        HttpServletRequest request = RequestHolder.getRequest();

        // 获得ip
        String ip = IpUtils.getIpAddr(request);

        // 得到注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 要访问的方法
        Method method = signature.getMethod();
        // 方法所属类
        String className = method.getDeclaringClass().getName();
        // 方法名
        String name = method.getName();

        // 拼接成全类名 --- 类名#方法名
        String ipKey = String.format("%s#%s", className, name);

        // 转换成HashCode --- 把全类名转化为hashcode
        int hashCode = Math.abs(ipKey.hashCode());

        // 保存到redis中的key: AVOID_REPEATABLE_COMMIT:ip_hashCode
        String key = String.format("%s:%s_%d", RedisConf.AVOID_REPEATABLE_COMMIT, ip, hashCode);

        log.info("ipKey={},hashCode={},key={}", ipKey, hashCode, key);

        // 得到方法上指定注解：AvoidRepeatableCommit
        AvoidRepeatableCommit avoidRepeatableCommit = method.getAnnotation(AvoidRepeatableCommit.class);

        // 得到注解上的指定时间，已设置默认时间 default:1000
        long timeout = avoidRepeatableCommit.timeout();

        // 访问redis中是否已经有保存
        String value = redisUtil.get(key);

        // 如果有，测抛出请勿重复提交表单提示
        if (StringUtils.isNotBlank(value)) {
            log.info("请勿重复提交表单");
            return ResultUtil.result(SysConf.ERROR, "请勿重复提交表单");
        }

        // 如果没有则保存到redis中,其实只要保存key就可以，根据key查询是否有这个键值对，如果有，则说明已经保存过，value其实不重要
        redisUtil.setEx(key, StringUtils.getUUID(), timeout, TimeUnit.MILLISECONDS);

        //执行方法
        Object object = point.proceed();
        return object;
    }

}
