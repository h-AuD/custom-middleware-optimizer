package AuD.ssh.forward;


import org.springframework.util.StringUtils;

/**
 * Description: ssh基本连接信息 -- 主机名(IP):host、登陆用户名 & 密码(or密钥):userName & password(permAbsolutePath)
 *
 * @author AuD/胡钊
 * @ClassName SSHSessionInfo
 * @date 2021/12/8 17:16
 * @Version 1.0
 */
class SSHSessionInfo {

    /** 默认的主机名 */
    private static final String DEFAULT_HOST = "localhost";

    /** ssh连接的主机名称 === 默认本机 参见{@link SSHSessionInfo#getHost()} */
    private String host;

    /** ssh连接的用户名,默认"root" */
    private String userName = "root";

    /** ssh连接的端口号 === 默认22 */
    private int port = 22;

    /** ssh连接的密码 */
    private String password = "";

    /** ssh连接 密钥文件绝对路径 */
    private String permAbsolutePath = "";

    /** 是否完成初始化 的一个标志量 */
    protected boolean flag;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPermAbsolutePath() {
        return permAbsolutePath;
    }

    public void setPermAbsolutePath(String permAbsolutePath) {
        this.permAbsolutePath = permAbsolutePath;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost(){
        if(!StringUtils.hasText(this.host)){
            this.host = DEFAULT_HOST;
        }
        return this.host;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
