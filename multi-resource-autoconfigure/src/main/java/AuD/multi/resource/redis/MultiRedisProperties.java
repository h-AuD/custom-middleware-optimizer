package AuD.multi.resource.redis;


import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Description: 多数据源属性配置,即通过spring配置文件(spring environment)
 *
 * @author AuD/胡钊
 * @ClassName MultiRedisProperties
 * @date 2021/7/15 11:52
 * @Version 1.0
 */
@ConfigurationProperties(prefix = "multi.redis")
public class MultiRedisProperties {

    /** 是否开启配置条件(i.e.当condition=true时,multiRedis才会生效),默认为false */
    private boolean flag = Boolean.FALSE;

    /**
     * redis具体配置信息. -- Map类型(Map<String,Object>) <br>
     * key为beanName prefix,eg.某个key为'db',则该key对应的redisTemplate beanName为 'dbRedisTemplate'
     *
     */
    private Map<String, RedisPropertiesPlus> resource;

    /**
     * 连接池配置信息(公用的)
     */
    private RedisProperties.Pool publicPool;


    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Map<String, RedisPropertiesPlus> getResource() {
        return resource;
    }

    public void setResource(Map<String, RedisPropertiesPlus> resource) {
        this.resource = resource;
    }

    public RedisProperties.Pool getPublicPool() {
        return publicPool;
    }

    public void setPublicPool(RedisProperties.Pool publicPool) {
        this.publicPool = publicPool;
    }

    /** 重利用 {@link } 配置信息 */
    public static class RedisPropertiesPlus extends RedisProperties {

    }

}
