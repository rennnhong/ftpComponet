package org.example.ftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public interface FTPClientCallback<T> {

    T doTransfer(FTPClient ftp)throws IOException;

}