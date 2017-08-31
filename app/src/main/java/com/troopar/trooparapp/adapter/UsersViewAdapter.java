package com.troopar.trooparapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.meg7.widget.CircleImageView;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsersViewAdapter extends BaseAdapter {

	private List<User> users;
	private LayoutInflater mInflater;
	private HashMap<String,Integer> locations;
	private LocalDBHelper localDBHelper;

	public UsersViewAdapter(Context context, ArrayList<User> users) {
		this.users = users;
		mInflater = LayoutInflater.from(context);
		locations=new HashMap<>();
	}

	public int getCount() {
		return users==null?0:users.size();
	}

	public Object getItem(int position) {
		return users==null?null:users.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		User entity = users.get(position);
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.user_item_view, null);
			viewHolder = new ViewHolder();
			viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
			viewHolder.userHead= (CircleImageView) convertView.findViewById(R.id.iv_userhead);
			viewHolder.unreadMessageCount= (TextView) convertView.findViewById(R.id.unreadMessageCount);
			viewHolder.latestMessage= (TextView) convertView.findViewById(R.id.tv_latestMessage);
			viewHolder.latestMessageTime= (TextView) convertView.findViewById(R.id.tv_latestMessageTime);
			convertView.setTag(viewHolder);
		} else {
			viewHolder= (ViewHolder) convertView.getTag();
		}
		String userId;
		switch (entity.getId()){
			case -3:
				userId=String.format("event/%s/%s",entity.getFirstName(),Constants.USERID);
				locations.put(String.format("event%s",entity.getFirstName()),position);
				viewHolder.tvUserName.setText(entity.getUserName());
				break;
			case -5:
				userId=String.format("group/%s/%s",entity.getFirstName(),Constants.USERID);
				locations.put(String.format("group%s",entity.getFirstName()),position);
				viewHolder.tvUserName.setText(entity.getUserName());
				break;
			default:
				userId=String.valueOf(entity.getId());
				locations.put(userId,position);
				viewHolder.tvUserName.setText(String.format("%s %s",entity.getFirstName(),entity.getLastName()));
				break;
		}
		if (Tools.isNullString(entity.getAvatarStandard())){
			viewHolder.userHead.setImageResource(R.drawable.user_image);
			viewHolder.userHead.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.appElementColor));
		}else{
			String url=entity.getAvatarStandard();
			if (url.startsWith("http")){
				Glide.with(convertView.getContext()).load(url).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.userHead);
			}else{
				Glide.with(convertView.getContext()).load(Uri.parse(String.format("file://%s",url))).override(70,70).into(viewHolder.userHead);
			}
		}
		if (entity.getUnreadMessageCount()>0){
			if (viewHolder.unreadMessageCount.getVisibility()==View.GONE){
				viewHolder.unreadMessageCount.setVisibility(View.VISIBLE);
			}
			viewHolder.unreadMessageCount.setText(String.valueOf(entity.getUnreadMessageCount()));
		}else{
			if (viewHolder.unreadMessageCount.getVisibility()==View.VISIBLE){
				viewHolder.unreadMessageCount.setVisibility(View.GONE);
			}
			viewHolder.unreadMessageCount.setText("0");
		}

		MessageModel messageModel=getLatestMessage(userId);
		if (messageModel!=null){
			switch (messageModel.getType()){
				case "chat":
					viewHolder.latestMessage.setText(messageModel.getContent());
					break;
				case "image":
					viewHolder.latestMessage.setText("[Image]");
					break;
				case "video":
					viewHolder.latestMessage.setText("[Video]");
					break;
				case "event":
					viewHolder.latestMessage.setText("Share a [Event]");
					break;
				case "change_group_name":
					viewHolder.latestMessage.setText("change group name to "+messageModel.getContent());
					break;
				case "audio":
					viewHolder.latestMessage.setText("[Audio]");
					break;
				default:viewHolder.latestMessage.setText(messageModel.getType());break;
			}
			viewHolder.latestMessageTime.setText(Tools.calculateTimeElapsed(messageModel.getCreatedTime()));
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView tvUserName;
		CircleImageView userHead;
		TextView unreadMessageCount;
		TextView latestMessage;
		TextView latestMessageTime;
	}

	public Integer getPositionWithId(String id){
		return locations.get(id);
	}

	public void setPositionWithId(String id,int position){
		locations.put(id,position);
	}

	public void clearUsers() {
		users=null;
		locations.clear();
		locations=null;
	}

	public void setLocalDBHelper(LocalDBHelper localDBHelper) {
		this.localDBHelper = localDBHelper;
	}

	private MessageModel getLatestMessage(String userId){
		long lastMessagePosition=localDBHelper.getMessageCountWithUser(Constants.USERID,userId);
		long limit=10;
		if (lastMessagePosition<limit){
			limit=lastMessagePosition;
			lastMessagePosition=0;
		}else{
			lastMessagePosition-=10;
		}
		ArrayList<MessageModel> messageModels=localDBHelper.getMessagesWithUser(Constants.USERID, userId, limit, lastMessagePosition);
		return messageModels.size()>0?messageModels.get(messageModels.size()-1):null;
	}


}
