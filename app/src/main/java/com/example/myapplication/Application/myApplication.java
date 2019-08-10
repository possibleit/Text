package com.example.myapplication.Application;

import com.youth.xframe.XFrame;

import org.litepal.LitePalApplication;

//import com.imnjh.imagepicker.PickerConfig;
//import com.imnjh.imagepicker.SImagePicker;

public class myApplication extends LitePalApplication {
    @Override
    public void onCreate()
    {
        super.onCreate();
        XFrame.initXLog()//初始化XLog
                .setTag("Test")//设置全局tag
                .setShowThreadInfo(false)//是否开启线程信息显示，默认true
                .setDebug(false);//是否显示日志，默认true，发布时最好关闭

    }
}
