/*******************************************************************************
 * Copyright 2011, 2012, 2013 fanfou.com, Xiaoke, Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fanfou.app.opensource.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.R;
import com.fanfou.app.opensource.api.bean.Status;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.util.DateTimeHelper;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.25
 * @version 1.1 2011.10.26
 * @version 2.0 2011.12.06
 * 
 */
public class StatusArrayAdapter extends BaseArrayAdapter<Status> {

    static class ViewHolder {
        ImageView headIcon = null;
        ImageView replyIcon = null;
        ImageView photoIcon = null;
        TextView nameText = null;
        TextView metaText = null;
        TextView contentText = null;

        ViewHolder(final View base) {
            this.headIcon = (ImageView) base
                    .findViewById(R.id.item_status_head);
            this.replyIcon = (ImageView) base
                    .findViewById(R.id.item_status_icon_reply);
            this.photoIcon = (ImageView) base
                    .findViewById(R.id.item_status_icon_photo);
            this.contentText = (TextView) base
                    .findViewById(R.id.item_status_text);
            this.metaText = (TextView) base.findViewById(R.id.item_status_meta);
            this.nameText = (TextView) base.findViewById(R.id.item_status_user);

        }
    }

    private static final String TAG = StatusArrayAdapter.class.getSimpleName();
    private static final int NONE = 0;
    private static final int MENTION = 1;
    private static final int SELF = 2;

    private static final int[] TYPES = new int[] { StatusArrayAdapter.NONE,
            StatusArrayAdapter.MENTION, StatusArrayAdapter.SELF, };

    private boolean colored;
    private int mMentionedBgColor;// = 0x332266aa;

    private int mSelfBgColor;// = 0x33999999;

    private List<Status> mStatus;

    public StatusArrayAdapter(final Context context, final List<Status> ss) {
        super(context, ss);
        init(context, false);
        if (ss == null) {
            this.mStatus = new ArrayList<Status>();
        } else {
            this.mStatus = ss;
        }
    }

    public StatusArrayAdapter(final Context context, final List<Status> ss,
            final boolean colored) {
        super(context, ss);
        init(context, colored);
        if (ss == null) {
            this.mStatus = new ArrayList<Status>();
        } else {
            this.mStatus = ss;
        }
    }

    @Override
    public int getCount() {
        return this.mStatus.size();
    }

    protected String getDateString(final Date date) {
        return DateTimeHelper.getInterval(date);
    }

    @Override
    public Status getItem(final int position) {
        return this.mStatus.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemViewType(final int position) {
        final Status s = getItem(position);
        if ((s == null) || s.isNull()) {
            return StatusArrayAdapter.NONE;
        }
        if (s.simpleText.contains("@" + AppContext.getUserName())) {
            return StatusArrayAdapter.MENTION;
        } else {
            return s.self ? StatusArrayAdapter.SELF : StatusArrayAdapter.NONE;
        }
    }

    @Override
    int getLayoutId() {
        return R.layout.list_item_status;
    }

    @Override
    public View getView(final int position, View convertView,
            final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(getLayoutId(), null);
            holder = new ViewHolder(convertView);
            setTextStyle(holder);
            setHeadImage(holder.headIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Status s = this.mStatus.get(position);

        if (!isTextMode()) {
            holder.headIcon.setTag(s.userProfileImageUrl);
            this.mLoader.displayImage(s.userProfileImageUrl, holder.headIcon,
                    R.drawable.default_head);
            holder.headIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (s != null) {
                        ActionManager.doProfile(
                                StatusArrayAdapter.this.mContext, s);
                    }
                }
            });
        }

        if (this.colored) {
            final int itemType = getItemViewType(position);
            switch (itemType) {
            case MENTION:
                convertView.setBackgroundColor(this.mMentionedBgColor);
                break;
            case SELF:
                convertView.setBackgroundColor(this.mSelfBgColor);
                break;
            case NONE:
                break;
            default:
                break;
            }
        }

        if (StringHelper.isEmpty(s.inReplyToStatusId)) {
            holder.replyIcon.setVisibility(View.GONE);
        } else {
            holder.replyIcon.setVisibility(View.VISIBLE);
        }

        if (StringHelper.isEmpty(s.photoLargeUrl)) {
            holder.photoIcon.setVisibility(View.GONE);
        } else {
            holder.photoIcon.setVisibility(View.VISIBLE);
        }

        holder.nameText.setText(s.userScreenName);
        holder.contentText.setText(s.simpleText);
        holder.metaText.setText(getDateString(s.createdAt) + " 通过" + s.source);

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return StatusArrayAdapter.TYPES.length;
    }

    private void init(final Context context, final boolean colored) {
        this.colored = colored;
        if (colored) {
            this.mMentionedBgColor = OptionHelper.readInt(this.mContext,
                    R.string.option_color_highlight_mention, context
                            .getResources().getColor(R.color.mentioned_color));
            this.mSelfBgColor = OptionHelper.readInt(this.mContext,
                    R.string.option_color_highlight_self, context
                            .getResources().getColor(R.color.self_color));
            if (AppContext.DEBUG) {
                log("init mMentionedBgColor="
                        + Integer.toHexString(this.mMentionedBgColor));
                log("init mSelfBgColor="
                        + Integer.toHexString(this.mSelfBgColor));
            }
        }
    }

    void log(final String message) {
        Log.e(StatusArrayAdapter.TAG, message);
    }

    private void setTextStyle(final ViewHolder holder) {
        final int fontSize = getFontSize();
        holder.contentText.setTextSize(fontSize);
        holder.nameText.setTextSize(fontSize);
        holder.metaText.setTextSize(fontSize - 4);
        final TextPaint tp = holder.nameText.getPaint();
        tp.setFakeBoldText(true);
    }

    public void updateDataAndUI(final List<Status> ss) {
        this.mStatus = ss;
        notifyDataSetChanged();
    }

}
