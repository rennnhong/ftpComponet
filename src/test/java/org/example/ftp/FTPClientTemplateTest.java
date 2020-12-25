package org.example.ftp;


import com.google.common.base.MoreObjects;
import org.example.ftp.response.DownloadStatus;
import org.example.ftp.response.UploadStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

public class FTPClientTemplateTest {

    private String ftpLocalDir = "D://ftp_test/local";
    private String remoteRoot = "/";

    @Before
    public void buildTestEnv() throws IOException {
        Path ftpLocalDirPath = Paths.get(ftpLocalDir);

//        if (Files.deleteIfExists(ftpLocalDirPath))
//            System.out.println("刪除已存在的本地資料夾");

        ftpLocalDirPath = Files.createDirectories(ftpLocalDirPath);

        for (int i = 0; i < 10; i++) {
            String fileName = "upload" + i;
            BufferedWriter bufferedWriter = com.google.common.io.Files.newWriter(ftpLocalDirPath.resolve(fileName + ".txt").toFile(), Charset.forName("utf-8"));
            bufferedWriter.write(MessageFormat.format("我是{0}", fileName));
            bufferedWriter.flush();
        }


    }

    private String host = "localhost";
    private String username = "rayluo";
    private String pwd = "1938";
    private String port = "21";

    @Test
    public void testUploadSingleFile() throws IOException {
        FTPClientTemplate ftp = new FTPClientTemplate(host, username, pwd, port);
        UploadStatus upload = ftp.upload(
                Paths.get(ftpLocalDir).resolve("upload1.txt").toString(),
                "/upload");

        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(upload)
                .addValue(upload.getCode())
                .addValue(upload.getDescription());

        System.out.println(toStringHelper.toString());
    }

    @Test
    public void testDownloadSingleFile() throws IOException {
        FTPClientTemplate ftp = new FTPClientTemplate(host, username, pwd, port);
        DownloadStatus download = ftp.download(
                Paths.get("download").resolve("download1.txt").toString(),
                Paths.get(ftpLocalDir).toString());

        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(download)
                .addValue(download.getCode())
                .addValue(download.getDescription());

        System.out.println(toStringHelper.toString());
    }

    @Test
    public void testListFiles() throws IOException {
        FTPClientTemplate ftp = new FTPClientTemplate(host, username, pwd, port);
        List<String> list = ftp.list(Paths.get("/list").toString());
        list.forEach(s -> System.out.println(s));
    }

    @Test
    public void testUploadMultiFiles() {

    }

    @Test
    public void testDownloadMultiFiles() {

    }

    @Test
    public void testCustomAction() throws IOException {
        FTPClientTemplate ftp = new FTPClientTemplate(host, username, pwd, port);
        ftp.execute(new FTPClientCallback<String>() {
            @Override
            public String doTransfer(FTPClientOperations ftp) throws IOException {

                System.out.println("task1");
                ftp.move("action1");
                ftp.list().forEach(s -> System.out.println(s));
                System.out.println("--------------------------------");


                System.out.println("task2");
                ftp.move("action2");
                ftp.list().forEach(s -> System.out.println(s));
                System.out.println("--------------------------------");


                System.out.println("task3");
                ftp.move("/download");
                ftp.download("download1.txt",ftpLocalDir);
                System.out.println("--------------------------------");

                System.out.println("task4");
                ftp.move("/upload");
                ftp.upload("upload1.txt",ftpLocalDir);
                System.out.println("--------------------------------");


                return null;
            }
        });
    }

}
