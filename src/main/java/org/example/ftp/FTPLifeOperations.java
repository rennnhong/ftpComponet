package org.example.ftp;

import java.io.IOException;

public interface FTPLifeOperations {
    void connect() throws IOException;

    void disconnect() throws IOException;

    boolean isConnected();

}
