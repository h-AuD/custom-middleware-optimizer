package AuD.ssh.forward;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Description: 端口转发模型
 *
 * @author AuD/胡钊
 * @ClassName ForwardingInfo
 * @date 2021/12/9 0:15
 * @Version 1.0
 */
class ForwardingInfo {

    /**
     * 跳板机信息
     */
    private SSHSessionInfo jumper;

    /**
     * 目标端口转发信息列表
     */
    private List<ForwardTargetInfo> targets;


    public SSHSessionInfo getJumper() {
        return jumper;
    }

    public void setJumper(SSHSessionInfo jumper) {
        this.jumper = jumper;
    }

    public List<ForwardTargetInfo> getTargets() {
        return targets;
    }

    public void setTargets(List<ForwardTargetInfo> targets) {
        this.targets = targets;
    }

    /**
     * 端口转发目标 信息 ==== 必须为static,对于{@link ObjectMapper#readValue(java.io.File, java.lang.Class)}.    <br>
     * objectMapper反序列化对象,内部类需要为static. ====> 对比spring boot {@link ConfigurationProperties},内部对象属性 为 public static. <br>
     * --> 为什么有这样的限制?是什么原因导致的?
     */
    static class ForwardTargetInfo{

        /** 端口转发需要使用的 本地端口 */
        private int localPort;

        /** 端口转发 目标主机 */
        private String remoteHost;

        /** 端口转发 目标端口 */
        private int remotePort;


        public int getLocalPort() {
            return localPort;
        }

        public void setLocalPort(int localPort) {
            this.localPort = localPort;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

        public void setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
        }

        public int getRemotePort() {
            return remotePort;
        }

        public void setRemotePort(int remotePort) {
            this.remotePort = remotePort;
        }
    }
}
