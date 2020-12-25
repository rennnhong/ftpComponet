package org.example.ftp.response;

public enum UploadStatus {
    Create_Directory_Fail(0,"遠程服務器相應目錄創建失敗"), // 遠程服務器相應目錄創建失敗
    Create_Directory_Success(1,"遠程服務器闖將目錄成功"), // 遠程服務器闖將目錄成功
    Upload_New_File_Success(2,"上傳新文件成功"), // 上傳新文件成功
    Upload_New_File_Failed(3,"上傳新文件失敗"), // 上傳新文件失敗
    File_Exits(4,"文件已經存在"), // 文件已經存在
    Remote_Bigger_Local(5,"遠程文件大於本地文件"), // 遠程文件大於本地文件
    Upload_From_Break_Success(6," 斷點續傳成功"), // 斷點續傳成功
    Upload_From_Break_Failed(7,"斷點續傳失敗"), // 斷點續傳失敗
    Delete_Remote_Faild(8,"刪除遠程文件失敗"); // 刪除遠程文件失敗

    private int code;

    private String description;


    private UploadStatus(int code , String description) {
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
//    public static UploadStatus fromCode(int code) {
//        Map enumMap = EnumUtils.getEnumMap(UploadStatus.class);
//        enumMap.get()
//
//        EnumUtils.getEnum(UploadStatus.class,"code");
//        return EnumUtils.fromEnumProperty(UploadStatus.class, "code", code);
//    }
}