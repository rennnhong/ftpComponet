package org.example.ftp.response;

public enum DownloadStatus {

    Remote_File_Noexist(0,"遠程文件不存在"), // 遠程文件不存在
    //用於單個下載
    Download_New_Success(1,"下載文件成功"), // 下載文件成功
    Download_New_Failed(2,"下載文件失敗"), // 下載文件失敗
    Local_Bigger_Remote(3,"本地文件大於遠程文件"), // 本地文件大於遠程文件
    Download_From_Break_Success(4,"文件斷點續傳成功"), // 斷點續傳成功
    Download_From_Break_Failed(5,"文件斷點續傳失敗"), // 斷點續傳失敗
    //用於批量下載
    Download_Batch_Success(6,"文件批量下載成功"),
    Download_Batch_Failure(7,"文件批量下載失敗"),
    Download_Batch_Failure_SUCCESS(8,"文件批量下載不完全成功");

    private int code;

    private String description;


    private DownloadStatus(int code , String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    /**
//     * 下載狀態中中使用的code
//     * @param code
//     * @return
//     */
//    public static DownloadStatus fromCode(int code) {
//        return EnumUtils.fromEnumProperty(DownloadStatus.class, "code", code);
//    }
}