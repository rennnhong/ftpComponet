package org.example.ftp;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class FTPClientTemplate {
    private static final Logger logger = LoggerFactory.getLogger(FTPClientTemplate.class);
//    private static final Logger logger = Logger.getLogger(FTPClientTemplate.class);

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
     * 查看服務器上文件列表方法
     *
     * @param remotedir
     * @return
     * @throws IOException
     */
    public List<String> list(final String remotedir) throws IOException {
        return execute(ftpOperations -> {
            ftpOperations.move(remotedir);
            return ftpOperations.list();
        });
    }

    /**
     * 文件上傳的方法
     *
     * @param localFilePath
     * @param remoteDirPath
     * @return
     * @throws IOException
     */
    public UploadStatus upload(final String localFilePath, final String remoteDirPath) throws IOException {
        return execute(ftpOperations -> {
            ftpOperations.move(remoteDirPath);
            Path localPath = Paths.get(localFilePath);
            Path localDirPath = localPath.getParent();
            return ftpOperations.upload(localPath.getFileName().toString(), localDirPath.toString());
        });
    }

    /**
     * 上傳文件到服務器,新上傳和斷點續傳
     *
     * @param remoteFile 遠程文件名，在上傳之前已經將服務器工作目錄做了改變
     * @param localFile  本地文件File句柄，絕對路徑
     * @param ftpClient  FTPClient引用
     * @return
     * @throws IOException
     */
    public UploadStatus uploadFile(String remoteFile, File localFile,
                                   FTPClient ftpClient, long remoteSize) throws IOException {
        return FtpHelper.getInstance().uploadFile(remoteFile, localFile, ftpClient, remoteSize);
    }


    /**
     * 從遠程服務器目錄下載文件到本地服務器目錄中
     *
     * @param localdir     FTP服務器保存目錄
     * @param remotedir    FTP下載服務器目錄
     * @param localTmpFile 臨時下載記錄文件
     * @return 成功下載記錄
     */
    public Collection<String> downloadList(final String localdir, final String remotedir, final String localTmpFile) throws IOException {
        return execute(ftpOperations -> {
            //切換到下載目錄的中
            ftpOperations.move(remotedir);
            //獲取目錄中所有的文件信息
//                FTPFile[] ftpfiles = ftpOperations.list();

            String[] fileNames = Iterables.toArray(ftpOperations.list(), String.class);
            Collection<String> fileNamesCol = new ArrayList<String>();
            //判斷文件目錄是否為空
            if (!ArrayUtils.isEmpty(fileNames)) {
                for (String fileName : fileNames) {
//                        String remoteFilePath = remotedir + separator + fileName;
//                        String localFilePath = localdir + separator + fileName;
//                        System.out.println("remoteFilePath =" + remoteFilePath + " localFilePath=" + localFilePath);
                    //單個文件下載狀態
//                        DownloadStatus downStatus=downloadFile(remoteFilePath, localFilePath);
                    DownloadStatus result = ftpOperations.download(fileName, localdir);
//                        DownloadStatus result = FtpHelper.getInstance().download(ftp, remoteFilePath, localFilePath);
                    if (result == DownloadStatus.Download_New_Success) {
                        //臨時目錄中添加記錄信息
                        String remoteFilePath = remotedir + separator + fileName;
                        fileNamesCol.add(remoteFilePath);
                    }
                }
            }
//                if(CollectionUtils.isNotEmpty(fileNamesCol)){
//                    FileOperateUtils.writeLinesToFile(fileNamesCol, localTmpFile);
//                }
            return fileNamesCol;
        });
    }

    /**
     * 從FTP服務器上下載文件至指定資料夾,支持斷點續傳，上傳百分比匯報
     *
     * @param remoteFile 遠程文件路徑
     * @param localDir   本地文件資料夾路徑
     * @return 上傳的狀態
     * @throws IOException
     */
    public DownloadStatus download(final String remoteFile, final String localDir) throws IOException {
        return execute(ftpOperations -> {
            Path remotePath = Paths.get(remoteFile);
            Path remoteDir = remotePath.getParent();
            String remoteFileName = remotePath.getFileName().toString();

            ftpOperations.move(remoteDir.toString());
            return ftpOperations.download(remoteFileName, localDir);
        });
    }

    /**
     * 執行FTP回調操作的方法
     *
     * @param callback 回調的函數
     * @throws IOException
     */
    public <T> T execute(FTPClientCallback<T> callback) throws IOException {
        FTPClient ftp = new FTPClient();
        try {

            /*if(getFtpClientConfig()!=null){
                 ftp.configure(getFtpClientConfig());
                 ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
            }*/
            //登錄FTP服務器
            try {
                //設置超時時間
                ftp.setDataTimeout(7200);
                //設置默認編碼
                ftp.setControlEncoding(DEAFULT_REMOTE_CHARSET);
                //設置默認端口
                ftp.setDefaultPort(DEAFULT_REMOTE_PORT);
                //設置被動模式
                ftp.enterLocalPassiveMode();
                //設置是否顯示隱藏文件
                ftp.setListHiddenFiles(false);
                //連接ftp服務器
                if (StringUtils.isNotEmpty(port) && NumberUtils.isDigits(port)) {
                    ftp.connect(host, Integer.valueOf(port));
                } else {
                    ftp.connect(host);
                }
            } catch (ConnectException e) {
                logger.error("連接FTP服務器失敗：" + ftp.getReplyString() + ftp.getReplyCode());
                throw new IOException("Problem connecting the FTP-server fail", e);
            }
            //得到連接的返回編碼
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
            }
            //登錄失敗權限驗證失敗
            if (!ftp.login(getUsername(), getPassword())) {
                ftp.quit();
                ftp.disconnect();
                logger.error("連接FTP服務器用戶或者密碼失敗：：" + ftp.getReplyString());
                throw new IOException("Cant Authentificate to FTP-Server");
            }
            if (logger.isDebugEnabled()) {
                logger.info("成功登錄FTP服務器：" + host + " 端口：" + port);
            }
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            //回調FTP的操作
            return callback.doTransfer(new FTPClientOperationsAdapter(ftp));
        } finally {
            //FTP退出
            ftp.logout();
            //斷開FTP連接
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
     * 獲取FTP的配置操作系統
     *
     * @return
     */
    public FTPClientConfig getFtpClientConfig() {
        //獲得系統屬性集
        Properties props = System.getProperties();
        //操作系統名稱
        String osname = props.getProperty("os.name");
        //針對window系統
        if (osname.equalsIgnoreCase("Windows XP")) {
            ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_NT);
            //針對linux系統
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
