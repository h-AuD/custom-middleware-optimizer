package AuD.ssh.forward;

import com.jcraft.jsch.JSchException;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Description: ssh 端口转发组件,包访问权限,即并不希望用户直接使用this class.
 *
 * @author AuD/胡钊
 * @ClassName SSHForwarding
 * @date 2021/7/13 16:20
 * @Version 1.0
 */
class SSHForwarding extends SSHSession {

    private ForwardingInfo forwards;

    public SSHForwarding(ForwardingInfo forwards) {
        super(forwards.getJumper());
        this.forwards = forwards;
        // ==== 连接SSH服务器,并且完成端口转发
        afterPropertiesSet();
    }

    /**
     * 本地端口转发 ===> 内部(setPortForwardingL)使用的是一个守护线程完成
     *
     */
    private void createForwarding() {
        final List<ForwardingInfo.ForwardTargetInfo> targets = forwards.getTargets();
        if(ObjectUtils.isEmpty(targets)){
            throw new IllegalArgumentException("targets must not empty");
        }else {
            targets.forEach(forwardTarget -> executeForward(forwardTarget));
        }
    }

    private void executeForward(ForwardingInfo.ForwardTargetInfo targetForward){
        try {
            // this function(setPortForwardingL)有个重载方法,参考源码
            this.session.setPortForwardingL(
                    targetForward.getLocalPort(),
                    targetForward.getRemoteHost(),
                    targetForward.getRemotePort()
            );
            log.info("ssh port forward is success,ssh info:{},target info:{}",forwards.getJumper(),targetForward);
        }catch (JSchException jSchException){
            log.warn("forward appear error ===> info is:{}\r\nssh-connection info is:{},forward-target info is:{}",
                    jSchException.getMessage(),forwards.getJumper(),targetForward);
            throw new RuntimeException(jSchException.getMessage());
        }
    }


    public void destroy() throws Exception {
        disconnect();
        log.info("ssh session({}) success close",forwards.getJumper());
    }

    /**
     * 初始化: 连接ssh & 完成端口转发
     */
    private void afterPropertiesSet(){
        initSshSession();
        createForwarding();
    }

}
