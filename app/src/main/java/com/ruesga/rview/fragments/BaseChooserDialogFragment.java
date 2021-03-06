/*
 * Copyright (C) 2016 Jorge Ruesga
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
package com.ruesga.rview.fragments;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.ruesga.rview.R;
import com.ruesga.rview.adapters.BaseAdapter;
import com.ruesga.rview.adapters.FilterableAdapter;
import com.ruesga.rview.databinding.BaseChooserDialogBinding;
import com.ruesga.rview.preferences.Constants;
import com.ruesga.rview.widget.DelayedAutocompleteTextView;

public class BaseChooserDialogFragment extends FilterableDialogFragment implements
        CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "BaseChooserDialogFragment";

    public static BaseChooserDialogFragment newInstance(
            int legacyChangeId, String projectId, String branch, View anchor, int requestCode) {
        BaseChooserDialogFragment fragment = new BaseChooserDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Constants.EXTRA_LEGACY_CHANGE_ID, legacyChangeId);
        arguments.putString(Constants.EXTRA_PROJECT_ID, projectId);
        arguments.putString(Constants.EXTRA_BRANCH, branch);
        arguments.putParcelable(EXTRA_ANCHOR, computeViewOnScreen(anchor));
        arguments.putInt(EXTRA_REQUEST_CODE, requestCode);
        fragment.setArguments(arguments);
        return fragment;
    }

    private BaseChooserDialogBinding mBinding;
    private BaseAdapter mAdapter;

    private int mLegacyChangeId;
    private String mProjectId;
    private String mBranch;

    public BaseChooserDialogFragment() {
    }

    @Override
    public int getFilterableItems() {
        return 1;
    }

    @Override
    public FilterableAdapter[] getAdapter() {
        return new FilterableAdapter[]{mAdapter};
    }

    @NonNull
    @Override
    public DelayedAutocompleteTextView[] getFilterView() {
        return new DelayedAutocompleteTextView[]{mBinding.base};
    }

    @Override
    public int getDialogTitle() {
        return R.string.change_action_rebase;
    }

    @Override
    public int getDialogActionLabel() {
        return R.string.change_action_rebase;
    }

    @Override
    public boolean isAllowEmpty() {
        return true;
    }

    @Override
    public boolean isSelectionRequired(int pos) {
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLegacyChangeId = getArguments().getInt(Constants.EXTRA_LEGACY_CHANGE_ID);
        mProjectId = getArguments().getString(Constants.EXTRA_PROJECT_ID);
        mBranch = getArguments().getString(Constants.EXTRA_BRANCH);
    }

    @Override
    public ViewDataBinding inflateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.base_chooser_dialog, container, true);
        mBinding.changeBase.setOnCheckedChangeListener(this);

        // initially 'change parent revision' is unchecked
        mBinding.baseWrapper.setEnabled(false);
        mBinding.baseWrapper.setHintEnabled(false);

        mAdapter = new BaseAdapter(mBinding.getRoot().getContext(),
                mLegacyChangeId, mProjectId, mBranch);
        return mBinding;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        mBinding.baseWrapper.setEnabled(checked);
        mBinding.baseWrapper.setHintEnabled(checked);
    }

    @Override
    public Object transformResult(int pos, String result) {
        if (!mBinding.changeBase.isChecked()) {
            return null;
        }
        if (TextUtils.isEmpty(result)) {
            return "";
        }

        try {
            return String.valueOf(Integer.valueOf(result.substring(0, result.indexOf(":"))));
        } catch (Exception ex) {
            return null;
        }
    }
}
