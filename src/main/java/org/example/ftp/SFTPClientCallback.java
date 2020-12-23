package org.example.ftp;

import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public interface SFTPClientCallback<T> {

    T doTransfer(ChannelSftp sftp)throws IOException;

}