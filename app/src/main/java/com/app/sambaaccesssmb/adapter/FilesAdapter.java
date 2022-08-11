package com.app.sambaaccesssmb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sambaaccesssmb.interfaces.FilesClickListener;
import com.app.sambaaccesssmb.model.FilesModel;
import com.app.sambaaccesssmb.R;
import com.app.sambaaccesssmb.utils.Utils;

import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private final List<FilesModel> filesList;
    private final Context context;
    private final FilesClickListener filesClickListener;

    public FilesAdapter(Context context, List<FilesModel> filesList, FilesClickListener filesClickListener) {
        this.context = context;
        this.filesList = filesList;
        this.filesClickListener = filesClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_files_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilesModel smbFile = filesList.get(position);
        String fileName = smbFile.getName();
        holder.txtFileName.setText(fileName);
        if (smbFile.isDirectory()) {
            holder.imgFileIcon.setImageResource(R.drawable.folder_v2);
            holder.txtExtension.setVisibility(View.GONE);
        } else {
            holder.txtFileName.setVisibility(View.VISIBLE);
            String extension = Utils.getExtension(fileName);
            int icon = Utils.getIconFromExtension(extension);
            if (icon == -1) {
                holder.imgFileIcon.setImageResource(R.drawable.ic_file);
                holder.txtExtension.setText(extension.toUpperCase());
                holder.txtExtension.setVisibility(View.VISIBLE);
            } else {
                holder.imgFileIcon.setImageResource(icon);
                holder.txtExtension.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(view -> filesClickListener.onFileClick(position));
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtFileName;
        private final TextView txtExtension;
        private final ImageView imgFileIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFileIcon = itemView.findViewById(R.id.imgFileIcon);
            txtExtension = itemView.findViewById(R.id.txtExtension);
            txtFileName = itemView.findViewById(R.id.txtFileName);
        }
    }
}
