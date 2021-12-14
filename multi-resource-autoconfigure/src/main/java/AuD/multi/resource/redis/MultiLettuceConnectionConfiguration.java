package AuD.multi.resource.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * 配置多个redis连接,完全参考"RedisAutoConfiguration",请务必熟悉"RedisAutoConfiguration"原理/流程. <br>
 *
 * 需要结合{@link MultiRedisInitializer}理解.
 *
 * @author AuD/胡钊
 * @ClassName MultiLettuceConnectionConfiguration
 * @date 2021/7/15 17:04
 * @Version 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(prefix = "multi.redis",name = "condition",havingValue = "true")
class MultiLettuceConnectionConfiguration extends MultiRedisConnectionConfiguration {

    /**
     * <p>
     * 包访问权限属性,被{@link MultiRedisInitializer.MultiRedisBeanPostProcessor#postProcessBeforeInitialization(Object,String)}使用.
     * </p>
     * <p> 多数据源beanName </p>
     */
    final static String MRCF = "multiRedisConnectionFactoryBeanMappingRelate";

    private MultiRedisProperties multiRedisProperties;

    public MultiLettuceConnectionConfiguration(MultiRedisProperties multiRedisProperties){
        super();
        this.multiRedisProperties = multiRedisProperties;
    }


    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(ClientResources.class)
    DefaultClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    /**
     * 往容器中注册 multi-RedisConnectionFactory,
     * 将其封装为一个Map对象
     *
     * @param clientResources
     * @return
     */
    @Bean(name = MRCF)
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    Map<String, LettuceConnectionFactory> multiRedisConnectionFactory(ClientResources clientResources){
        // == 获取资源配置(redis配置信息集合)
        Map<String, MultiRedisProperties.RedisPropertiesPlus> resource = multiRedisProperties.getResource();
        // == 防止 "resource == null" 情况,即警告caller,没有配置redis资源信息
        Assert.notNull(resource,"redis connection info must not empty");
        Map<String, LettuceConnectionFactory> multiRedisConnectionFactory = new HashMap<>(resource.size());
        /** 对配置资源循环遍历,为每个资源创建RedisConnectionFactory */
        resource.forEach((key,redisProperties)->{
            /** 1.获取 lettuceClientConfiguration ,通过配置信息redisProperties */
            LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(clientResources,redisProperties);
            /** 2.根据由步骤1获取的clientConfig 创建 lettuceConnectionFactory */
            LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(clientConfig, redisProperties);
            multiRedisConnectionFactory.put(key, lettuceConnectionFactory);
        });
        return multiRedisConnectionFactory;
    }


    private LettuceConnectionFactory createLettuceConnectionFactory(LettuceClientConfiguration clientConfiguration,RedisProperties redisProperties) {
        if (getSentinelConfig(redisProperties) != null) {
            return new LettuceConnectionFactory(getSentinelConfig(redisProperties), clientConfiguration);
        }
        if (getClusterConfiguration(redisProperties) != null) {
            return new LettuceConnectionFactory(getClusterConfiguration(redisProperties), clientConfiguration);
        }
        return new LettuceConnectionFactory(getStandaloneConfig(redisProperties), clientConfiguration);
    }

    private LettuceClientConfiguration getLettuceClientConfiguration(ClientResources clientResources,RedisProperties redisProperties){
        RedisProperties.Pool pool = multiRedisProperties.getPublicPool();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = null;
        /** 判断是否配置公用pool信息,如果配置,则采用publicPool(即使各个内容也配置pool) */
        if(pool!=null){
            builder = createBuilder(pool);
        }else {
            builder = createBuilder(redisProperties.getLettuce().getPool());
        }
        applyProperties(builder,redisProperties);
        if (StringUtils.hasText(redisProperties.getUrl())) {
            customizeConfigurationFromUrl(builder,redisProperties);
        }
        builder.clientResources(clientResources);
        return builder.build();
    }


    private LettuceClientConfiguration.LettuceClientConfigurationBuilder applyProperties(
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder,RedisProperties redisProperties) {
        if (redisProperties.isSsl()) {
            builder.useSsl();
        }
        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }
        if (redisProperties.getLettuce() != null) {
            RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
            }
        }
        if (StringUtils.hasText(redisProperties.getClientName())) {
            builder.clientName(redisProperties.getClientName());
        }
        return builder;
    }

    private void customizeConfigurationFromUrl(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder,RedisProperties properties) {
        ConnectionInfo connectionInfo = parseUrl(properties.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }


    private LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool pool) {
        if (pool == null) {
            return LettuceClientConfiguration.builder();
        }
        return new PoolBuilderFactory().createBuilder(pool);
    }




    /**
     * Inner class to allow optional commons-pool2 dependency.
     */
    private static class PoolBuilderFactory {

        LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool properties) {
            return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(properties));
        }

        private GenericObjectPoolConfig<?> getPoolConfig(RedisProperties.Pool properties) {
            GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(properties.getMaxActive());
            config.setMaxIdle(properties.getMaxIdle());
            config.setMinIdle(properties.getMinIdle());
            if (properties.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRuns().toMillis());
            }
            if (properties.getMaxWait() != null) {
                config.setMaxWaitMillis(properties.getMaxWait().toMillis());
            }
            return config;
        }

    }


}
