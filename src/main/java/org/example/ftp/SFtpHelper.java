package org.example.ftp;//package org.example.ftp;
//
////import org.apache.commons.net.ftp.FTP;
////import org.apache.commons.net.ftp.FTPClient;
////import org.apache.commons.net.ftp.FTPFile;
////import org.apache.commons.net.ftp.FTPReply;
//
//import com.jcraft.jsch.ChannelSftp;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPReply;
//
//import java.io.*;
//import java.util.StringTokenizer;
//
//public class SFtpHelper implements FTPCompleteOperations {
//    private static String DEAFULT_REMOTE_CHARSET="UTF-8";
//    private static String DEAFULT_LOCAL_CHARSET="UTF-8";
//
//
//
//    private static SFtpHelper instance=new SFtpHelper();
//
//    public static FTPCompleteOperations getInstance(){
//        return instance;
//    }
//
//    public  boolean connect(FTPClient ftpClient, String hostname, int port, String username, String password) throws IOException {
//        ftpClient.connect(hostname, port);
//        ftpClient.setControlEncoding(DEAFULT_REMOTE_CHARSET);
//        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
//            if (ftpClient.login(username, password)) {
//                return true;
//            }
//        }
//        disconnect(ftpClient);
//        return false;
//    }
//
//    public DownloadStatus download(ChannelSftp sftp, String remoteFilePath, String localFilePath)
//            throws IOException {
//        DownloadStatus result;
//        // 检查远程文件是否存在
//        sftp.ls(remoteFilePath);
//        FTPFile[] files = ftpClient.listFiles(new String(
//                remoteFilePath.getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
//        if (files.length != 1) {
//            System.out.println("远程文件不存在");
//            return DownloadStatus.Remote_File_Noexist;
//        }
//        long lRemoteSize = files[0].getSize();
//        File f = new File(localFilePath);
//        // 本地存在文件，进行断点下载
//        if (f.exists()) {
//            long localSize = f.length();
//            // 判断本地文件大小是否大于远程文件大小
//            if (localSize >= lRemoteSize) {
//                System.out.println("本地文件大于远程文件，下载中止");
//                return DownloadStatus.Local_Bigger_Remote;
//            }
//            // 进行断点续传，并记录状态
//            FileOutputStream out = new FileOutputStream(f, true);
//            ftpClient.setRestartOffset(localSize);
//            InputStream in = ftpClient.retrieveFileStream(new String(remoteFilePath
//                    .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
//            byte[] bytes = new byte[1024];
//            long step = lRemoteSize / 100;
//            long process = localSize / step;
//            int c;
//            while ((c = in.read(bytes)) != -1) {
//                out.write(bytes, 0, c);
//                localSize += c;
//                long nowProcess = localSize / step;
//                if (nowProcess > process) {
//                    process = nowProcess;
//                    if (process % 10 == 0){
//                        System.out.println("下载进度：" + process);
//                    }
//                    // TODO 更新文件下载进度,值存放在process变量中
//                }
//            }
//            in.close();
//            out.close();
//            boolean isDo = ftpClient.completePendingCommand();
//            if (isDo) {
//                result = DownloadStatus.Download_From_Break_Success;
//            } else {
//                result = DownloadStatus.Download_From_Break_Failed;
//            }
//        } else {
//            OutputStream out = new FileOutputStream(f);
//            InputStream in = ftpClient.retrieveFileStream(new String(remoteFilePath
//                    .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
//            byte[] bytes = new byte[1024];
//            long step = lRemoteSize / 100;
//            long process = 0;
//            long localSize = 0L;
//            int c;
//            while ((c = in.read(bytes)) != -1) {
//                out.write(bytes, 0, c);
//                localSize += c;
//                long nowProcess = localSize / step;
//                if (nowProcess > process) {
//                    process = nowProcess;
//                    if (process % 10 == 0){
//                        System.out.println("下载进度：" + process);
//                    }
//                    // TODO 更新文件下载进度,值存放在process变量中
//                }
//            }
//            in.close();
//            out.close();
//            boolean upNewStatus = ftpClient.completePendingCommand();
//            if (upNewStatus) {
//                result = DownloadStatus.Download_New_Success;
//            } else {
//                result = DownloadStatus.Download_New_Failed;
//            }
//        }
//        return result;
//    }
//
//    public UploadStatus upload(FTPClient ftpClient,String localFilePath, String remoteFilePath) throws IOException {
//        // 设置PassiveMode传输
//        ftpClient.enterLocalPassiveMode();
//        // 设置以二进制流的方式传输
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//        ftpClient.setControlEncoding(DEAFULT_REMOTE_CHARSET);
//        UploadStatus result;
//        // 对远程目录的处理
//        String remoteFileName = remoteFilePath;
//        if (remoteFilePath.contains("/")) {
//            remoteFileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
//            // 创建服务器远程目录结构，创建失败直接返回
//            if (createDirecroty(remoteFilePath, ftpClient) == UploadStatus.Create_Directory_Fail) {
//                return UploadStatus.Create_Directory_Fail;
//            }
//        }
//        // ftpClient.feat();
//        // System.out.println( ftpClient.getReply());
//        // System.out.println( ftpClient.acct(null));
//        // System.out.println(ftpClient.getReplyCode());
//        // System.out.println(ftpClient.getReplyString());
//        // 检查远程是否存在文件
//        FTPFile[] files = ftpClient.listFiles(new String(remoteFileName
//                .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET));
//        if (files.length == 1) {
//            long remoteSize = files[0].getSize();
//            File f = new File(localFilePath);
//            long localSize = f.length();
//            if (remoteSize == localSize) { // 文件存在
//                return UploadStatus.File_Exits;
//            } else if (remoteSize > localSize) {
//                return UploadStatus.Remote_Bigger_Local;
//            }
//            // 尝试移动文件内读取指针,实现断点续传
//            result = uploadFile(remoteFileName, f, ftpClient, remoteSize);
//            // 如果断点续传没有成功，则删除服务器上文件，重新上传
//            if (result == UploadStatus.Upload_From_Break_Failed) {
//                if (!ftpClient.deleteFile(remoteFileName)) {
//                    return UploadStatus.Delete_Remote_Faild;
//                }
//                result = uploadFile(remoteFileName, f, ftpClient, 0);
//            }
//        } else {
//            result = uploadFile(remoteFileName, new File(localFilePath), ftpClient, 0);
//        }
//        return result;
//    }
//    /**
//     *
//     * 断开与远程服务器的连接
//     * @throws IOException
//     */
//    public void disconnect(FTPClient ftpClient) throws IOException {
//        if (ftpClient.isConnected()) {
//            ftpClient.disconnect();
//        }
//    }
//    /**
//     * 递归创建远程服务器目录
//     * @param remote 远程服务器文件绝对路径
//     * @param ftpClient  FTPClient对象
//     * @return 目录创建是否成功
//     * @throws IOException
//     */
//    public UploadStatus createDirecroty(String remote, FTPClient ftpClient)
//            throws IOException {
//        UploadStatus status = UploadStatus.Create_Directory_Success;
//        String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
//        if (!directory.equalsIgnoreCase("/")
//                && !ftpClient.changeWorkingDirectory(new String(directory
//                .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET))) {
//            // 如果远程目录不存在，则递归创建远程服务器目录
//            int start = 0;
//            int end = 0;
//            if (directory.startsWith("/")) {
//                start = 1;
//            } else {
//                start = 0;
//            }
//            end = directory.indexOf("/", start);
//            while (true) {
//                String subDirectory = new String(remote.substring(start, end)
//                        .getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET);
//                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
//                    if (ftpClient.makeDirectory(subDirectory)) {
//                        ftpClient.changeWorkingDirectory(subDirectory);
//                    } else {
//                        System.out.println("创建目录失败");
//                        return UploadStatus.Create_Directory_Fail;
//                    }
//                }
//                start = end + 1;
//                end = directory.indexOf("/", start);
//                // 检查所有目录是否创建完毕
//                if (end <= start) {
//                    break;
//                }
//            }
//        }
//        return status;
//    }
//
//    /**
//     * 上传文件到服务器,新上传和断点续传
//     * @param remoteFile  远程文件名，在上传之前已经将服务器工作目录做了改变
//     * @param localFile   本地文件File句柄，绝对路径
//     * @param processStep  需要显示的处理进度步进值
//     * @param ftpClient  FTPClient引用
//     * @return
//     * @throws IOException
//     */
//    public UploadStatus uploadFile(String remoteFile, File localFile,
//                                   FTPClient ftpClient, long remoteSize) throws IOException {
//        UploadStatus status;
//        // 显示进度的上传
//        long step = localFile.length() / 100;
//        long process = 0;
//        long localreadbytes = 0L;
//        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
//        String remote = new String(remoteFile.getBytes(DEAFULT_REMOTE_CHARSET), DEAFULT_LOCAL_CHARSET);
//        OutputStream out = ftpClient.appendFileStream(remote);
//        if (out == null)
//        {
//            String message = ftpClient.getReplyString();
//            throw new RuntimeException(message);
//        }
//        // 断点续传
//        if (remoteSize > 0) {
//            ftpClient.setRestartOffset(remoteSize);
//            process = remoteSize / step;
//            raf.seek(remoteSize);
//            localreadbytes = remoteSize;
//        }
//        byte[] bytes = new byte[1024];
//        int c;
//        while ((c = raf.read(bytes)) != -1) {
//            out.write(bytes, 0, c);
//            localreadbytes += c;
//            if (localreadbytes / step != process) {
//                process = localreadbytes / step;
//                System.out.println("上传进度:" + process);
//                // TODO 汇报上传状态
//            }
//        }
//        out.flush();
//        raf.close();
//        out.close();
//        boolean result = ftpClient.completePendingCommand();
//        if (remoteSize > 0) {
//            status = result ? UploadStatus.Upload_From_Break_Success
//                    : UploadStatus.Upload_From_Break_Failed;
//        } else {
//            status = result ? UploadStatus.Upload_New_File_Success
//                    : UploadStatus.Upload_New_File_Failed;
//        }
//        return status;
//    }
//
//
//    protected void makeRemoteDir(FTPClient ftp, String dir)
//            throws IOException
//    {
//        String workingDirectory = ftp.printWorkingDirectory();
//        if (dir.indexOf("/") == 0) {
//            ftp.changeWorkingDirectory("/");
//        }
//        String subdir = new String();
//        StringTokenizer st = new StringTokenizer(dir, "/");
//        while (st.hasMoreTokens()) {
//            subdir = st.nextToken();
//            if (!(ftp.changeWorkingDirectory(subdir))) {
//                if (!(ftp.makeDirectory(subdir)))
//                {
//                    int rc = ftp.getReplyCode();
//                    if (((rc != 550) && (rc != 553) && (rc != 521)))
//                    {
//                        throw new IOException("could not create directory: " + ftp.getReplyString());
//                    }
//                }
//                else {
//                    ftp.changeWorkingDirectory(subdir);
//                }
//            }
//        }
//        if (workingDirectory != null){
//            ftp.changeWorkingDirectory(workingDirectory);
//        }
//    }
//
//
//
//
//
//
//
//
//
//    /**
//     * 获取指定目录下的文件名称列表
//     * @param currentDir
//     *            需要获取其子目录的当前目录名称
//     * @return 返回子目录字符串数组
//     */
//    public String[] GetFileNames(FTPClient ftpClient,String currentDir) {
//        String[] dirs = null;
//        try {
//            if (currentDir == null)
//                dirs = ftpClient.listNames();
//            else
//                dirs = ftpClient.listNames(currentDir);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return dirs;
//    }
//
//    /**
//     * 获取指定目录下的文件与目录信息集合
//     * @param currentDir
//     *            指定的当前目录
//     * @return 返回的文件集合
//     */
//    public FTPFile[] GetDirAndFilesInfo(FTPClient ftpClient,String currentDir)
//    {
//        FTPFile[] files = null;
//        try
//        {
//            if (currentDir == null)
//                files = ftpClient.listFiles();
//            else
//                files = ftpClient.listFiles(currentDir);
//        }
//        catch (IOException ex)
//        {
//            ex.printStackTrace();
//        }
//        return files;
//    }
//
//    @Override
//    public void moveDir(String remoteDir) {
//
//    }
//
//    @Override
//    public void createDir(String remoteDir) {
//
//    }
//}