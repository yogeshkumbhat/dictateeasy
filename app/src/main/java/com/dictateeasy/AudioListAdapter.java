package com.dictateeasy;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {

    private File[] allFiles;
    private TimeAgo timeAgo;
    private PopupMenu popupMenu;
    private Context mCtx;
    private  AudioViewHolder viewHolder;
    private onItemListClick onItemListClick;

    public AudioListAdapter(File[] allFiles, onItemListClick onItemListClick,Context mCtx) {
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
        this.mCtx=mCtx;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.list_title.setText(allFiles[position].getName());
        holder.list_date.setText(timeAgo.getTimeAgo(allFiles[position].lastModified()));

    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView list_image;
        private TextView list_title;
        private TextView list_date;
        private TextView list_item_option;
        private ConstraintLayout audioSnippet;
       // private ConstraintLayout audio_row;

        public AudioViewHolder(@NonNull final View itemView) {
            super(itemView);

            list_image = itemView.findViewById(R.id.list_image_view);
            list_title = itemView.findViewById(R.id.list_title);
            list_date = itemView.findViewById(R.id.list_date);
            list_item_option=itemView.findViewById(R.id.list_item_option);
            audioSnippet=(ConstraintLayout) itemView.findViewById(R.id.audio_snippet);

            //itemView.setOnClickListener(this);
            if(audioSnippet!=null) {
                audioSnippet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("snippet_clicked", allFiles.toString() + "");
                        //play audio

                        onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition(),1,itemView);
                    }
                });
            }
            if(list_item_option!=null) {
                list_item_option.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("option_clicked", v.getTag() + "");
                        inflateMenu(v,itemView);
                    }
                });
            }

        }
        public void inflateMenu(View v, final View itemView)
        {
            //open menu
            //creating a popup menu
            viewHolder=new AudioViewHolder(v);
            PopupMenu popup = new PopupMenu(mCtx, viewHolder.list_item_option);
            //inflating menu from xml resource
            popup.inflate(R.menu.list_item_menu);
            //adding click listener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.edit_btn:
                            //handle menu1 click
                            Log.e("menu_btn","edit");
                            onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition(),2,itemView);
                            break;
                        case R.id.deletebtn:
                            //handle menu2 click
                            Log.e("menu_btn","delete");
                            Log.e("sent_position1",getAdapterPosition()+"");
                            int file_position=getAdapterPosition();
                            onItemListClick.onClickListener(allFiles[file_position], file_position,3,itemView);
                            Log.e("sent_position",file_position+"");
                            allFiles = removeTheElement(allFiles, file_position);


                           // Log.e("file_arr"+itemView.getAdapterPosition(), Arrays.toString(allFiles));
                            break;

                    }
                    return false;
                }
            });
            //displaying the popup
            popup.show();
        }

        @Override
        public void onClick(View view) {
            Log.e("id dede",view.getTag()+"");
        }
    }
    // Function to remove the element
    public  static File[] removeTheElement(File[] arr,int index)
    {

        // If the array is empty
        // or the index is not in array range
        // return the original array
        if (arr == null
                || index < 0
                || index >= arr.length) {

            return arr;
        }

        // Create another array of size one less
        File[] anotherArray = new File[arr.length - 1];

        // Copy the elements except the index
        // from original array to the other array
        for (int i = 0, k = 0; i < arr.length; i++) {

            // if the index is
            // the removal element index
            if (i == index) {
                continue;
            }

            // if the index is not
            // the removal element index
            anotherArray[k++] = arr[i];
        }

        // return the resultant array
        return anotherArray;
    }

    public interface onItemListClick {
        void onClickListener(File file, int position,int action,View v);
        //action 1 to play
        //2 to edit
        //3 to delete
    }

}
