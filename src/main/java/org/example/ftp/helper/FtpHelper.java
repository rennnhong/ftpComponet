package org.example.ftp.helper;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * apache FtpClient 的封裝工具類
 */
public class FtpHelper {
    private static String DEAFULT_REMOTE_CHARSET = "UTF-8";
    private static String DEAFULT_LOCAL_CHARSET = "UTF-8";


    private static FtpHelper instance = new FtpHelper();

    public static FtpHelper getInstance() {
        return instance;
    }

    /**
     * 連接到FTP服務器
     *
     * @param hostname主機名
     * @param port        端口
     * @param username    用戶名
     * @param password    密碼
     * @return 是否連接成功
     * @throws IOException
     */
    public boolean connect(FTPClient ftpClient, String hostname, int port, String username, String password) throws IOException {
        ftpClient.connect(hostname, port);
        ftpClient.setControlEncoding(DEAFULT_REMOTE_CHARSET);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(username, password)) {
                return true;
            }
        }
        disconnect(ftpClient);
        return false;
    }

    /**
     * 從FTP服務器上下載文件,支持斷點續傳，上傳百分比匯報
     *
     * @param remoteFilePath 遠程文件路徑
     * @param localFilePath  本地文件路徑
     * @return 上傳的狀態
     * @throws IOException
     */
    public DownloadStatus download(FTPClient ftpClient, String remoteFilePath, String localFilePath)
            throws IOException {
        // 設置被動模式
//        ftpClient.enterLocalPassiveMode();
        // 設置以二進制方式傳輸
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        DownloadStatus result;
        // 檢查遠程文件是否存在
        FTPFile[] files = ftpClient.listFiles(new String(
                remoteFilePath.getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
        if (files.length != 1) {
            System.out.println("遠程文件不存在");
            return DownloadStatus.Remote_File_Noexist;
        }
        long lRemoteSize = files[0].getSize();
        File f = new File(localFilePath);
        // 本地存在文件，進行斷點下載
        if (f.exists()) {
            long localSize = f.length();
            // 判斷本地文件大小是否大於遠程文件大小
            if (localSize >= lRemoteSize) {
                System.out.println("本地文件大於遠程文件，下載中止");
                return DownloadStatus.Local_Bigger_Remote;
            }
            // 進行斷點續傳，並記錄狀態
            FileOutputStream out = new FileOutputStream(f, true);
            ftpClient.setRestartOffset(localSize);
            InputStream in = ftpClient.retrieveFileStream(new String(remoteFilePath
                    .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
            byte[] bytes = new byte[1024];
            long step = lRemoteSize / 100;
            long process = localSize / step;
            int c;
            while ((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
                localSize += c;
                long nowProcess = localSize / step;
                if (nowProcess > process) {
                    process = nowProcess;
                    if (process % 10 == 0) {
                        System.out.println("下載進度：" + process);
                    }
                    // TODO 更新文件下載進度,值存放在process變量中
                }
            }
            in.close();
            out.close();
            boolean isDo = ftpClient.completePendingCommand();
            if (isDo) {
                result = DownloadStatus.Download_From_Break_Success;
            } else {
                result = DownloadStatus.Download_From_Break_Failed;
            }
        } else {
            OutputStream out = new FileOutputStream(f);
            InputStream in = ftpClient.retrieveFileStream(new String(remoteFilePath
                    .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
            byte[] bytes = new byte[1024];
            long step = lRemoteSize / 100;
            long process = 0;
            long localSize = 0L;
            int c;
            while ((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
                localSize += c;
                long nowProcess = localSize / step;
                if (nowProcess > process) {
                    process = nowProcess;
                    if (process % 10 == 0) {
                        System.out.println("下載進度：" + process);
                    }
                    // TODO 更新文件下載進度,值存放在process變量中
                }
            }
            in.close();
            out.close();
            boolean upNewStatus = ftpClient.completePendingCommand();
            if (upNewStatus) {
                result = DownloadStatus.Download_New_Success;
            } else {
                result = DownloadStatus.Download_New_Failed;
            }
        }
        return result;
    }

    /**
     * 上傳文件到FTP服務器，支持斷點續傳
     *
     * @param localFilePath  本地文件名稱，絕對路徑
     * @param remoteFilePath 遠程文件路徑，使用/home/directory1/subdirectory/file.ext
     *                       按照Linux上的路徑指定方式，支持多級目錄嵌套，支持遞歸創建不存在的目錄結構
     * @return 上傳結果
     * @throws IOException
     */
    public UploadStatus upload(FTPClient ftpClient, String localFilePath, String remoteFilePath) throws IOException {
        // 設置PassiveMode傳輸
        ftpClient.enterLocalPassiveMode();
        // 設置以二進制流的方式傳輸
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setControlEncoding(DEAFULT_REMOTE_CHARSET);
        UploadStatus result;
        // 對遠程目錄的處理
        String remoteFileName = remoteFilePath;
        if (remoteFilePath.contains("/")) {
            remoteFileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
            // 創建服務器遠程目錄結構，創建失敗直接返回
            if (createDirecroty(remoteFilePath, ftpClient) == UploadStatus.Create_Directory_Fail) {
                return UploadStatus.Create_Directory_Fail;
            }
        }
        // ftpClient.feat();
        // System.out.println( ftpClient.getReply());
        // System.out.println( ftpClient.acct(null));
        // System.out.println(ftpClient.getReplyCode());
        // System.out.println(ftpClient.getReplyString());
        // 檢查遠程是否存在文件
        FTPFile[] files = ftpClient.listFiles(new String(remoteFileName
                .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
        if (files.length == 1) {
            long remoteSize = files[0].getSize();
            File f = new File(localFilePath);
            long localSize = f.length();
            if (remoteSize == localSize) { // 文件存在
                return UploadStatus.File_Exits;
            } else if (remoteSize > localSize) {
                return UploadStatus.Remote_Bigger_Local;
            }
            // 嘗試移動文件內讀取指針,實現斷點續傳
            result = uploadFile(remoteFileName, f, ftpClient, remoteSize);
            // 如果斷點續傳沒有成功，則刪除服務器上文件，重新上傳
            if (result == UploadStatus.Upload_From_Break_Failed) {
                if (!ftpClient.deleteFile(remoteFileName)) {
                    return UploadStatus.Delete_Remote_Faild;
                }
                result = uploadFile(remoteFileName, f, ftpClient, 0);
            }
        } else {
            result = uploadFile(remoteFileName, new File(localFilePath), ftpClient, 0);
        }
        return result;
    }

    /**
     * 斷開與遠程服務器的連接
     *
     * @throws IOException
     */
    public void disconnect(FTPClient ftpClient) throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    /**
     * 遞歸創建遠程服務器目錄
     *
     * @param remote    遠程服務器文件絕對路徑
     * @param ftpClient FTPClient對象
     * @return 目錄創建是否成功
     * @throws IOException
     */
    public UploadStatus createDirecroty(String remote, FTPClient ftpClient)
            throws IOException {
        UploadStatus status = UploadStatus.Create_Directory_Success;
        String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
        if (!directory.equalsIgnoreCase("/")
                && !ftpClient.changeWorkingDirectory(new String(directory
                .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET))) {
            // 如果遠程目錄不存在，則遞歸創建遠程服務器目錄
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            while (true) {
                String subDirectory = new String(remote.substring(start, end)
                        .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET);
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                        ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        System.out.println("創建目錄失敗");
                        return UploadStatus.Create_Directory_Fail;
                    }
                }
                start = end + 1;
                end = directory.indexOf("/", start);
                // 檢查所有目錄是否創建完畢
                if (end <= start) {
                    break;
                }
            }
        }
        return status;
    }

    /**
     * 上傳文件到服務器,新上傳和斷點續傳
     *
     * @param remoteFile  遠程文件名，在上傳之前已經將服務器工作目錄做了改變
     * @param localFile   本地文件File句柄，絕對路徑
     * @param processStep 需要顯示的處理進度步進值
     * @param ftpClient   FTPClient引用
     * @return
     * @throws IOException
     */
    public UploadStatus uploadFile(String remoteFile, File localFile,
                                   FTPClient ftpClient, long remoteSize) throws IOException {
        UploadStatus status;
        // 顯示進度的上傳
        long step = localFile.length() / 100;
        long process = 0;
        long localreadbytes = 0L;
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
        String remote = new String(remoteFile.getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET);
        OutputStream out = ftpClient.appendFileStream(remote);
        if (out == null) {
            String message = ftpClient.getReplyString();
            throw new RuntimeException(message);
        }
        // 斷點續傳
        if (remoteSize > 0) {
            ftpClient.setRestartOffset(remoteSize);
            process = remoteSize / step;
            raf.seek(remoteSize);
            localreadbytes = remoteSize;
        }
        byte[] bytes = new byte[1024];
        int c;
        while ((c = raf.read(bytes)) != -1) {
            out.write(bytes, 0, c);
            localreadbytes += c;
            if (localreadbytes / step != process) {
                process = localreadbytes / step;
                System.out.println("上傳進度:" + process);
                // TODO 匯報上傳狀態
            }
        }
        out.flush();
        raf.close();
        out.close();
        boolean result = ftpClient.completePendingCommand();
        if (remoteSize > 0) {
            status = result ? UploadStatus.Upload_From_Break_Success
                    : UploadStatus.Upload_From_Break_Failed;
        } else {
            status = result ? UploadStatus.Upload_New_File_Success
                    : UploadStatus.Upload_New_File_Failed;
        }
        return status;
    }


    public void makeRemoteDir(FTPClient ftp, String dir)
            throws IOException {
        if (dir.indexOf("/") == 0) {
            ftp.changeWorkingDirectory("/");
        }
        String subdir = new String();
        StringTokenizer st = new StringTokenizer(dir, "/");
        while (st.hasMoreTokens()) {
            subdir = st.nextToken();
            if (!(ftp.changeWorkingDirectory(subdir))) {
                if (!(ftp.makeDirectory(subdir))) {
                    int rc = ftp.getReplyCode();
                    if (((rc != 550) && (rc != 553) && (rc != 521))) {
                        throw new IOException("could not create directory: " + ftp.getReplyString());
                    }
                } else {
                    ftp.changeWorkingDirectory(subdir);
                }
            }
        }
    }


    /**
     * 獲取指定目錄下的文件名稱列表
     *
     * @param currentDir 需要獲取其子目錄的當前目錄名稱
     * @return 返回子目錄字符串數組
     */
    public List<String> GetFileNames(FTPClient ftpClient, String currentDir) {
        List<String> dirs = null;
        try {
            if (currentDir == null)
                dirs = Arrays.stream(ftpClient.listFiles()).map(file -> file.getName()).collect(Collectors.toList());
            else
                dirs = Arrays.stream(ftpClient.listFiles(currentDir)).map(file -> file.getName()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirs;
    }

    /**
     * 獲取指定目錄下的文件與目錄信息集合
     *
     * @param currentDir 指定的當前目錄
     * @return 返回的文件集合
     */
    public FTPFile[] GetDirAndFilesInfo(FTPClient ftpClient, String currentDir) {
        FTPFile[] files = null;
        try {
            if (currentDir == null)
                files = ftpClient.listFiles();
            else
                files = ftpClient.listFiles(currentDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return files;
    }
}