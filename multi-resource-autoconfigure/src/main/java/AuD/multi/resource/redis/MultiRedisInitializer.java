package AuD.multi.resource.redis;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: 利用spring boot启动流程中的初始化容器步骤,来完成redisTemplate注册功能.    <br>
 *
 * @author AuD/胡钊
 * @ClassName MultiRedisInitializer
 * @date 2021/7/15 17:41
 * @Version 1.0
 */
public class MultiRedisInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * 参见{@link AutoConfigurationImportSelector#getExcludeAutoConfigurationsProperty()}
     */
    private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

    /**
     * redis autoconfigure class name(redis自动配置类的全限定名)
     */
    private static final String REDIS_AUTOCONFIGURE = "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration";

    /**
     *
     * @param applicationContext
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 获取multi redis config开关,默认false.
        final Boolean propertySwitch = applicationContext.getEnvironment().getProperty("multi.redis.condition", Boolean.class,Boolean.FALSE);
        if(propertySwitch){
            /* ==================== 尝试排除原来的redis自动配置 =========================
             * autoconfigure排除的逻辑参考:
             * AutoConfigurationImportSelector#getExcludeAutoConfigurationsProperty().
             * PS:若springboot后期版本中该function逻辑发送变化,this相关逻辑也需要相对于调整.
             *
             * ========================================================================*/
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            String[] excludes = environment.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class);
            // ==== environment中存在"配置排除项"属性,并且排除内容不包含redisAutoConfigure时,才需要添加redisAutoConfigure
            if(ObjectUtils.isEmpty(excludes)){
                // excludes为null,往environment中添加属性源,其目的做自动装配的排除工作
                Map<String,Object> map = new HashMap<>();
                excludes = new String[]{REDIS_AUTOCONFIGURE};
                map.put(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,excludes);
                applicationContext.getEnvironment().getPropertySources().addFirst(new MapPropertySource(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,map));
            }else if(!Arrays.asList(excludes).contains(REDIS_AUTOCONFIGURE)) {
                // excludes存在时,并且excludes不包含redisAutoConfigure -- 用于应对user自己已经设置了redisAutoconfigure排除项.
                String[] tmp = new String[excludes.length+1];
                System.arraycopy(excludes,0,tmp,0,excludes.length);
                tmp[excludes.length] = REDIS_AUTOCONFIGURE;
                excludes = tmp;
            }
            applicationContext.getBeanFactory().addBeanPostProcessor(new MultiRedisBeanPostProcessor(applicationContext));
        }
    }

    /**
     * bean后缀处理器,往容器中注入redisTemplate
     */
    class MultiRedisBeanPostProcessor implements BeanPostProcessor {

        // ==
        private final static String SUFFIX_REDIS = "RedisTemplate";

        private final static String SUFFIX_STR_REDIS = "StringRedisTemplate";

        private final static String SUFFIX_RCF = "RedisConnectionFactory";

        private ConfigurableApplicationContext applicationContext;

        MultiRedisBeanPostProcessor(ConfigurableApplicationContext applicationContext){
            this.applicationContext = applicationContext;
        }

        /**
         * 根据是否包含指定的beanName({@link MultiLettuceConnectionConfiguration#MRCF}),来完成相应的逻辑.    <br>
         * 主要构建redisTemplate对象,并注册到spring容器中.(Note:这时注册对象并没有spring bean生命周期,仅仅往容器中put(beanName,Object)).    <br>
         *
         * @param bean
         * @param beanName
         * @return
         * @throws BeansException
         */
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            // 在bean实例化之前需要做的事情,判断当前bean是否满足条件,即 beanName == MRCF
            if(MultiLettuceConnectionConfiguration.MRCF.equals(beanName)){
                Map<String, LettuceConnectionFactory> redisConnectionFactory = (Map<String, LettuceConnectionFactory>) applicationContext.getBean(beanName);
                ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
                // == 获取 RedisSerializer 对象,由用户自己定义,并注入到spring容器中.
                // note: redisSerializer可能为null,如果容器中不存在,或者不唯一
                RedisSerializer redisSerializer = beanFactory.getBeanProvider(RedisSerializer.class).getIfUnique();
                redisConnectionFactory.forEach((key,lettuceConnectionFactory)->{
                    // 注册redisConnectionFactory?貌似没有这个必要,因为真正需要的组件只有RedisTemplate.
                    // 其实不然,最好还是注册redisConnectionFactory,这样对于user而言,可以通过redisFactory构建想要的redisTemplate对象.
                    // 因为this auto config 会自动配置 StringRedisTemplate & RedisTemplate<String,Object>
                    // 对于RedisTemplate<String,Object>,可能用户并不想要这样的,所以用户可以使用redisConnectionFactory自定义RedisTemplate
                    beanFactory.registerSingleton(key+SUFFIX_RCF,lettuceConnectionFactory);
                    beanFactory.registerSingleton(key+SUFFIX_STR_REDIS,buildString(lettuceConnectionFactory,redisSerializer));
                    beanFactory.registerSingleton(key+SUFFIX_REDIS,build(lettuceConnectionFactory,redisSerializer));
                });
                // 移除bean,释放资源 == note:该方法不适应此场景(需要从容器中移除某个bean),该方法是call bean的destroy方法
                // beanFactory.destroyBean(beanName,redisResource);
                // 释放Map资源,note:这里并没有删除redisConnectionFactory对象,因为redisConnectionFactory依然被引用,仅仅清空map而已.
                redisConnectionFactory.clear();
                // 直接返回null即可
                return null;
            }
            return bean;
        }


        private StringRedisTemplate buildString(LettuceConnectionFactory lettuceConnectionFactory,RedisSerializer redisSerializer){
            StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
            stringRedisTemplate.setConnectionFactory(lettuceConnectionFactory);
            // == 设置默认序列化方式,如果redisSerializer为null,则使用jdk序列化方式(参见"afterPropertiesSet()")
            stringRedisTemplate.setDefaultSerializer(redisSerializer);
            // == must execute below function -- 因为 stringRedisTemplate 并没有经历spring bean生命周期.
            stringRedisTemplate.afterPropertiesSet();
            return stringRedisTemplate;
        }

        private RedisTemplate<String,Object> build(LettuceConnectionFactory lettuceConnectionFactory,RedisSerializer redisSerializer){
            RedisTemplate<String,Object> redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(lettuceConnectionFactory);
            redisTemplate.setDefaultSerializer(redisSerializer);
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        }

    }


}
