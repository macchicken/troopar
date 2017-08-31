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
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class DisplayUsersViewAdapter extends BaseAdapter {

	private List<User> users;
	private LayoutInflater mInflater;


	public DisplayUsersViewAdapter(Context context, ArrayList<User> users) {
		this.users = users;
		mInflater = LayoutInflater.from(context);
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
			convertView = mInflater.inflate(R.layout.display_user_item_view, null);
			viewHolder = new ViewHolder();
			viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
			viewHolder.userHead= (CircleImageView) convertView.findViewById(R.id.iv_userhead);
			convertView.setTag(viewHolder);
		} else {
			viewHolder= (ViewHolder) convertView.getTag();
		}
		switch (entity.getId()){
			case -3:
				viewHolder.tvUserName.setText(entity.getUserName());
				break;
			case -5:
				viewHolder.tvUserName.setText(entity.getUserName());
				break;
			default:
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
				Glide.with(convertView.getContext()).load(Uri.parse(String.format("file://%s",url))).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.userHead);
			}
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView tvUserName;
		CircleImageView userHead;
	}

	public void clearUsers() {
		users=null;
	}


}
