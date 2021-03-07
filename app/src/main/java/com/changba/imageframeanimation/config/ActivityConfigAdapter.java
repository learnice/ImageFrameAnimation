package com.changba.imageframeanimation.config;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.changba.imageframeanimation.R;

/**
 * @author HeXuebin on 2020/12/27.
 */
public class ActivityConfigAdapter<T extends IActivityConfig> extends RecyclerView.Adapter<ActivityConfigAdapter.VH> {
    private final T[] configs;

    public ActivityConfigAdapter(T[] configs) {
        this.configs = configs;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_config_activity_list, null, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final T t = configs[position];
        holder.btnAction.setText(t.getName());
        holder.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), t.getClazz()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return configs.length;
    }

    static class VH extends RecyclerView.ViewHolder {
        Button btnAction;

        public VH(@NonNull View itemView) {
            super(itemView);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}
