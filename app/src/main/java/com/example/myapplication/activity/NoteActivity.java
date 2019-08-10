package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myapplication.Bean.Note;
import com.example.myapplication.GlideSimpleLoader;
import com.example.myapplication.R;
import com.example.myapplication.util.CommonUtil;
import com.example.myapplication.util.ImageUtils;
import com.example.myapplication.util.StringUtils;
import com.github.ielse.imagewatcher.ImageWatcherHelper;
import com.sendtion.xrichtext.RichTextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NoteActivity extends AppCompatActivity {
    private static final String TAG = "NoteActivity";

    private TextView tv_note_title;//笔记标题
    private RichTextView tv_note_content;//笔记内容
    private TextView tv_note_time;//笔记创建时间
    private TextView tv_note_group;//选择笔记分类
    private ScrollView shot_view;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private String myGroupName;

    private ProgressDialog loadingDialog;
    private Disposable mDisposable;
    private ImageWatcherHelper iwHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initView();

    }

    private void initView() {

        iwHelper = ImageWatcherHelper.with(this, new GlideSimpleLoader());

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        tv_note_title = (TextView) findViewById(R.id.tv_note_title);//标题
        tv_note_title.setTextIsSelectable(true);
        tv_note_content = (RichTextView) findViewById(R.id.tv_note_content);//内容
        tv_note_time = (TextView) findViewById(R.id.tv_note_time);
        tv_note_group = (TextView) findViewById(R.id.tv_note_group);

        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("data");
            note = (Note) bundle.getSerializable("note");

            if (note != null) {
                myTitle = note.getTitle();
                myContent = note.getContent();

                tv_note_title.setText(myTitle);
                tv_note_content.post(new Runnable() {
                    @Override
                    public void run() {
                        dealWithContent();
                    }
                });
                tv_note_time.setText(note.getCreateTime());
                tv_note_group.setText("默认笔记");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dealWithContent(){
        //showEditData(myContent);
        tv_note_content.clearAllLayout();
        showDataSync(myContent);

        // 图片点击事件
        tv_note_content.setOnRtImageClickListener(new RichTextView.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(View view, String imagePath) {
                try {
                    ArrayList<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                    int currentPosition = imageList.indexOf(imagePath);
                    //showToast("点击图片："+currentPosition+"："+imagePath);

                    List<Uri> dataList = new ArrayList<>();
                    for (int i = 0; i < imageList.size(); i++) {
                        dataList.add(ImageUtils.getUriFromPath(imageList.get(i)));
                    }
                    iwHelper.show(dataList, currentPosition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 异步方式显示数据
     * @param html
     */
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
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        //showToast("解析错误：图片不存在或已损坏");
                        Log.e(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(String text) {
                        try {
                            if (tv_note_content !=null) {
                                if (text.contains("<img") && text.contains("src=")) {
                                    //imagePath可能是本地路径，也可能是网络地址
                                    Log.e("test", text);
                                    String imagePath = StringUtils.getImgSrc(text);
                                    Log.e("test", imagePath);
                                    tv_note_content.addImageViewAtIndex(tv_note_content.getLastIndex(), imagePath);
                                } else {
                                    tv_note_content.addTextViewAtIndex(tv_note_content.getLastIndex(), text);
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
     * @param html
     */
    private void showEditData(ObservableEmitter<String> emitter, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        } catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_note_edit://编辑笔记
                Intent intent = new Intent(NoteActivity.this, NewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                intent.putExtra("flag", 1);//编辑笔记
                startActivity(intent);
                finish();
                break;
            case R.id.action_note_share://分享笔记
                CommonUtil.shareTextAndImage(this, note.getTitle(), note.getContent(), null);//分享图文
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }

    @Override
    public void onBackPressed() {
        if (!iwHelper.handleBackPressed()) {
            super.onBackPressed();
        }
        finish();
    }
}
