package AuD.multi.resource.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisOperations;

/**
 * Description: 参考自{@code org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}.    <br>
 *
 * 目前仅仅做了2件事:   <br>
 * 1.{@link EnableConfigurationProperties} -- 开启{@link MultiRedisProperties}.   <br>
 * -- 关于{@code EnableConfigurationProperties}需要掌握的事项:  -- 实际上需要掌握spring属性值注入的原理于流程.    <br>
 *
 * 2.{@link Import} -- 导入配置类{@link MultiLettuceConnectionConfiguration}
 *
 * @author AuD/胡钊
 * @ClassName MultiRedisAutoConfiguration
 * @date 2021/7/15 17:03
 * @Version 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(MultiRedisProperties.class)
@Import(MultiLettuceConnectionConfiguration.class)
public class MultiRedisAutoConfiguration {
}
