package com.cryotech.notepad;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {

    private static RecyclerViewClickListener itemListener;
    private ArrayList<Note> noteList;
    private ArrayList<Note> originalList;
    private TextView resultCount;
    private Context ctx;
    private MyViewHolder viewHolder;
    public int filterPref = 0;

    public interface RecyclerViewClickListener
    {
        void ItemClicked(int position);
        void ItemLongClicked(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View card;
        public TextView title, date, time;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            card = view;
            title = view.findViewById(R.id.note_title);
            date = view.findViewById(R.id.note_date);
            time = view.findViewById(R.id.note_time);
            thumbnail = view.findViewById(R.id.note_icon);
        }
    }


    public NoteAdapter(ArrayList<Note> noteList, Context mContext, TextView resultCount, RecyclerViewClickListener itemListener) {
        this.ctx = mContext;
        this.noteList = noteList;
        this.resultCount = resultCount;
        this.itemListener = itemListener;
    }

    public void removeItem(int position)
    {
        noteList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Note> results = new ArrayList<>();
                if (originalList == null)
                    originalList = noteList;
                if (constraint != null) {
                    if (originalList != null && originalList.size() > 0) {
                        for (final Note n : originalList) {

                            switch (filterPref) {
                                case 0:
                                    if (n.getTitle()
                                            .contains(constraint.toString()))
                                        results.add(n);
                                    break;
                                case 1:
                                    if (n.getDateFormat()
                                            .contains(constraint.toString()))
                                        results.add(n);
                                    break;
                                case 2:
                                    if (n.getTimeFormat()
                                            .contains(constraint.toString()))
                                        results.add(n);
                                    break;
                            }
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          Filter.FilterResults results) {

                if (noteList != null)
                {
                    noteList = (ArrayList<Note>) results.values;
                    resultCount.setText("Displaying " + noteList.size() + " Note(s)");
                    notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_note_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final Note note = noteList.get(position);
        viewHolder = holder;
        viewHolder.title.setText(note.getTitle());
        viewHolder.date.setText(note.getDateFormat());
        viewHolder.time.setText(note.getTimeFormat());

        if (note.getLock())
        {
            Glide.with(ctx).load(R.drawable.ic_locked).into(holder.thumbnail);
        }
        else
        {
            Glide.with(ctx).load(R.drawable.ic_unlocked).into(holder.thumbnail);
        }

        holder.card.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                itemListener.ItemClicked(position);

            }
        });

        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                itemListener.ItemLongClicked(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public ArrayList<Note> getList() { return noteList; }
}
