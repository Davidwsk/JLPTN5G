package com.iscdasia.smartjlptn5_android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iscdasia.smartjlptn5_android.QuestionListFragment.OnListFragmentInteractionListener;
import com.iscdasia.smartjlptn5_android.model.Question;
import com.iscdasia.smartjlptn5_android.viewmodel.UserQuestionStatisticResult;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Question} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyQuestionListRecyclerViewAdapter extends RecyclerView.Adapter<MyQuestionListRecyclerViewAdapter.ViewHolder> {

    private final List<Question> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyQuestionListRecyclerViewAdapter(List<Question> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_questionlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getQuestionText());
        if(DataAccess.USER_QUESTION_STATISTIC_RESULT_ARRAY_LIST.size() > position) {
            UserQuestionStatisticResult userQuestionStatisticResult = DataAccess.USER_QUESTION_STATISTIC_RESULT_ARRAY_LIST.get(position);
            if (userQuestionStatisticResult != null) {
                holder.mUQStatisticView.setText(userQuestionStatisticResult.getAllResult());
                holder.mCurrentResult.setText(userQuestionStatisticResult.getCurrentResult());
            }
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public final TextView mUQStatisticView;
        public final TextView mCurrentResult;
        public Question mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
            mUQStatisticView = (TextView) view.findViewById(R.id.userQuestionStatistic);
            mCurrentResult = (TextView) view.findViewById(R.id.currentResult);
            if(CurrentApp.IsFinished) {
                mCurrentResult.setVisibility(View.VISIBLE);
                mUQStatisticView.setVisibility(View.VISIBLE);
            }
            else {
                mCurrentResult.setVisibility(View.GONE);
                mUQStatisticView.setVisibility(View.GONE);
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
