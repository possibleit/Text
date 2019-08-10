package com.example.myapplication.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.Bean.Note;
import com.example.myapplication.R;
import com.youth.xframe.adapter.XRecyclerViewAdapter;
import com.youth.xframe.adapter.XViewHolder;

import java.util.List;


public class itemAdapter extends XRecyclerViewAdapter<Note> {

    private onSwipeListener mOnSwipeListener;
    public itemAdapter(@NonNull RecyclerView mRecyclerView, List<Note> dataLists){
        super(mRecyclerView,dataLists);
    }

    @Override
    public int getItemLayoutResId(Note note,int position){
        int layoutResId = -1;
        if (note.hasImage()){
            layoutResId = R.layout.image_item;
        }else {
            layoutResId = R.layout.item;
        }
        return layoutResId;
    }

    @Override
    public void bindData(final XViewHolder holder, Note note, final int position) {

        if(note.hasImage()){//判断是否含有图片

            LinearLayout content = holder.getView(R.id.imgitem_content);
            TextView textView = holder.getView(R.id.img_item_text);
            TextView textView2 = holder.getView(R.id.img_item_time);
            ImageView imageView = holder.getView(R.id.img_item_img);
            Button imgitem_btnTop = holder.getView(R.id.imgitem_btnTop);
            Button imgitem_btnDelete = holder.getView(R.id.imgitem_btnDelete);

            textView.setText(note.getTitle());
            textView2.setText(note.getCreateTime());
            imageView.setImageURI(Uri.parse(note.getImagePath()));
            imgitem_btnTop.setText(true ? "取消置顶":"置顶");
            content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (null != mOnSwipeListener) {
                        //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                        //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                        //((CstSwipeDelMenu) holder.itemView).quickClose();
                        /**
                         * 使用holder.getAdapterPosition()以更新改动后的item的索引值
                         */
                        mOnSwipeListener.longclick(holder.getAdapterPosition());
                    }
                    return false;
                }
            });
            //注意事项，设置item点击，不能对整个holder.itemView设置咯，只能对第一个子View，即原来的content设置，这算是局限性吧。
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnSwipeListener) {
                        mOnSwipeListener.click(holder.getAdapterPosition());
                    }
                }
            });

//            imgitem_btnUnRead.setVisibility(position % 3 == 0 ? View.GONE : View.VISIBLE);

            imgitem_btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnSwipeListener) {
                        //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                        //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                        //((CstSwipeDelMenu) holder.itemView).quickClose();
                        mOnSwipeListener.onDel(holder.getAdapterPosition());
                    }
                }
            });

            //置顶：
            imgitem_btnTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null!=mOnSwipeListener){
                        mOnSwipeListener.onTop(holder.getAdapterPosition());
                    }
                }
            });
        }else {
            LinearLayout content = holder.getView(R.id.item_content);
            TextView textView = holder.getView(R.id.item_text);
            TextView textView2 = holder.getView(R.id.item_time);
            Button item_btnTop = holder.getView(R.id.item_btnTop);
            Button item_btnDelete = holder.getView(R.id.item_btnDelete);
            textView.setText(note.getTitle());
            textView2.setText(note.getCreateTime());
            item_btnTop.setText("置顶");


            content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    Toast.makeText(getContext(), "longclig", Toast.LENGTH_SHORT).show();
//                    Log.d("TAG", "onLongClick() called with: v = [" + v + "]");
//                    return false;
                    if (null != mOnSwipeListener) {
                        //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                        //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                        //((CstSwipeDelMenu) holder.itemView).quickClose();
                        mOnSwipeListener.longclick(holder.getAdapterPosition());
                    }
                    return false;
                }
            });

            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnSwipeListener) {
                        mOnSwipeListener.click(holder.getAdapterPosition());
                    }
                }
            });

//            imgitem_btnUnRead.setVisibility(position % 3 == 0 ? View.GONE : View.VISIBLE);

            item_btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnSwipeListener) {
                        //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                        //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                        //((CstSwipeDelMenu) holder.itemView).quickClose();
                        mOnSwipeListener.onDel(holder.getAdapterPosition());
                    }
                }
            });

            //置顶：
            item_btnTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null!=mOnSwipeListener){
                        mOnSwipeListener.onTop(holder.getAdapterPosition());
                    }

                }
            });
        }
    }

    /**
     * 和Activity通信的接口
     */
    public interface onSwipeListener {
        void click(int pos);

        void longclick(int pos);

        void onDel(int pos);

        void onTop(int pos);
    }



    public onSwipeListener getOnDelListener() {
        return mOnSwipeListener;
    }

    public void setOnDelListener(onSwipeListener mOnDelListener) {
        this.mOnSwipeListener = mOnDelListener;
    }
}

