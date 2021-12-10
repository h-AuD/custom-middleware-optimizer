package AuD.ssh.forward;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Description: 端口转发初始化器. <br>
 * <p> 参考{@link SpringApplication} 生命周期(流程) </p>
 * -- 即希望某个bean(sshForward)提前被创建,这样做的目的是确保(100%)端口转发在datasource初始化之前. <br>
 *
 *
 *
 * @author AuD/胡钊
 * @ClassName ForwardingInitializer
 * @date 2021/7/13 11:38
 * @Version 1.0
 */
public class ForwardingInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ForwardingInitializer.class);


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final String configPath = applicationContext.getEnvironment().getProperty("ssh.forward.config.absolute.path", String.class);
        if(StringUtils.hasText(configPath)){
            // build ssh forward config
            SSHForwardingProperties sshForwardingProperties = buildForwardProperties(configPath);
            /* 设置jumper server,转发对象可以有多个,并且它们可以拥有相同的jumper server */
            sshForwardingProperties.setJumperServer();
            /* 执行端口转发(在之前会连接到jumper server上,如果使用的是同一个jumper server,则ssh连接只会执行一次) */
            sshForwardingProperties.getForwards().forEach(forwardProperties -> new SSHForwarding(forwardProperties));
        }
    }


    /**
     *
     */
    public SSHForwardingProperties buildForwardProperties(String configPath){
        SSHForwardingProperties sshForwardingProperties = new SSHForwardingProperties();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            sshForwardingProperties = objectMapper.readValue(new File(configPath), SSHForwardingProperties.class);;
        }catch (Exception e){
            // TODO do something
            log.warn("build ForwardProperties occur error:{}",e.getMessage());
        }
        return sshForwardingProperties;
    }







}
