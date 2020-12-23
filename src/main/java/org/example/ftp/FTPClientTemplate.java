package org.example.ftp;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class FTPClientTemplate implements FTPClientOperations {
    private static final Logger logger = Logger.getLogger(FTPClientTemplate.class);

    private static String DEAFULT_REMOTE_CHARSET = "UTF-8";
    private static int DEAFULT_REMOTE_PORT = 21;
    private static String separator = File.separator;
    private FTPClientConfig ftpClientConfig;
    private String host;
    private String username;
    private String password;
    private String port;

    public FTPClientTemplate(String host, String user, String pwd, String port) {
        this.host = host;
        this.username = user;
        this.password = pwd;
        this.port = port;
    }

    /**
     * 查看服务器上文件列表方法
     *
     * @param remotedir
     * @return
     * @throws IOException
     */
    public FTPFile[] list(final String remotedir) throws IOException {
        return execute(new FTPClientCallback<FTPFile[]>() {
            public FTPFile[] doTransfer(FTPClient ftp) throws IOException {
                ftp.changeWorkingDirectory(remotedir);
                FTPFile[] files = ftp.listFiles(remotedir);
                return files;
            }
        });
    }

    /**
     * 文件上传的方法
     *
     * @param remote
     * @param local
     * @return
     * @throws IOException
     */
    public UploadStatus upload(final String local, final String remote) throws IOException {
        return execute(new FTPClientCallback<UploadStatus>() {
            public UploadStatus doTransfer(FTPClient ftpClient) throws IOException {
                return FtpHelper.getInstance().upload(ftpClient, local, remote);
            }
        });
    }

    /**
     * 上传文件到服务器,新上传和断点续传
     *
     * @param remoteFile  远程文件名，在上传之前已经将服务器工作目录做了改变
     * @param localFile   本地文件File句柄，绝对路径
     * @param processStep 需要显示的处理进度步进值
     * @param ftpClient   FTPClient引用
     * @return
     * @throws IOException
     */
    public UploadStatus uploadFile(String remoteFile, File localFile,
                                   FTPClient ftpClient, long remoteSize) throws IOException {
        return FtpHelper.getInstance().uploadFile(remoteFile, localFile, ftpClient, remoteSize);
    }


    /**
     * 从远程服务器目录下载文件到本地服务器目录中
     *
     * @param localdir              FTP服务器保存目录
     * @param remotedir             FTP下载服务器目录
     * @param localTempFile临时下载记录文件
     * @return 成功下载记录
     */
    public Collection<String> downloadList(final String localdir, final String remotedir, final String localTmpFile) throws IOException {
        return execute(new FTPClientCallback<Collection<String>>() {
            public Collection<String> doTransfer(final FTPClient ftp) throws IOException {
                //切换到下载目录的中
                ftp.changeWorkingDirectory(remotedir);
                //获取目录中所有的文件信息
                FTPFile[] ftpfiles = ftp.listFiles();
                Collection<String> fileNamesCol = new ArrayList<String>();
                //判断文件目录是否为空
                if (!ArrayUtils.isEmpty(ftpfiles)) {
                    for (FTPFile ftpfile : ftpfiles) {
                        String remoteFilePath = remotedir + separator + ftpfile.getName();
                        String localFilePath = localdir + separator + ftpfile.getName();
                        System.out.println("remoteFilePath =" + remoteFilePath + " localFilePath=" + localFilePath);
                        //单个文件下载状态
//                        DownloadStatus downStatus=downloadFile(remoteFilePath, localFilePath);
                        DownloadStatus result = FtpHelper.getInstance().download(ftp, remoteFilePath, localFilePath);
                        if (result == DownloadStatus.Download_New_Success) {
                            //临时目录中添加记录信息
                            fileNamesCol.add(remoteFilePath);
                        }
                    }
                }
//                if(CollectionUtils.isNotEmpty(fileNamesCol)){
//                    FileOperateUtils.writeLinesToFile(fileNamesCol, localTmpFile);
//                }
                return fileNamesCol;
            }
        });
    }

    /**
     * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报
     *
     * @param remote 远程文件路径
     * @param local  本地文件路径
     * @return 上传的状态
     * @throws IOException
     */
    public DownloadStatus downloadFile(final String remote, final String local) throws IOException {
        return execute(new FTPClientCallback<DownloadStatus>() {
            public DownloadStatus doTransfer(FTPClient ftpClient) throws IOException {
                DownloadStatus result = FtpHelper.getInstance().download(ftpClient, remote, local);
                return result;
            }
        });
    }

    /**
     * 执行FTP回调操作的方法
     *
     * @param callback 回调的函数
     * @throws IOException
     */
    public <T> T execute(FTPClientCallback<T> callback) throws IOException {
        FTPClient ftp = new FTPClient();
        try {

            /*if(getFtpClientConfig()!=null){
                 ftp.configure(getFtpClientConfig());
                 ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
            }*/
            //登录FTP服务器
            try {
                //设置超时时间
                ftp.setDataTimeout(7200);
                //设置默认编码
                ftp.setControlEncoding(DEAFULT_REMOTE_CHARSET);
                //设置默认端口
                ftp.setDefaultPort(DEAFULT_REMOTE_PORT);
                //设置被动模式
                ftp.enterLocalPassiveMode();
                //设置是否显示隐藏文件
                ftp.setListHiddenFiles(false);
                //连接ftp服务器
                if (StringUtils.isNotEmpty(port) && NumberUtils.isDigits(port)) {
                    ftp.connect(host, Integer.valueOf(port));
                } else {
                    ftp.connect(host);
                }
            } catch (ConnectException e) {
                logger.error("连接FTP服务器失败：" + ftp.getReplyString() + ftp.getReplyCode());
                throw new IOException("Problem connecting the FTP-server fail", e);
            }
            //得到连接的返回编码
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
            }
            //登录失败权限验证失败
            if (!ftp.login(getUsername(), getPassword())) {
                ftp.quit();
                ftp.disconnect();
                logger.error("连接FTP服务器用户或者密码失败：：" + ftp.getReplyString());
                throw new IOException("Cant Authentificate to FTP-Server");
            }
            if (logger.isDebugEnabled()) {
                logger.info("成功登录FTP服务器：" + host + " 端口：" + port);
            }
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            //回调FTP的操作
            return callback.doTransfer(ftp);
        } finally {
            //FTP退出
            ftp.logout();
            //断开FTP连接
            if (ftp.isConnected()) {
                ftp.disconnect();
            }
        }
    }

    protected String resolveFile(String file) {
        return null;
        //return file.replace(System.getProperty("file.separator").charAt(0), this.remoteFileSep.charAt(0));
    }

    /**
     * 获取FTP的配置操作系统
     *
     * @return
     */
    public FTPClientConfig getFtpClientConfig() {
        //获得系统属性集
        Properties props = System.getProperties();
        //操作系统名称
        String osname = props.getProperty("os.name");
        //针对window系统
        if (osname.equalsIgnoreCase("Windows XP")) {
            ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_NT);
            //针对linux系统
        } else if (osname.equalsIgnoreCase("Linux")) {
            ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        }
        if (logger.isDebugEnabled()) {
            logger.info("the ftp client system os Name " + osname);
        }
        return ftpClientConfig;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
