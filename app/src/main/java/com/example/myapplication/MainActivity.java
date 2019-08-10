package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.myapplication.Bean.Note;
import com.example.myapplication.activity.BaseActivity;
import com.example.myapplication.activity.NewActivity;
import com.example.myapplication.activity.NoteActivity;
import com.example.myapplication.adapter.headerAdapter;
import com.example.myapplication.adapter.itemAdapter;
import com.yalantis.phoenix.PullToRefreshView;
import com.youth.xframe.adapter.decoration.DividerDecoration;
import com.youth.xframe.adapter.decoration.StickyHeaderDecoration;

import org.litepal.LitePal;
import org.michaelbel.bottomsheet.BottomSheet;

import java.util.List;


public class MainActivity extends BaseActivity {
    private com.yalantis.phoenix.PullToRefreshView mPullToRefreshView;
    private RecyclerView recyclerView;

    private itemAdapter adapter;
    private PullToRefreshView.OnRefreshListener refresh_listener;
    private itemAdapter.onSwipeListener swipeListener;
    private List<Note> notes;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        notes = LitePal.findAll(Note.class);
        refresh_listener = new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notes = LitePal.findAll(Note.class);
                        adapter.setDataLists(notes);
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, 1000);
            }
        };
        swipeListener = new itemAdapter.onSwipeListener() {
            @Override
            public void click(int pos){
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                Bundle bundle = new Bundle();
                Note note = notes.get(pos);
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }

            @Override
            public void longclick(final int pos){
                create_bottom_sheet(pos);
            }

            @Override
            public void onDel(int pos) {
                LitePal.delete(Note.class,adapter.getItem(pos).getId());
                notes.remove(pos);
                adapter.setDataLists(notes);
            }

            @Override
            public void onTop(int pos) {
                //TODO 置顶
            }
        };
    }

    @Override
    public void initView() {
        mPullToRefreshView = findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(refresh_listener);
        recyclerView = findViewById(R.id.listview);
        adapter = new itemAdapter(recyclerView, notes);
        adapter.setOnDelListener(swipeListener);
        recyclerView.addItemDecoration(new DividerDecoration(Color.parseColor("#C4C4C4"),1));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        StickyHeaderDecoration decoration = new StickyHeaderDecoration(new headerAdapter(this).setNotes(notes));
        decoration.setIncludeHeader(false);
        recyclerView.addItemDecoration(decoration);
    }


    @Override
    public void onResume(){
        notes = LitePal.findAll(Note.class);
        adapter.setDataLists(notes);
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add:
                Intent intent = new Intent(MainActivity.this, NewActivity.class);
                intent.putExtra("groupName", "默认笔记");
                intent.putExtra("flag", 0);
                startActivity(intent);
                break;
            case R.id.menu_change:
                //TODO 多选编辑
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void create_bottom_sheet(final int pos){
        //删除便签，置顶，取消
        final String[] items;
        //TODO 判断是否已置顶
        if(true) {
            items = new String[] {
                            "置顶",
                            "删除便签",
                            "取消",
            };
        }else {
                    items = new String[] {
                            "取消置顶",
                            "删除便签",
                            "取消",
                    };
        }

        BottomSheet.Builder builder = new BottomSheet.Builder(MainActivity.this);
        builder.setTitle("操作");

        builder.setItems(items,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                        // your code here
                    switch (which){
                    case 0:
                        //TODO 置顶或取消置顶
                        break;
                    case 1:
                        LitePal.delete(Note.class,adapter.getItem(pos).getId());
                        notes.remove(pos);
                        adapter.setDataLists(notes);
                        break;
                    case 2:
                        break;
                        }
                    }
        });
        builder.setContentType(BottomSheet.LIST);
        builder.setTitleTextColor(0xFFFF5252);
        builder.setItemTextColor(0xFFFF5252);
        builder.setBackgroundColor(Color.WHITE);
        builder.setDividers(true);
        builder.show();
    }

}
