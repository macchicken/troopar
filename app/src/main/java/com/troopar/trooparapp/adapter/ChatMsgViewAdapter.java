package com.troopar.trooparapp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.adapter.viewholder.ChatMsgViewHolder;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;


public class ChatMsgViewAdapter extends BaseAdapter{

	private interface IMsgViewType {
		int IMVT_COM_MSG = 0;// 收到对方的消息
		int IMVT_COM_VIDEO = 3;// 收到对方的视频
		int IMVT_TO_MSG = 1;// 自己发送出去的消息
		int IMVT_TO_VIDEO = 2;// 自己发送出去的视频
		int GROUP_IMVT_COM_MSG = 5;
		int GROUP_IMVT_COM_VIDEO = 6;
		int GROUP_IMVT_TO_MSG = 7;
		int GROUP_IMVT_TO_VIDEO = 8;
		int IM_COM_EVENT_MSG = 9;
		int IM_TO_EVENT_MSG = 10;
	}

	private List<MessageModel> mColl;// 消息对象数组
	private LayoutInflater mInflater;
	private LocalDBHelper localDBHelper;
	private boolean isGroup=false;


	public ChatMsgViewAdapter(Context context, List<MessageModel> coll) {
		mColl = coll;
		mInflater = LayoutInflater.from(context);
		localDBHelper=LocalDBHelper.getInstance();
	}

	@Override
	public int getCount() {
		return mColl.size();
	}

