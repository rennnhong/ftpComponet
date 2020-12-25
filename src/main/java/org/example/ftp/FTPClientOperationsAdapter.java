package org.example.ftp;

import org.apache.commons.net.ftp.*;
import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FTPClientOperationsAdapter implements FTPClientOperations {
    private static String DEAFULT_REMOTE_CHARSET = "UTF-8";
    private static String DEAFULT_LOCAL_CHARSET = "UTF-8";
    private static int DEAFULT_REMOTE_PORT = 21;
    private static String separator = File.separator;
//    private FTPClientConfig ftpClientConfig;
//
//    private final String host;
//    private final String username;
//    private final String password;
//    private final String port;
//
//    private final FTPClient ftp;

//    // Todo ftpclient的建構改成用builder pattern
//    public FTPClientImpl(String host, String user, String pwd, String port) throws IOException {
//        this.host = host;
//        this.username = user;
//        this.password = pwd;
//        this.port = port;
//        this.ftp = new FTPClient();
//        try {
//            /*if(getFtpClientConfig()!=null){
//                 ftp.configure(getFtpClientConfig());
//                 ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
//            }*/
//            //登錄FTP服務器
//            try {
//                //設置超時時間
//                ftp.setDataTimeout(7200);
//                //設置默認編碼
//                ftp.setControlEncoding(DEAFULT_REMOTE_CHARSET);
//                //設置默認端口
//                ftp.setDefaultPort(DEAFULT_REMOTE_PORT);
//                //設置被動模式
//                ftp.enterLocalPassiveMode();
//                //設置是否顯示隱藏文件
//                ftp.setListHiddenFiles(false);
//                //連接ftp服務器
//                this.connect();
//            } catch (IOException e) {
//                throw new IOException("Problem connecting the FTP-server fail", e);
//            }
//
//            //得到連接的返回編碼
//            int reply = ftp.getReplyCode();
//
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                disconnect();
//            }
//            //登錄失敗權限驗證失敗
//            if (!ftp.login(this.username, this.password)) {
//                disconnect();
////                logger.error("連接FTP服務器用戶或者密碼失敗：：" + ftp.getReplyString());
//                throw new IOException("Cant Authentificate to FTP-Server");
//            }
////            if (logger.isDebugEnabled()) {
////                logger.info("成功登錄FTP服務器：" + host + " 端口：" + port);
////            }
//            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
//            //回調FTP的操作
//        } finally {
//            disconnect();
//        }
//
//    }

    private final FTPClient ftp;

    public FTPClientOperationsAdapter(FTPClient ftpClient) {
        this.ftp = ftpClient;
    }

    @Override
    public DownloadStatus download(String fileName, String localDir) throws IOException {
        Path remoteFilePath = Paths.get(ftp.printWorkingDirectory()).resolve(fileName);
        Path localFilePath = Paths.get(localDir).resolve(fileName);
        return FtpHelper.getInstance().download(ftp, remoteFilePath.toString(), localFilePath.toString());
    }

    @Override
    public UploadStatus upload(String fileName, String localDir) throws IOException {
        Path localFilePath = Paths.get(localDir).resolve(fileName);
        Path remoteFilePath = Paths.get(ftp.printWorkingDirectory()).resolve(fileName);
        return FtpHelper.getInstance().upload(ftp, localFilePath.toString(), remoteFilePath.toString());
    }

    @Override
    public void move(String remoteDir) throws IOException {
        FtpHelper.getInstance().makeRemoteDir(ftp, remoteDir);
    }

    @Override
    public List<String> list() throws IOException {
        return FtpHelper.getInstance().GetFileNames(ftp, ftp.printWorkingDirectory());
    }

//    @Override
//    public void connect() throws IOException {
//        try {
//            //連接ftp服務器
//            if (StringUtils.isNotEmpty(port) && NumberUtils.isDigits(port)) {
//                ftp.connect(host, Integer.valueOf(port));
//            } else {
//                ftp.connect(host);
//            }
//        } catch (IOException e) {
//            logger.error("連接FTP服務器失敗：" + ftp.getReplyString() + ftp.getReplyCode());
//            throw e;
//        }
//    }
//
//    @Override
//    public void disconnect() throws IOException {
//        if(!isConnected()) return;
//        if (isConnected()) {
//            ftp.logout();
//        }
//        ftp.disconnect();
//    }
//
//    @Override
//    public boolean isConnected() {
//        if (Objects.isNull(ftp)) return false;
//        return ftp.isConnected();
//    }

}
