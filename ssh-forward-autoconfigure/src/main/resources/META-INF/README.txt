shell隧道功能 == 提供本地端口转发,对于用户(使用者)而言,仅仅需要配置相关信息即可.

说明:
通常自己封装"jsch"对象(function),在web容器启动时,开启端口转发,参考使用接口"ServletContextListener".
eg:
@WebListener
public class SSHConnectionListener implements ServletContextListener {

    @Resource
    private SSHConnection sshConnection;    // 自己封装一个ssh组件(即jsch)

    /**
     * 监听Servlet初始化事件
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // 建立连接
        try {
            sshConnection.createSshAndForwardL();
            log.info("成功建立SSH连接！");
        } catch (Throwable e) {
            log.info("SSH连接失败！");
            e.printStackTrace(); // error connecting SSH server
        }
    }

    /**
     * 监听Servlet终止事件
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // 断开连接
        try {
            sshConnection.closeSSH(); // disconnect
            log.info("成功断开SSH连接!");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("断开SSH连接出错！");
        }
    }
}

==============================>
上述做法,在某些场景下可能会出现问题,eg:multi-datasource场景.
通常多数据源由开放着自己配置的,在这种情况,不一定能保证上述动作会在datasource初始化之前完成.
如果datasource先一步执行,则会出现连接异常,因为端口转发未执行,datasource却开始连接,所以肯定是连不上的.
-- 如果对springMVC、springboot启动流程非常熟悉的话,依然是可以完成需求的.但是这种对技术要求还是比较高,并不是每个人有如此技能.
-- 因此ssh-autoconfig来了.

使用教程: 在spring配置文件中设置相关属性即可.
eg:
## 开启shell隧道连接
ssh.forward.switch=true
## 跳板机(jump)配置
ssh.resource.host=192.168.132.101
ssh.resource.user-name=centos
ssh.resource.port=22
## 注意:密钥和密码只会使用其中一个,优先使用密钥
ssh.forward.resource.password=123
ssh.forward.resource.perm-absolute-path=/xxx.perm
ssh.forward.resource.is-exit=true   ## ssh连接出现异常,是否终止Application,默认true

## 端口转发的目标信息,考虑到肯能存在多个数据源配置,有个targets集合配置,如下:(注意,如果即配置集合targets,又配置了target,优先以集合为主,target会被过滤掉)
ssh.forward.is-exit=true   ## 端口转发连接出现异常,是否终止Application,默认true
ssh.forward.target.local-port=3306  ## 本地端口
ssh.forward.target.remote-host=xxxx ## 目标地址
ssh.forward.target.remote-port=3306 ## 目标端口
ssh.forward.targets[0].local-port=1433
ssh.forward.targets[0].remote-host=xxx
ssh.forward.targets[0].remote-port=1433
ssh.forward.targets[1].local-port=1434
ssh.forward.targets[1].remote-host=xxx
ssh.forward.targets[1].remote-port=1434

starter执行流程如下:
