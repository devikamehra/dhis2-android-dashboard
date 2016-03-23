package org.hisp.dhis.android.dashboard.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.ui.models.Field;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by arazabishov on 7/27/15.
 */
public class AccountFieldAdapter extends AbsAdapter<Field, AccountFieldAdapter.FieldViewHolder> {
    /**
     *  This is to store the context of the calling activity
     */
    private Context context;

    /**
     * This is to keep track of the last position of the current view
     */
    private int lastPosition = -1;

    public AccountFieldAdapter(Context context, LayoutInflater inflater) {
        super(context, inflater);
        this.context = context;
    }

    /**
     * Contribution by Devika Mehra
     * https://github.com/devikamehra
     * @param viewToAnimate view to be animated by the given set of animation
     * @param position the position of the item to which animation has been applied
     *
     * The following method would apply animation to individual Account field items which would
     * sliding in from the left and then fades in.
     *
     */
    private void setAnimation(View viewToAnimate, int position)
    {
        if (position > lastPosition)
        {
            AnimationSet set = new AnimationSet(true);
            set.addAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
            set.addAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_in));
            viewToAnimate.startAnimation(set);
            lastPosition = position;
        }
    }
    @Override
    public FieldViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FieldViewHolder(getLayoutInflater()
                .inflate(R.layout.recycler_view_field_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FieldViewHolder holder, int position) {
        setAnimation(holder.itemView, position);
        Field field = getData().get(position);
        holder.labelTextView.setText(field.getLabel());
        holder.valueTextView.setText(field.getValue());
    }

    public static class FieldViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.field_label_text_view)
        public TextView labelTextView;

        @Bind(R.id.field_value_text_view)
        public TextView valueTextView;

        public FieldViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
