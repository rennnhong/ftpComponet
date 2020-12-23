package org.example.ftp;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FTPTest {

    private String host = "localhost";
    private String port = "21";
    private String username = "rayluo";
    private String password = "1938";
    private String localDir = "D:\\ftptest-local";
    private String remoteDir = "/";

    @Test
    public void testUpload() throws IOException {
        FTPClientTemplate ftpClient = new FTPClientTemplate(host, username, password, port);

        ftpClient.upload("D:\\ftptest-local\\上傳測試.txt", "/上傳測試.txt");

    }


    @Test
    public void testDownload() throws IOException {
        FTPClientTemplate ftpClient = new FTPClientTemplate(host, username, password, port);
        ftpClient.downloadFile("/下載測試.txt", "D:\\ftptest-local\\下載測試.txt");
    }

    @Test
    public void testMultiDownload() throws IOException {
        FTPClientTemplate ftpClient = new FTPClientTemplate(host, username, password, port);
        ftpClient.downloadList(localDir+"\\"+"多檔測試",remoteDir+"/"+"多檔測試",localDir+"/"+"tempFile.txt");
//        ftpClient.downloadFile("/下載測試.txt", "D:\\ftptest-local\\下載測試.txt");
    }

}
