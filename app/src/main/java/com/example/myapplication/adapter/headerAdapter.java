package com.example.myapplication.adapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.Bean.Note;
import com.example.myapplication.R;
import com.example.myapplication.util.CommonUtil;
import com.youth.xframe.adapter.decoration.StickyHeaderDecoration;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 悬浮headerAdapter
 */
public class headerAdapter implements StickyHeaderDecoration.IStickyHeaderAdapter<headerAdapter.HeaderHolder> {

    private LayoutInflater mInflater;
    private List<Note> notes;
    private Date date;
    private Calendar calendar = Calendar.getInstance();
    private int count = 0;
    private int year;

    public headerAdapter setNotes(List<Note> notes) {
        this.notes = notes;
        return this;
    }

    public headerAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public long getHeaderId(int position) {
//        return position / 3;
        if (notes != null){
            if (date == null){
                date = CommonUtil.string2date(notes.get(position).getCreateTime());
                calendar.setTime(date);
                year = calendar.get(Calendar.YEAR);
            }else {
                calendar.setTime(CommonUtil.string2date(notes.get(position).getCreateTime()));
                if (year == calendar.get(Calendar.YEAR)){
                    return count;
                }else {
                    year = calendar.get(Calendar.YEAR);
                    Log.e("debug",String.valueOf(year));
                    return count++;
                }
            }
        }
        return 0;
    }

    @Override
    public HeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.header_item, parent, false);
        return new HeaderHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(headerAdapter.HeaderHolder viewholder, int position) {
        viewholder.header.setText(String.valueOf(year) + "年");
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        public TextView header;

        public HeaderHolder(View itemView) {
            super(itemView);
            header = (TextView) itemView;
        }
    }
}

