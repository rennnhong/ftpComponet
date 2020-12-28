package org.example.ftp;

import java.io.IOException;

public interface FtpClientCallback<T> {

    T doTransfer(FtpOperations ftp)throws IOException;

}