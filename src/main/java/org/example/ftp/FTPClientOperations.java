package org.example.ftp;


import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.Collection;

public interface FTPClientOperations {
    /**
     * 文件上传的方法
     * @param remote
     * @param local
     * @return
     * @throws IOException
     */
    public UploadStatus upload(String remote, String local) throws IOException;


    /**
     *
     * 从远程服务器目录下载文件到本地服务器目录中
     * @param localdir FTP服务器保存目录
     * @param remotedir FTP下载服务器目录
     * @param localTempFile临时下载记录文件
     * @return 成功返回true，否则返回false
     */
    public Collection<String> downloadList(final String localdir, final String remotedir, final String localTmpFile) throws IOException;

    /**
     * 文件下载的方法
     * @param remote
     * @param local
     * @return
     * @throws IOException
     */
    public DownloadStatus downloadFile(String local, String remote) throws IOException;

    /**
     * 查看服务器上文件列表方法
     * @param remotedir
     * @return
     * @throws IOException
     */
    public FTPFile[] list(final String remotedir) throws IOException;
}