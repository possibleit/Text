package com.example.myapplication.Bean;

public class File_Upload_Response {

    /**
     * upload_method : 111
     * file : avatar/check.png
     * upload_time : 2019-08-10 08:43:20.070482+00:00
     */

    private String upload_method;
    private String file;
    private String upload_time;

    public String getUpload_method() {
        return upload_method;
    }

    public void setUpload_method(String upload_method) {
        this.upload_method = upload_method;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(String upload_time) {
        this.upload_time = upload_time;
    }
}
