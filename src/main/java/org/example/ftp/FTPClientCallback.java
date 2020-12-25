package org.example.ftp;

import java.io.IOException;

public interface FTPClientCallback<T> {

    T doTransfer(FTPClientOperations ftp)throws IOException;

}