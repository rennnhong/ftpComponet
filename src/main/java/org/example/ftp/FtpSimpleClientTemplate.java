package org.example.ftp;

import java.util.Collection;

public interface FtpSimpleClientTemplate extends FtpBaseClientTemplate {
    /**
     * 從遠程伺服器目錄下載多個文件到本地服務器目錄中
     *
     * @param localDir     FTP服務器本地目錄
     * @param remoteDir    FTP遠程服務器目錄
     * @param localTmpFile 臨時下載記錄文件
     * @return 成功下載記錄 // todo 封裝回傳物件，包含下載成功跟失敗的檔案紀錄
     */
    Collection<String> downloadList(final String localDir, final String remoteDir, final String localTmpFile) throws Exception;

    /**
     * 從遠程伺服器目錄下載多個文件到本地服務器目錄中
     *
     * @param remoteDir    FTP遠程服務器目錄
     * @param localDir     FTP服務器本地目錄
     * @param localTmpFile 臨時下載記錄文件
     * @return 成功上傳記錄 // todo 封裝回傳物件，包含上傳成功跟失敗的檔案紀錄
     */
    Collection<String> uploadList(final String remoteDir, final String localDir, final String localTmpFile) throws Exception;
}
