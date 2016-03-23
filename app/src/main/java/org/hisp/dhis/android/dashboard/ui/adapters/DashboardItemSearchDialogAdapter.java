/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hisp.dhis.android.dashboard.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.hisp.dhis.android.dashboard.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DashboardItemSearchDialogAdapter extends BaseAdapter implements Filterable {
    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();
    private final LayoutInflater mInflater;
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<OptionAdapterValue> mObjects;
    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<OptionAdapterValue> mOriginalValues;
    private ArrayFilter mFilter;

    public DashboardItemSearchDialogAdapter(LayoutInflater inflater) {
        mInflater = inflater;
        this.context = inflater.getContext();
        mObjects = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionAdapterValue getItem(int position) {
        return mObjects.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    /**
     *  This is to store the context of the calling activity
     */
    private Context context;

    /**
     * This is to keep track of the last position of the current view
     */
    private int lastPosition = -1;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }
    /**
     * Contribution by Devika Mehra
     * https://github.com/devikamehra
     * @param viewToAnimate view to be animated by the given set of animation
     * @param position the position of the item to which animation has been applied
     *
     * The following method would apply animation to individual items in the list of items.
     * Each item will fade and slide in from the left
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

    private View createViewFromResource(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(
                    R.layout.fragment_dialog_dashboard_item_option, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        setAnimation(holder.view, position);
        String item = getItem(position).label;
        holder.textView.setText(item);

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    public void swapData(List<OptionAdapterValue> values) {
        if (values == null) {
            values = new ArrayList<>();
        }

        clear();
        addAll(values);
        notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    private void addAll(Collection<OptionAdapterValue> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
    }

    /**
     * Remove all elements from the list.
     */
    private void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
    }

    private static class ViewHolder {
        public final TextView textView;
        public View view;
        public ViewHolder(View view) {
            this.view = view;
            textView = (TextView) view.findViewById(R.id.textview_item);
        }
    }

    public static class OptionAdapterValue {
        public final String id;
        public final String label;

        public OptionAdapterValue(String id, String label) {
            this.id = id;
            this.label = label;
        }

        private static boolean objectsEqual(Object a, Object b) {
            return a == b || (a != null && a.equals(b));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof OptionAdapterValue)) {
                return false;
            }
            OptionAdapterValue p = (OptionAdapterValue) o;
            return objectsEqual(p.id, label) && objectsEqual(p.id, label);
        }

        @Override
        public int hashCode() {
            return (id == null ? 0 : id.hashCode()) ^ (label == null ? 0 : label.hashCode());
        }
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<OptionAdapterValue> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<OptionAdapterValue> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<OptionAdapterValue> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final OptionAdapterValue optionValue = values.get(i);
                    final String value = optionValue.label;
                    final String valueText = value.toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(optionValue);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(optionValue);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<OptionAdapterValue>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}