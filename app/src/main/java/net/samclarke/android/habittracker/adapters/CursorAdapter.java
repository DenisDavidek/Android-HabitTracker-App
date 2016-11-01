package net.samclarke.android.habittracker.adapters;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

public abstract class CursorAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {

    private Cursor mCursor;
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetChanged();
        }
    };

    public abstract void onBindViewHolder(T viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(T viewHolder, int position) {
        if (mCursor == null) {
            throw new IllegalStateException("Cursor should be set");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid position " + position);
        }

        onBindViewHolder(viewHolder, mCursor);
    }


    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        Cursor oldCursor = mCursor;
        mCursor = newCursor;

        if (oldCursor != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }

        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

        notifyDataSetChanged();

        return oldCursor;
    }
}
