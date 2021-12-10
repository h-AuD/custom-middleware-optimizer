package AuD.ssh.forward;


import java.util.List;

/**
 * Description: SSH端口转发配置.
 *
 *
 * @author AuD/胡钊
 * @ClassName SSHForwardingProperties
 * @date 2021/7/15 10:00
 * @Version 1.0
 */
class SSHForwardingProperties {

    /**
     * jumper全局配置
     */
    private SSHSessionInfo globalHostInfo;

    /** ssh端口转发属性配置,必须为静态内部类 */
    private List<ForwardingInfo> forwards;

    /**
     * 设置{@link ForwardingInfo}的jumper信息.
     * -- 即当{@code ForwardingInfo#jumper}为null时,将{@code globalHostInfo}赋给jumper.
     */
    void setJumperServer(){
        this.forwards.stream().filter(forwardingInfo -> forwardingInfo.getJumper()==null)
                .forEach(forwardingInfo -> forwardingInfo.setJumper(this.globalHostInfo));
    }

    public SSHSessionInfo getGlobalHostInfo() {
        return globalHostInfo;
    }

    public void setGlobalHostInfo(SSHSessionInfo globalHostInfo) {
        this.globalHostInfo = globalHostInfo;
    }

    public List<ForwardingInfo> getForwards() {
        return forwards;
    }

    public void setForwards(List<ForwardingInfo> forwards) {
        this.forwards = forwards;
    }
}
