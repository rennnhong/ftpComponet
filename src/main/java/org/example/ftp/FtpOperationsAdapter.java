package org.example.ftp;

import org.apache.commons.net.ftp.*;
import org.example.ftp.helper.FtpHelper;
import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * 適配自定義介面與apache FtpClient的操作
 */
public class FtpOperationsAdapter implements FtpOperations {
    private final FTPClient ftp;

    public FtpOperationsAdapter(FTPClient ftpClient) {
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
}
