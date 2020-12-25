package org.example.ftp;//package org.example.ftp;
//
//
//import com.jcraft.jsch.*;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.math.NumberUtils;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPClientConfig;
//import org.apache.commons.net.ftp.FTPFile;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Properties;
//
//public class SFTPClientTemplate implements FTPClientOperations {
////    private static final Logger logger = Logger.getLogger(SFTPClientTemplate.class);
//    private static final Logger logger = LoggerFactory.getLogger(SFTPClientTemplate.class);
//
//    private static String DEAFULT_REMOTE_CHARSET = "UTF-8";
//    private static int DEAFULT_REMOTE_PORT = 22;
//    private static String separator = File.separator;
//    private FTPClientConfig ftpClientConfig;
//    private String host;
//    private String username;
//    private String password;
//    private String port;
//    private static String CHANNEL_TYPE = "sftp";
//    private static int DEFAULT_TIMEOUT = 7200;
//
//    public SFTPClientTemplate(String host, String user, String pwd, String port) {
//        this.host = host;
//        this.username = user;
//        this.password = pwd;
//        this.port = port;
//    }
//
//    /**
//     * 查看服务器上文件列表方法
//     *
//     * @param remotedir
//     * @return
//     * @throws IOException
//     */
//    public String[] list(final String remotedir) throws IOException {
//        return execute(new SFTPClientCallback<String[]>() {
//            @Override
//            public String[] doTransfer(ChannelSftp sftp) throws IOException {
//                return new String[0];
//            }
//        });
//    }
//
//    /**
//     * 文件上传的方法
//     *
//     * @param remote
//     * @param local
//     * @return
//     * @throws IOException
//     */
//    public UploadStatus upload(final String local, final String remote) throws IOException {
//        return execute(new SFTPClientCallback<UploadStatus>() {
//            public UploadStatus doTransfer(ChannelSftp sftp) throws IOException {
//                return FtpHelper.getInstance().upload(ftpClient, local, remote);
//            }
//        });
//    }
//
//
//
//    /**
//     * 从远程服务器目录下载文件到本地服务器目录中
//     *
//     * @param localdir              FTP服务器保存目录
//     * @param remotedir             FTP下载服务器目录
//     * @param localTempFile临时下载记录文件
//     * @return 成功下载记录
//     */
//    public Collection<String> downloadList(final String localdir, final String remotedir, final String localTmpFile) throws IOException {
//        return execute(new SFTPClientCallback<Collection<String>>() {
//            @Override
//            public Collection<String> doTransfer(ChannelSftp sftp) throws IOException {
//                return null;
//            }
//        });
//    }
//
//    /**
//     * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报
//     *
//     * @param remote 远程文件路径
//     * @param local  本地文件路径
//     * @return 上传的状态
//     * @throws IOException
//     */
//    public DownloadStatus downloadFile(final String remote, final String local) throws IOException {
//        return execute(new SFTPClientCallback<DownloadStatus>() {
//            public DownloadStatus doTransfer(ChannelSftp sftp) throws IOException {
//                DownloadStatus result = FtpHelper.getInstance().download(ftpClient, remote, local);
//                return result;
//            }
//        });
//    }
//
//    /**
//     * 执行FTP回调操作的方法
//     *
//     * @param callback 回调的函数
//     * @throws IOException
//     */
//    public <T> T execute(SFTPClientCallback<T> callback) throws IOException {
//        Session session = null;
//        ChannelSftp sftp = null;
//        try {
//
//            /*if(getFtpClientConfig()!=null){
//                 ftp.configure(getFtpClientConfig());
//                 ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
//            }*/
//            //登录FTP服务器
//            try {
//                JSch jsch = new JSch();
//                int port;
//                if (StringUtils.isNotEmpty(this.port) && NumberUtils.isDigits(this.port)) {
//                    port = Integer.valueOf(this.port);
//                } else {
//                    port = Integer.valueOf(DEAFULT_REMOTE_PORT);
//                }
//                session = jsch.getSession(username, host, port);
//
//                //--設置session屬性
//                //設置超時時間
//                session.setTimeout(7200);
//
//                //開啟會話
//                session.connect();
//
//                //開啟通道
//                Channel channel = session.openChannel(CHANNEL_TYPE);
//                channel.connect();
//
//                //SFTP操作物件
//                sftp = (ChannelSftp) channel;
//
//                //连接ftp服务器
//            } catch (JSchException e) {
//                logger.error("連接SFTP失敗：" + e.getMessage());
//                throw new IOException("Problem connecting the FTP-server fail", e);
//            }
//
//            //回調SFTP的操作
//            return callback.doTransfer(sftp);
//        } finally {
//            if (sftp != null) {
//                if (sftp.isConnected()) sftp.exit();
//            }
//            if (session != null) {
//                if (session.isConnected()) session.disconnect();
//            }
//        }
//    }
//
//    protected String resolveFile(String file) {
//        return null;
//        //return file.replace(System.getProperty("file.separator").charAt(0), this.remoteFileSep.charAt(0));
//    }
//
//    /**
//     * 获取FTP的配置操作系统
//     *
//     * @return
//     */
//    public FTPClientConfig getFtpClientConfig() {
//        //获得系统属性集
//        Properties props = System.getProperties();
//        //操作系统名称
//        String osname = props.getProperty("os.name");
//        //针对window系统
//        if (osname.equalsIgnoreCase("Windows XP")) {
//            ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_NT);
//            //针对linux系统
//        } else if (osname.equalsIgnoreCase("Linux")) {
//            ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
//        }
//        if (logger.isDebugEnabled()) {
//            logger.info("the ftp client system os Name " + osname);
//        }
//        return ftpClientConfig;
//    }
//
//
//    public String getHost() {
//        return host;
//    }
//
//    public void setHost(String host) {
//        this.host = host;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//
//}
