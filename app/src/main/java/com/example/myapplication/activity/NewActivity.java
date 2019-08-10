package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lehttp.IeHttp.LeHttp;
import com.example.lehttp.IeHttp.LeJsonDataTransform;
import com.example.myapplication.Bean.File_Upload_Response;
import com.example.myapplication.Bean.Note;
import com.example.myapplication.GlideSimpleLoader;
import com.example.myapplication.R;
import com.example.myapplication.util.CommonUtil;
import com.example.myapplication.util.FileUtils;
import com.example.myapplication.util.ImageUtils;
import com.example.myapplication.util.MyGlideEngine;
import com.example.myapplication.util.SDCardUtil;
import com.example.myapplication.util.StringUtils;
import com.github.ielse.imagewatcher.ImageWatcherHelper;
import com.sendtion.xrichtext.RichTextEditor;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewActivity extends AppCompatActivity {
    private EditText et_new_title;
    private RichTextEditor et_new_content;
    private TextView tv_new_time;
    private TextView tv_new_group;

    private int screenWidth,screenHeight;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private ProgressDialog insertDialog;
    private ProgressDialog loadingDialog;
    private Disposable subsInsert;
    private Disposable subsLoading;
    private ImageWatcherHelper iwHelper;

    private Note note;
    private String myTitle;
    private String myContent;
    private String myNoteTime;
    private int flag,is_click = 0;
    private String ppp;
    private static final int cutTitleLength = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        iwHelper = ImageWatcherHelper.with(this, new GlideSimpleLoader());
        et_new_content = findViewById(R.id.et_new_content);
        et_new_title = (EditText) findViewById(R.id.et_new_title);//标题
        tv_new_time = (TextView) findViewById(R.id.tv_new_time);
        tv_new_group = (TextView) findViewById(R.id.tv_new_group);
        openSoftKeyInput();
        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);
        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);

        try {
            Intent intent = getIntent();
            flag = intent.getIntExtra("flag", 0);//0新建，1编辑
            if (flag == 1){//编辑
                setTitle("编辑笔记");
                Bundle bundle = intent.getBundleExtra("data");
                note = (Note) bundle.getSerializable("note");

                if (note != null) {
                    myTitle = note.getTitle();
                    myContent = note.getContent();
                    myNoteTime = note.getCreateTime();

                    loadingDialog = new ProgressDialog(this);
                    loadingDialog.setMessage("数据加载中...");
                    loadingDialog.setCanceledOnTouchOutside(false);
                    loadingDialog.show();

                    tv_new_time.setText(note.getCreateTime());
                    et_new_title.setText(note.getTitle());
                    tv_new_group.setText("默认笔记");
                    et_new_content.post(new Runnable() {
                        @Override
                        public void run() {
                            dealWithContent();
                        }
                    });
                }
            } else {
                setTitle("新建笔记");
                note = new Note();
                tv_new_group.setText("默认笔记");
                myNoteTime = CommonUtil.date2string(new Date());
                tv_new_time.setText(myNoteTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getEditData() {
        List<RichTextEditor.EditData> editList = et_new_content.buildEditData();
        StringBuffer content = new StringBuffer();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
            }
        }
        return content.toString();
    }


    protected void showEditData(String content) {
        et_new_content.clearAllLayout();
        List<String> textList = StringUtils.cutStringByImgTag(content);
        for (int i = 0; i < textList.size(); i++) {
            String text = textList.get(i);
            if (text.contains("<img")) {
                String imagePath = StringUtils.getImgSrc(text);
                et_new_content.measure(0,0);
                Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, screenWidth, screenHeight);
                if (bitmap != null){
                    et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), bitmap, imagePath);
                } else {
                    et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                }
                et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
            }
        }
    }



    private void callGallery(){

        Matisse.from(this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))//照片视频全部显示MimeType.allOf()
                .countable(true)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(3)//最大选择数量为9
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                .thumbnailScale(0.85f)//缩放比例
                .theme(R.style.Matisse_Zhihu)//主题  暗色主题 R.style.Matisse_Dracula
                .imageEngine(new MyGlideEngine())//图片加载方式，Glide4需要自定义实现
                .capture(true) //是否提供拍照功能，兼容7.0系统需要下面的配置
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .captureStrategy(new CaptureStrategy(true,"com.sendtion.matisse.fileprovider"))//存储到哪里
                .forResult(REQUEST_CODE_CHOOSE);//请求码
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == 1){
                    //处理调用系统图库
                } else if (requestCode == REQUEST_CODE_CHOOSE){
                    //异步方式插入图片
                    insertImagesSync(data);
                }
            }
        }
    }

    /**
     * 异步方式插入图片
     */
    private void insertImagesSync(final Intent data){
        insertDialog.show();

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try{
                    et_new_content.measure(0, 0);
                    List<Uri> mSelected = Matisse.obtainResult(data);
                    // 可以同时插入多张图片
                    for (Uri imageUri : mSelected) {
                        String imagePath = SDCardUtil.getFilePathFromUri(NewActivity.this,  imageUri);
                        Log.e("test", "###path=" + imagePath);
                        String[] s = imagePath.split("/");
                        ppp = s[s.length - 1];
                        Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, screenWidth, screenHeight);//压缩图片
                        //bitmap = BitmapFactory.decodeFile(imagePath);
                        imagePath = SDCardUtil.saveToSdCard(bitmap);
                        //Log.e(TAG, "###imagePath="+imagePath);
                        Log.e("test", "****" + imagePath);
                        emitter.onNext(imagePath);
                    }

                    emitter.onComplete();
                }catch (Exception e){
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        //showToast("图片插入成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        //showToast("图片插入失败:"+e.getMessage());
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsInsert = d;
                    }

                    @Override
                    public void onNext(String imagePath) {
                        et_new_content.insertImage(imagePath, et_new_content.getMeasuredWidth());
                    }
                });
    }


    private void showDataSync(final String html){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                showEditData(emitter, html);
            }
        })
                //.onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        if (et_new_content != null) {
                            //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        //showToast("解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsLoading = d;
                    }

                    @Override
                    public void onNext(String text) {
                        try {
                            if (et_new_content != null) {
                                if (text.contains("<img") && text.contains("src=")) {
                                    //imagePath可能是本地路径，也可能是网络地址
                                    String imagePath = StringUtils.getImgSrc(text);
                                    //插入空的EditText，以便在图片前后插入文字
                                    et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                                    et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), imagePath);
                                } else {
                                    et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 显示数据
     */
    protected void showEditData(ObservableEmitter<String> emitter, String html) {
        try{
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        }catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    private void dealWithContent(){
        //showEditData(note.getContent());
        et_new_content.clearAllLayout();
        showDataSync(note.getContent());

        // 图片删除事件
        et_new_content.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {

            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath)) {
                    boolean isOK = SDCardUtil.deleteFile(imagePath);
                    if (isOK) {
                        //showToast("删除成功：" + imagePath);
                    }
                }
            }
        });
        // 图片点击事件
        et_new_content.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(View view, String imagePath) {
                try {
                    myContent = getEditData();
                    if (!TextUtils.isEmpty(myContent)){
                        List<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                        if (!TextUtils.isEmpty(imagePath)) {
                            int currentPosition = imageList.indexOf(imagePath);
                            //showToast("点击图片：" + currentPosition + "：" + imagePath);

                            List<Uri> dataList = new ArrayList<>();
                            for (int i = 0; i < imageList.size(); i++) {
                                dataList.add(ImageUtils.getUriFromPath(imageList.get(i)));
                            }
                            iwHelper.show(dataList, currentPosition);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openSoftKeyInput(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm != null && !imm.isActive() && et_new_content != null){
            et_new_content.requestFocus();
            //第二个参数可设置为0
            //imm.showSoftInput(et_content, InputMethodManager.SHOW_FORCED);//强制显示
            imm.showSoftInputFromInputMethod(et_new_content.getWindowToken(),
                    InputMethodManager.SHOW_FORCED);
        }
    }

    private void closeSoftKeyInput(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm != null && imm.isActive() && getCurrentFocus() != null){
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            //imm.hideSoftInputFromInputMethod();//据说无效
            //imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0); //强制隐藏键盘
            //如果输入法在窗口上已经显示，则隐藏，反之则显示
            //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_image:
                closeSoftKeyInput();//关闭软键盘
                callGallery();
                break;
            case R.id.action_new_save:
                try {
                    saveNoteData(false);

                    //TODO 网络请求
                    if (note.getContent().contains("<img") && note.getContent().contains("src=")) {
                        //imagePath可能是本地路径，也可能是网络地址
                        String s = note.getImagePath();
                        String imagePath = FileUtils.getPath(getApplicationContext(),Uri.parse(s));
                        sendRequest(ppp);
                    }
                    is_click = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNoteData(boolean isBackground) throws Exception {
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String groupName = tv_new_group.getText().toString();
        String noteTime = tv_new_time.getText().toString();

        try {
                if (noteTitle.length() == 0 ){//如果标题为空，则截取内容为标题
                    if (noteContent.length() > cutTitleLength){
                        noteTitle = noteContent.substring(0,cutTitleLength);
                    } else if (noteContent.length() > 0){
                        noteTitle = noteContent;
                    }
                }
                note.setTitle(noteTitle);
                note.setContent(noteContent);
                note.setGroupName(groupName);
                note.setType(2);
                note.setBgColor("#FFFFFF");
                note.setIsEncrypt(0);
                note.setCreateTime(CommonUtil.date2string(new Date()));
                if (flag == 0 ) {//新建笔记
                    if (noteTitle.length() == 0 && noteContent.length() == 0) {
                        if (!isBackground){
                            Toast.makeText(NewActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        note.save();
                        Toast.makeText(getApplicationContext(),note.getContent(),Toast.LENGTH_SHORT).show();
                        flag = 1;//插入以后只能是编辑
                        if (!isBackground){
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }else if (flag == 1) {//编辑笔记
                    if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                            || !noteTime.equals(myNoteTime)) {
                        Toast.makeText(getApplicationContext(),note.getContent(),Toast.LENGTH_SHORT).show();
                    }
                    if (!isBackground){
                        finish();
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //如果APP处于后台，或者手机锁屏，则保存数据
            if (CommonUtil.isAppOnBackground(getApplicationContext()) ||
                    CommonUtil.isLockScreeen(getApplicationContext())){
                saveNoteData(true);//处于后台时保存数据
            }

            if (subsLoading != null && subsLoading.isDisposed()){
                subsLoading.dispose();
            }
            if (subsInsert != null && subsInsert.isDisposed()){
                subsInsert.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出处理
     */
    private void dealwithExit(){
        try {
            String noteTitle = et_new_title.getText().toString();
            String noteContent = getEditData();
            String groupName = tv_new_group.getText().toString();
            String noteTime = tv_new_time.getText().toString();
            if (flag == 0) {//新建笔记
                if (noteTitle.length() > 0 || noteContent.length() > 0) {
                    saveNoteData(false);
                }
            }else if (flag == 1) {//编辑笔记
                if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                        || !noteTime.equals(myNoteTime)) {
                    saveNoteData(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (!iwHelper.handleBackPressed()) {
            super.onBackPressed();
        }
        dealwithExit();
    }

    private void sendRequest(String path){
        HashMap<String,String> data = new HashMap<>();
        data.put("upload_method",path);
        Log.e("test", path);
        HashMap<String, File> file_map = new HashMap<>();
        file_map.put(path + ".jpg",new File(path));
        LeHttp.sendJsonRequest("http://47.106.86.63:8080/file/upload/", data,file_map, File_Upload_Response.class, new LeJsonDataTransform<File_Upload_Response>() {
            @Override
            public void onSuccess(File_Upload_Response m) {
                Log.e("test", m.toString());
                Toast.makeText(getApplicationContext(),"上传成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                Log.i("test","failure");
                Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
