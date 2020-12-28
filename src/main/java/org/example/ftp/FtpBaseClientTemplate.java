package org.example.ftp;

import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FtpBaseClientTemplate {
    /**
     * 文件上傳的方法
     *
     * @param localFilePath 本地檔案路徑
     * @param remoteDirPath 遠程資料夾路徑
     * @return
     * @throws IOException
     */
    UploadStatus upload(final String localFilePath, final String remoteDirPath) throws IOException;

    /**
     * 文件上傳的方法
     *
     * @param localFile 本地檔案
     * @param remoteDirPath 遠程資料夾路徑
     * @return
     * @throws IOException
     */
    UploadStatus upload(final File localFile, final String remoteDirPath) throws IOException;


    /**
     * 從FTP服務器上下載文件至指定資料夾,支持斷點續傳，上傳百分比匯報
     *
     * @param remoteFile 遠程文件路徑
     * @param localDirPath   本地文件資料夾路徑
     * @return 上傳的狀態
     * @throws IOException
     */
    DownloadStatus download(final String remoteFile, final String localDirPath) throws IOException;
    /**
     * 從FTP服務器上下載文件至指定資料夾,支持斷點續傳，上傳百分比匯報
     *
     * @param remoteFilePath 遠程文件路徑
     * @param localDir   本地文件資料夾
     * @return 上傳的狀態
     * @throws IOException
     */
    DownloadStatus download(final String remoteFilePath, final File localDir) throws IOException;

    /**
     * 查看服務器上文件列表方法
     *
     * @param remoteDir
     * @return
     * @throws IOException
     */
    List<String> list(final String remoteDir) throws IOException;
}
