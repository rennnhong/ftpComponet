package org.example.ftp;

import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;

import java.io.IOException;
import java.util.List;

public interface FtpOperations {
    DownloadStatus download(String fileName, String localDir) throws IOException;

    UploadStatus upload(String fileName, String localDir) throws IOException;

    void move(String remoteDir) throws IOException;

    List<String> list() throws IOException;
}
