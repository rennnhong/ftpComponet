package org.example.ftp;

import java.io.IOException;

public interface FtpExecutable {
    /**
     * 執行FTP回調操作的方法
     *
     * @param callback 回調的函數
     * @throws IOException
     */
    public <T> T execute(FtpClientCallback<T> callback) throws IOException;
}
