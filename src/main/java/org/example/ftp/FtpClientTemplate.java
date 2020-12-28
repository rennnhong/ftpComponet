package org.example.ftp;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.example.ftp.helper.FtpHelper;
import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FtpClientTemplate implements FtpSimpleClientTemplate, FtpExecutable {
    private static final Logger logger = LoggerFactory.getLogger(FtpClientTemplate.class);

    private static String DEFAULT_REMOTE_CHARSET = "UTF-8";
    private static int DEFAULT_REMOTE_PORT = 21;
    private static int DEFAULT_TIMEOUT = 7200;

    private static String separator = File.separator;

    private final String host;
    private final String username;
    private final String password;
    private final int port;

    private String controlEncoding;
    private int timeout;
    private boolean passiveMode;

    private FtpClientTemplate(Builder builder) {
        host = builder.host;
        username = builder.username;
        password = builder.password;
        port = builder.port;
        controlEncoding = builder.controlEncoding;
        timeout = builder.timeout;
        passiveMode = builder.passiveMode;
    }

    @Override
    public List<String> list(final String remoteDir) throws IOException {
        return execute(ftpOperations -> {
            ftpOperations.move(remoteDir);
            return ftpOperations.list();
        });
    }


    @Override
    public UploadStatus upload(final String localFilePath, final String remoteDirPath) throws IOException {
        return execute(ftpOperations -> {
            ftpOperations.move(remoteDirPath);
            Path localPath = Paths.get(localFilePath);
            Path localDirPath = localPath.getParent();
            return ftpOperations.upload(localPath.getFileName().toString(), localDirPath.toString());
        });
    }

    @Override
    public UploadStatus upload(File localFile, String remoteDirPath) throws IOException {
        /* todo 待實作 */
        return null;
    }

//    /**
//     * 上傳文件到服務器,新上傳和斷點續傳
//     *
//     * @param remoteFile 遠程文件名，在上傳之前已經將服務器工作目錄做了改變
//     * @param localFile  本地文件File句柄，絕對路徑
//     * @param ftpClient  FTPClient引用
//     * @return
//     * @throws IOException
//     */
//    public UploadStatus uploadFile(String remoteFile, File localFile,
//                                   FTPClient ftpClient, long remoteSize) throws IOException {
//        return FtpHelper.getInstance().uploadFile(remoteFile, localFile, ftpClient, remoteSize);
//    }

    @Override
    public Collection<String> downloadList(final String localDir, final String remoteDir, final String localTmpFile) throws IOException {
        return execute(ftpOperations -> {
            //切換到下載目錄的中
            ftpOperations.move(remoteDir);
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
                    DownloadStatus result = ftpOperations.download(fileName, localDir);
//                        DownloadStatus result = FtpHelper.getInstance().download(ftp, remoteFilePath, localFilePath);
                    if (result == DownloadStatus.Download_New_Success) {
                        //臨時目錄中添加記錄信息
                        String remoteFilePath = remoteDir + separator + fileName;
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

    @Override
    public Collection<String> uploadList(String remoteDir, String localDir, String localTmpFile) throws Exception {
        /* todo 待實作 */
        return null;
    }

    @Override
    public DownloadStatus download(final String remoteFile, final String localDirPath) throws IOException {
        return execute(ftpOperations -> {
            Path remotePath = Paths.get(remoteFile);
            Path remoteDir = remotePath.getParent();
            String remoteFileName = remotePath.getFileName().toString();

            ftpOperations.move(remoteDir.toString());
            return ftpOperations.download(remoteFileName, localDirPath);
        });
    }

    @Override
    public DownloadStatus download(String remoteFilePath, File localDir) throws IOException {
        /* todo 待實作 */
        return null;
    }

    /**
     * 執行FTP回調操作的方法
     *
     * @param callback 回調的函數
     * @throws IOException
     */
    @Override
    public <T> T execute(FtpClientCallback<T> callback) throws IOException {
        FTPClient ftp = new FTPClient();
        try {
            /*if(getFtpClientConfig()!=null){
                 ftp.configure(getFtpClientConfig());
                 ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
            }*/
            //登錄FTP服務器
            try {
                //設置超時時間
                ftp.setDataTimeout(timeout);
                //設置默認編碼
                ftp.setControlEncoding(controlEncoding);
                //設置默認端口
                ftp.setDefaultPort(port);
                //設置被動模式
                if (passiveMode) ftp.enterLocalPassiveMode();
                //設置是否顯示隱藏文件
                ftp.setListHiddenFiles(false);
                //連接ftp服務器

                ftp.connect(host, port);

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
            if (!ftp.login(this.username, this.password)) {
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
            return callback.doTransfer(new FtpOperationsAdapter(ftp));
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

    public static class Builder {
        private int port = DEFAULT_REMOTE_PORT;
        private String controlEncoding = DEFAULT_REMOTE_CHARSET;
        private int timeout = DEFAULT_TIMEOUT;
        private boolean passiveMode = false;


        private final String host;
        private final String username;
        private final String password;
//        private final String port;

        public Builder(String host, String username, String password) {
            this.host = host;
            this.username = username;
            this.password = password;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setControlEncoding(String controlEncoding) {
            this.controlEncoding = controlEncoding;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setPassiveMode(boolean passiveMode) {
            this.passiveMode = passiveMode;
            return this;
        }

        public FtpSimpleClientTemplate build() {
            return new FtpClientTemplate(this);
        }

    }


}
