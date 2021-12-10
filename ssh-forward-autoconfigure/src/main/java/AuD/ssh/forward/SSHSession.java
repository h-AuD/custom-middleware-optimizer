package AuD.ssh.forward;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * Description: ssh会话基本function,eg.初始化连接,关闭连接
 *
 * @author AuD/胡钊
 * @ClassName SSHSession
 * @date 2021/7/13 15:41
 * @Version 1.0
 */
abstract class SSHSession {

    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SSHSession.class);

    /**
     * JSch使用Session来定义一个远程节点
     */
    protected Session session;


    /**
     * ssh 服务器(通常是jumper server)连接信息.   <br>
     * note: 这里没有setter,禁止set {@link SSHSessionInfo}
     */
    protected SSHSessionInfo sshSessionInfo;


    public SSHSession(SSHSessionInfo sshSessionInfo){
        this.sshSessionInfo = sshSessionInfo;
    }


    /**
     * 初始化化{@link Session},先实例化,再建立ssh连接
     * 优先使用密钥,如果密钥为空,其次使用密码
     */
    public void initSshSession() {
        // === 验证当前主机是否已经完成连接
        if(!sshSessionInfo.isFlag()){
            doInitSession();
        }
    }


    private void doInitSession(){
        synchronized (sshSessionInfo){
            try {
                JSch jsch = new JSch();
                Properties config = new Properties();
                config.put("StrictHostKeyChecking","no");
                /** 当host为null时,getSession()抛异常,但是默认配置host为localhost,所以成员变量{@link SSHSession#session 不会为null} */
                session = jsch.getSession(sshSessionInfo.getUserName(), sshSessionInfo.getHost(), sshSessionInfo.getPort());
                String permPath = sshSessionInfo.getPermAbsolutePath();
                String password = sshSessionInfo.getPassword();
                if(StringUtils.hasText(permPath)){
                    jsch.addIdentity(permPath);
                    config.put("PreferredAuthentications","publickey");
                }else if(StringUtils.hasText(password)){
                    session.setPassword(password);
                }
                session.setConfig(config);
                // 如果密码/密钥为空、无效,此处会发送异常
                connect();
                this.sshSessionInfo.setFlag(true);
                log.info("SSH session establish connection,and the connection status is {}",session.isConnected());
            }catch (JSchException jSchException){
                log.warn("SSH session connection occur error,exception info:{},And ssh_config info:{}",jSchException.getMessage(), sshSessionInfo);
                throw new RuntimeException(jSchException.getMessage());
            }
        }
    }


    /** 连接 */
    public void connect() throws JSchException {
        if(this.session!=null && !session.isConnected()){
            this.session.connect();
        }
    }

    /** 关闭连接 */
    public void disconnect(){
        if(this.session!=null){
            this.session.disconnect();
        }
    }

    /**
     * host info getter.
     * @return
     */
    protected SSHSessionInfo getSshConnectInfo(){
        return sshSessionInfo;
    }


}
