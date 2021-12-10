package AuD.ssh.forward;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.File;

/**
 * Description: SFTP客户端,包访问权限,仅仅提供代码方面的参考.
 *
 * @author AuD/胡钊
 * @ClassName SFTPClient
 * @date 2021/5/18 10:20
 * @Version 1.0
 */
public class SFTPClient extends SSHSession {

    private static final String SFTP = "sftp";


    /** 初始化ssh会话 == 参见{@link SSHSession#initSshSession()} */
    public SFTPClient(SSHSessionInfo sshSessionInfo){
        super(sshSessionInfo);
        initSshSession();
    }


    /**
     * 上传文件到SFTP
     * @param localFile
     * @param remoteFile
     */
    public boolean upload(String localFile, String remoteFile){
        ChannelSftp channel = null;
        boolean res = false;
        try {
            if(!session.isConnected()){
                connect();  //尝试再次连接
            }
            channel = (ChannelSftp)session.openChannel(SFTP);
            channel.connect();
            channel.put(localFile, remoteFile);
            res = true;
        }catch (JSchException | SftpException e){
            log.warn("upload process occur error,info is:{}",e.getMessage());
        }finally {
            if(channel!=null){
                channel.disconnect();
            }
            return res;
        }
    }

    /**
     * 上传文件到SFTP,通过目录形式,即将某个目录下的文件上传到指定的远程目录
     * @param localDir
     * @param remoteDir
     */
    public boolean uploadByDir(String localDir, String remoteDir){
        ChannelSftp channel = null;
        boolean res =false;
        try {
            if(!session.isConnected()){
                connect();  //尝试再次连接
            }
            channel = (ChannelSftp)session.openChannel(SFTP);
            channel.connect();
            cd(channel,remoteDir);
            File local = new File(localDir);
            /* ===== 遍历文件对象,并且过滤文件夹 ===== */
            for (File file : local.listFiles(file->!file.isDirectory())) {
                channel.put(file.getAbsolutePath(),remoteDir+File.separator+file.getName());
            }
            res = true;
        }catch (JSchException | SftpException e){
            log.warn("upload process occur error,info is:{}",e.getMessage());
        }finally {
            if(channel!=null){
                channel.disconnect();
            }
            return res;
        }
    }

    /**
     * 进入远程目录下(或者创建目录),通过判断异常信息是否为"No such file"来确定该目录是否不存在
     * 备注:
     * "No such file" 信息来自版本 jsch_0.1.55
     */
    private void cd(ChannelSftp channel,String remoteDir) throws SftpException {
        try {
            channel.cd(remoteDir);
        }catch (Exception e){
            if("No such file".equals(e.getMessage())){
                channel.mkdir(remoteDir);
            }
        }
    }



}
