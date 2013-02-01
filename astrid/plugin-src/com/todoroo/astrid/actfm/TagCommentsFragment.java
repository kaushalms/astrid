/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.actfm;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.timsu.astrid.R;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.utility.DateUtilities;
import com.todoroo.andlib.utility.Preferences;
import com.todoroo.astrid.activity.TaskListActivity;
import com.todoroo.astrid.adapter.UpdateAdapter;
import com.todoroo.astrid.data.RemoteModel;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.data.UserActivity;
import com.todoroo.astrid.helper.AsyncImageView;
import com.todoroo.astrid.service.StatisticsConstants;
import com.todoroo.astrid.service.TagDataService;
import com.todoroo.astrid.tags.TagService;
import com.todoroo.astrid.utility.AstridPreferences;

public class TagCommentsFragment extends CommentsFragment {

    private TagData tagData;

    @Autowired
    private TagDataService tagDataService;

    public TagCommentsFragment() {
        super();
    }

    public TagCommentsFragment(TagData tagData) {
        this();
        this.tagData = tagData;
    }

    @Override
    protected int getLayout() {
        return R.layout.tag_updates_fragment;
    }

    @Override
    protected void loadModelFromIntent(Intent intent) {
        if (tagData == null)
            tagData = intent.getParcelableExtra(TagViewFragment.EXTRA_TAG_DATA);
    }

    @Override
    protected boolean hasModel() {
        return tagData != null;
    }

    @Override
    protected String getModelName() {
        return tagData.getValue(TagData.NAME);
    }

    @Override
    protected Cursor getCursor() {
        return tagDataService.getUpdates(tagData);
    }

    @Override
    protected String getSourceIdentifier() {
        return (tagData == null) ? UpdateAdapter.FROM_RECENT_ACTIVITY_VIEW : UpdateAdapter.FROM_TAG_VIEW;
    }

    @Override
    protected void addHeaderToListView(ListView listView) {
        if (AstridPreferences.useTabletLayout(getActivity()) && tagData != null) {
            listHeader = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.tag_updates_header, listView, false);
            populateListHeader(listHeader);
            listView.addHeaderView(listHeader);
        }
    }

    @Override
    protected void populateListHeader(ViewGroup header) {
        if (header == null) return;
        TextView tagTitle = (TextView) header.findViewById(R.id.tag_title);
        String tagName = tagData.getValue(TagData.NAME);
        tagTitle.setText(tagName);
        TextView descriptionTitle = (TextView) header.findViewById(R.id.tag_description);
        String description = tagData.getValue(TagData.TAG_DESCRIPTION);
        if (!TextUtils.isEmpty(description)) {
            descriptionTitle.setText(description);
            descriptionTitle.setVisibility(View.VISIBLE);
        }
        else {
            descriptionTitle.setVisibility(View.GONE);
        }


        AsyncImageView imageView = (AsyncImageView) header.findViewById(R.id.tag_picture);
        imageView.setDefaultImageResource(TagService.getDefaultImageIDForTag(tagName));
        imageView.setUrl(tagData.getPictureUrl(TagData.PICTURE, RemoteModel.PICTURE_MEDIUM));
    }

    @Override
    protected void performFetch(boolean manual, Runnable done) {
        done.run();
//        actFmSyncService.fetchUpdatesForTag(tagData, manual, done);
    }

    @Override
    protected UserActivity createUpdate() {
        UserActivity userActivity = new UserActivity();
        userActivity.setValue(UserActivity.MESSAGE, addCommentField.getText().toString());
        userActivity.setValue(UserActivity.ACTION, UserActivity.ACTION_TAG_COMMENT);
        userActivity.setValue(UserActivity.USER_UUID, Task.USER_ID_SELF);
        userActivity.setValue(UserActivity.TARGET_ID, tagData.getUuid());
        userActivity.setValue(UserActivity.TARGET_NAME, tagData.getValue(TagData.NAME));
        userActivity.setValue(UserActivity.CREATED_AT, DateUtilities.now());
        return userActivity;
    }

    @Override
    protected String commentAddStatistic() {
        return StatisticsConstants.ACTFM_TAG_COMMENT;
    }

    @Override
    protected void setLastViewed() {
        if(tagData != null && RemoteModel.isValidUuid(tagData.getValue(TagData.UUID))) {
            Preferences.setLong(UPDATES_LAST_VIEWED + tagData.getValue(TagData.UUID), DateUtilities.now());
            Activity activity = getActivity();
            if (activity instanceof TaskListActivity)
                ((TaskListActivity) activity).setCommentsCount(0);
        }
    }


}
