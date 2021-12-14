package AuD.multi.resource.utils;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Description: TODO
 *
 * @author AuD/胡钊
 * @ClassName SpringEnvironmentUtils
 * @date 2021/12/14 16:19
 * @Version 1.0
 */
public class SpringEnvironmentUtils {

    /**
     * 自动配置类排除的信号属性名
     */
    private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";


    /**
     * 添加spring boot 自动配置排除项.
     *
     * @param environment
     * @param autoconfigureClassName    需要排除的自动配置类全限定名
     */
    public static void addAutoconfigureExcludes(ConfigurableEnvironment environment,String autoconfigureClassName){
        // 1.尝试从 environment 中获取自动配置类排除项的属性名
        String[] excludes = environment.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class);
        // 2.判断 excludes 是否存在?
        // 不存在 -> excludes = {autoconfigureClass},存在&不包含目标属性 -> excludes = {excludes + autoconfigureClass}
        if(ObjectUtils.isEmpty(excludes)){
            // excludes为null
            excludes = new String[]{autoconfigureClassName};
        }else if(!Arrays.asList(excludes).contains(autoconfigureClassName)) {
            // excludes存在时,并且excludes不包含redisAutoConfigure
            String[] tmp = new String[excludes.length+1];
            System.arraycopy(excludes,0,tmp,0,excludes.length);
            tmp[excludes.length] = autoconfigureClassName;
            excludes = tmp;
        }
        // 当 excludes 不为空时,才往 environment 中添加属性源(PropertySources)
        if(!ObjectUtils.isEmpty(excludes)){
            // 创建自动配置排除源(spring.autoconfigure.exclude)
            Map<String,Object> excludeSource = new HashMap<>();
            excludeSource.put(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,excludes);
            environment.getPropertySources().addFirst(new MapPropertySource(UUID.randomUUID().toString().replace("-",""),excludeSource));
        }


    }


}