	@Override
	public Object getItem(int position) {
		return mColl.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 得到Item的类型，是对方发过来的消息，还是自己发送出去的
	 */
	@Override
	public int getItemViewType(int position) {
		MessageModel entity = mColl.get(position);
		if (entity.getMsgType()) {//收到的消息
			if (entity.getType().equals("video")){
				if (isGroup){
					return IMsgViewType.GROUP_IMVT_COM_VIDEO;
				}
				return IMsgViewType.IMVT_COM_VIDEO;
			}
			if (entity.getType().equals("event")){
				return IMsgViewType.IM_COM_EVENT_MSG;
			}
			if (isGroup){
				return IMsgViewType.GROUP_IMVT_COM_MSG;
			}
			return IMsgViewType.IMVT_COM_MSG;
		} else {//自己发送的消息
			if (entity.getType().equals("video")){
				if (isGroup){
					return IMsgViewType.GROUP_IMVT_TO_VIDEO;
				}
				return IMsgViewType.IMVT_TO_VIDEO;
			}
			if (entity.getType().equals("event")){
				return IMsgViewType.IM_TO_EVENT_MSG;
			}
			if (isGroup){
				return IMsgViewType.GROUP_IMVT_TO_MSG;
			}
			return IMsgViewType.IMVT_TO_MSG;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 10;// 消息类型的总数
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MessageModel entity = mColl.get(position);
		boolean isComMsg = entity.getMsgType();
		String type=entity.getType();
		int typeCode;
		final ChatMsgViewHolder viewHolder;
		if (isComMsg) {
			if ("video".equals(type)){
				if (isGroup){
					typeCode=R.layout.group_chatting_item_msg_video_left;
				}else{
					typeCode=R.layout.chatting_item_msg_video_left;
				}
			}else if ("event".equals(type)){
				typeCode=R.layout.chatting_event_item_left;
			}else{
				if (isGroup){
					typeCode=R.layout.group_chatting_item_msg_text_left;
				}else{
					typeCode=R.layout.chatting_item_msg_text_left;
				}
			}
		}else{
			if ("video".equals(type)){
				if (isGroup){
					typeCode=R.layout.group_chatting_item_msg_video_right;
				}else{
					typeCode=R.layout.chatting_item_msg_video_right;
				}
			}else if ("event".equals(type)){
				typeCode=R.layout.chatting_event_item_right;
			}else{
				if (isGroup){
					typeCode=R.layout.group_chatting_item_msg_text_right;
				}else{
					typeCode=R.layout.chatting_item_msg_text_right;
				}
			}
		}
		if (convertView == null) {
			convertView = mInflater.inflate(typeCode, null);
			viewHolder = new ChatMsgViewHolder(convertView,type);
			convertView.setTag(typeCode,viewHolder);
		} else {
			viewHolder = (ChatMsgViewHolder) convertView.getTag(typeCode);
		}
		if (!isComMsg){
			String photoFileName= Constants.PHOTOFILENAME;
			if (!Tools.isNullString(photoFileName)){
				if (photoFileName.startsWith("http")){
					Glide.with(convertView.getContext()).load(photoFileName).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.getUserHead());
				}else{
					viewHolder.setUserHeadWithBitmap(loadImage(photoFileName));
				}
			}else{
				viewHolder.setUserHeadWithImageResource(R.drawable.user_image);
				viewHolder.setUserHearBackground(convertView.getContext().getResources().getColor(R.color.appElementColor));
			}
			if (isGroup){
				viewHolder.setUserName(String.format("%s %s",Constants.USERFIRSTNAME,Constants.USERLASTNAME));// display user name only in the group chat
			}
		}else{
			String from;
			User user;
			if (isGroup){
				from=entity.getFrom().split("/")[2];
				user=localDBHelper.getCacheUser(from);
				viewHolder.setUserName(String.format("%s %s",user.getFirstName(),user.getLastName()));// display user name only in the group chat
			}else{
				from=entity.getFrom();
				user=localDBHelper.getCacheUser(from);
			}
			if (user==null||Tools.isNullString(user.getAvatarStandard())){
				viewHolder.setUserHeadWithImageResource(R.drawable.user_image);
				viewHolder.setUserHearBackground(convertView.getContext().getResources().getColor(R.color.appElementColor));
			}else{
				Glide.with(convertView.getContext()).load(user.getAvatarStandard()).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.getUserHead());
			}
		}
		viewHolder.setTvSendTime(entity.getCreatedTime());
		switch (type){
			case "create_group":
				viewHolder.setTvContentWithCompoundDrawables(0, 0, 0, 0);
				viewHolder.setTvContent("I create a group, let's chat");
				viewHolder.setTvContentPosition(String.valueOf(position));
				break;
			case "change_group_name":
				viewHolder.setTvContentWithCompoundDrawables(0, 0, 0, 0);
				viewHolder.setTvContent(String.format("I change the group name to %s",entity.getContent()));
				viewHolder.setTvContentPosition(String.valueOf(position));
				break;
			case "group_quit":
				viewHolder.setTvContentWithCompoundDrawables(0, 0, 0, 0);
				String content=entity.getContent();
				String[] quitUserIds=content.substring(1,content.length()-1).split(",");
				StringBuilder stringBuilder=new StringBuilder();
				for (String userId:quitUserIds){
					User user =localDBHelper.getCacheUser(userId);
					if (user!=null){
						stringBuilder.append(user.getFirstName()).append(" ").append(user.getLastName()).append(",");
					}
				}
				viewHolder.setTvContent(String.format(" %s leave the group",stringBuilder.toString()));
				viewHolder.setTvContentPosition(String.valueOf(position));
				break;
			case "group_invite":
				viewHolder.setTvContentWithCompoundDrawables(0, 0, 0, 0);
				String content3=entity.getContent();
				String[] newUserIds=content3.substring(1,content3.length()-1).split(",");
				StringBuilder stringBuilder3=new StringBuilder();
				for (String userId:newUserIds){
					User user =localDBHelper.getCacheUser(userId);
					if (user!=null){
						stringBuilder3.append(user.getFirstName()).append(" ").append(user.getLastName()).append(",");
					}
				}
				viewHolder.setTvContent(String.format(" %s join the group",stringBuilder3.toString()));
				viewHolder.setTvContentPosition(String.valueOf(position));
				break;
			case "audio":
				viewHolder.setTvContent("");
				viewHolder.setTvContentWithCompoundDrawables(0,0,R.drawable.chatto_voice_playing,0);
				viewHolder.setTvContentPosition(String.valueOf(position));
				break;
			case "chat":
				viewHolder.setTvContentWithCompoundDrawables(0, 0, 0, 0);
				viewHolder.setTvContent(entity.getContent());
				viewHolder.setTvContentPosition(String.valueOf(position));
				break;
			case "event":
				try {
					System.out.println(entity.getId());
					JSONObject eventObject=new JSONObject(entity.getContent());
					viewHolder.setEventTitle(eventObject.getString("name"));
					viewHolder.setEventDescription(eventObject.getString("description"),eventObject.getString("id"));
					Glide.with(convertView.getContext()).load(eventObject.getString("smallImageUrl")).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.getEventImage());
					viewHolder.setTvContentPosition(String.valueOf(position));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case "image":
				try {
					JSONObject fileUrls=new JSONObject(entity.getContent());
					viewHolder.setTvContent("");
					final Resources resources=convertView.getResources();
					Glide.with(convertView.getContext()).load(fileUrls.getString("new")).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>(){
						@Override
						public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
							viewHolder.setTvContentWithCompoundDrawables(new BitmapDrawable(resources,resource), null, null, null);
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
				viewHolder.setTvContentPosition(String.valueOf(position));
				break;
			case "video":
				try {
					String thumbFileName = new JSONObject(entity.getContent()).getString("new");
					if (thumbFileName.startsWith("http")){
						final Resources resources=convertView.getResources();
						Glide.with(convertView.getContext()).load(thumbFileName).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {

							@Override
							public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
								viewHolder.setThumbnailImage(new BitmapDrawable(resources,resource));
							}
						});
					}else{// local file
						Drawable drawable = Drawable.createFromPath(thumbFileName);
						viewHolder.setThumbnailImage(drawable);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				viewHolder.setVideoViewContainerPosition(String.valueOf(position));
				break;
			default:
				break;
		}
		return convertView;
	}

	private Uri getFileUri(String photoFileName){
		return Uri.fromFile(new File(Tools.checkAppImageDirectory() + File.separator + photoFileName));
	}

	private Bitmap loadImage(String photoFileName){
		Log.d("ChatMsgViewAdapter", "load image " + photoFileName);
		String imagePath=getFileUri(photoFileName).getPath();
		BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
		sampleOptions.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(imagePath,sampleOptions);
		int inSampleSize=Tools.calculateInSampleSize(sampleOptions, 300, 300);
		sampleOptions.inJustDecodeBounds=false;
		sampleOptions.inSampleSize=inSampleSize;
		return BitmapFactory.decodeFile(imagePath, sampleOptions);
	}

	public void releaseResource(){
		mInflater=null;
	}

	public void setGroupFlag(boolean groupFlag) {
		isGroup = groupFlag;
	}


}
