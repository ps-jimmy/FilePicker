/*
 * Created by Jaisel Rahman <jaiselrahman@gmail.com>.
 * Copyright (c) 2018. All rights reserved.
 * This file belongs to StudentCorner project.
 */

package com.jaisel.filepicker.adapter;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaisel.filepicker.model.File;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class MultiSelectionAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final String TAG = MultiSelectionAdapter.class.getSimpleName();
    private ArrayList<File> selectedItems = new ArrayList<>();
    private ArrayList<File> files;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnSelectionListener<VH> customOnSelectionListener;
    private boolean isSelectionStarted = false;
    private boolean enabledSelection = false;
    private boolean isSingleClickSelection = false;
    private int maxSelection = -1;
    private OnSelectionListener<VH> onSelectionListener = new OnSelectionListener<VH>() {
        @Override
        public void onSelectionBegin() {
            isSelectionStarted = true;
            if (customOnSelectionListener != null) customOnSelectionListener.onSelectionBegin();
        }

        @Override
        public void onSelected(VH viewHolder, int position) {
            if (maxSelection > 0 && selectedItems.size() >= maxSelection) {
                onMaxReached();
                return;
            }
            setItemSelected(viewHolder.itemView, position, true);
            if (customOnSelectionListener != null)
                customOnSelectionListener.onSelected(viewHolder, position);
        }

        @Override
        public void onUnSelected(VH viewHolder, int position) {
            setItemSelected(viewHolder.itemView, position, false);
            if (customOnSelectionListener != null)
                customOnSelectionListener.onUnSelected(viewHolder, position);
        }

        @Override
        public void onSelectAll() {
            isSelectionStarted = true;
            selectedItems.clear();
            selectedItems.addAll(files);
            notifyDataSetChanged();
            if (customOnSelectionListener != null) customOnSelectionListener.onSelectAll();
        }

        @Override
        public void onUnSelectAll() {
            for (int i = selectedItems.size() - 1; i >= 0; i--) {
                int position = files.indexOf(selectedItems.get(i));
                removeSelection(position);
                handleItemChanged(position);
            }
            isSelectionStarted = false;
            if (customOnSelectionListener != null) customOnSelectionListener.onUnSelectAll();
        }

        @Override
        public void onSelectionEnd() {
            isSelectionStarted = false;
            if (customOnSelectionListener != null) customOnSelectionListener.onSelectionEnd();
        }

        @Override
        public void onMaxReached() {
            if (customOnSelectionListener != null) customOnSelectionListener.onMaxReached();
        }
    };

    public MultiSelectionAdapter(ArrayList<File> items) {
        this.files = items;
    }

    public int getMaxSelection() {
        return maxSelection;
    }

    public void setMaxSelection(int maxSelection) {
        this.maxSelection = maxSelection;
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        final View view = holder.itemView;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (enabledSelection && (isSelectionStarted || isSingleClickSelection)) {
                    if (selectedItems.contains(files.get(position))) {
                        onSelectionListener.onUnSelected(holder, position);
                        if (selectedItems.isEmpty()) {
                            onSelectionListener.onSelectionEnd();
                        }
                    } else {
                        onSelectionListener.onSelected(holder, position);
                    }
                }
                if (onItemClickListener != null)
                    onItemClickListener.onClick(v, position);
            }
        });

        setItemSelected(view, position, selectedItems.contains(files.get(position)));

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                if (enabledSelection) {
                    if (!isSelectionStarted) {
                        onSelectionListener.onSelectionBegin();
                        onSelectionListener.onSelected(holder, position);
                    } else if (selectedItems.size() <= 1
                            && selectedItems.contains(files.get(position))) {
                        onSelectionListener.onSelectionEnd();
                        onSelectionListener.onUnSelected(holder, position);
                    }
                }
                return onItemLongClickListener == null ||
                        onItemLongClickListener.onLongClick(view, position);
            }
        });
    }

    public boolean isSelected(File file) {
        return selectedItems.contains(file);
    }

    public void enableSelection(boolean enabledSelection) {
        this.enabledSelection = enabledSelection;
    }

    public void enableSingleClickSelection(boolean enableSingleClickSelection) {
        this.enabledSelection = enableSingleClickSelection || enabledSelection;
        this.isSingleClickSelection = enableSingleClickSelection;
    }

    public void setOnItemClickListener(OnItemClickListener onClickListener) {
        this.onItemClickListener = onClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnSelectionListener(OnSelectionListener<VH> onSelectionListener) {
        this.customOnSelectionListener = onSelectionListener;
    }

    public ArrayList<File> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(ArrayList<File> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void unSelectAll() {
        onSelectionListener.onUnSelectAll();
    }

    public void selectAll() {
        onSelectionListener.onSelectAll();
    }

    public void handleDataSetChanged() {
        notifyDataSetChanged();
    }

    public void handleItemChanged(int position) {
        notifyItemChanged(position);
    }

    public void handleItemInserted(int position) {
        notifyItemInserted(position);
    }

    public void handleItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void handleItemRemoved(int position) {
        if (enabledSelection) {
            removeSelection(position);
        }
        notifyItemRemoved(position);
    }

    public void handleItemRangeRemoved(int positionStart, int itemCount) {
        if (enabledSelection) {
            for (int i = positionStart; i < itemCount; i++) {
                removeSelection(i);
            }
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    private void setItemSelected(View view, int position, boolean selected) {
        if (selected) {
            if (!selectedItems.contains(files.get(position)))
                selectedItems.add(files.get(position));
        } else {
            selectedItems.remove(files.get(position));
            if (selectedItems.isEmpty()) {
                onSelectionListener.onSelectionEnd();
            }
        }
    }

    private void removeSelection(int position) {
        selectedItems.remove(files.get(position));
        if (selectedItems.isEmpty()) {
            onSelectionListener.onSelectionEnd();
        }
    }

    public boolean add(File file) {
        if (files.add(file)) {
            handleItemInserted(files.size() - 1);
            return true;
        }
        return false;
    }

    public void add(int position, File file) {
        files.add(position, file);
        handleItemInserted(position);
    }

    public boolean addAll(Collection<File> itemSelection) {
        int lastPosition = files.size();
        if (files.addAll(itemSelection)) {
            notifyItemRangeInserted(lastPosition, itemSelection.size());
            return true;
        }
        return false;
    }

    public boolean addAll(int position, Collection<File> itemCollection) {
        int lastPosition = files.size();
        if (files.addAll(position, itemCollection)) {
            handleItemRangeInserted(lastPosition, files.size());
            return true;
        }
        return false;
    }

    public void remove(File item) {
        int position = files.indexOf(item);
        handleItemRemoved(position);
        files.remove(position);
    }

    public File remove(int position) {
        handleItemRemoved(position);
        return files.remove(position);
    }

    public void removeAll(Collection<File> itemCollection) {
        ArrayList<File> removeItems = new ArrayList<>(itemCollection);
        for (int i = itemCollection.size() - 1; i >= 0; i--) {
            remove(removeItems.get(i));
        }
    }

    public interface OnItemClickListener {
        void onClick(View v, int position);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(View v, int postion);
    }

    public interface OnSelectionListener<VH> {
        void onSelectionBegin();

        void onSelected(VH viewHolder, int position);

        void onUnSelected(VH viewHolder, int position);

        void onSelectAll();

        void onUnSelectAll();

        void onSelectionEnd();

        void onMaxReached();
    }
}