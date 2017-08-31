package com.troopar.trooparapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.meg7.widget.CircleImageView;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsersCheckBoxViewAdapter extends BaseAdapter {

	private List<User> users;
	private LayoutInflater mInflater;
	private HashMap inGroupUsers;


	public UsersCheckBoxViewAdapter(Context context, ArrayList<User> users, HashMap inGroupUsers) {
		this.users = users;
		this.inGroupUsers = inGroupUsers;
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
			convertView = mInflater.inflate(R.layout.user_item_checkbox_view, null);
			viewHolder = new ViewHolder();
			viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);
			viewHolder.userHead= (CircleImageView) convertView.findViewById(R.id.iv_userhead);
			viewHolder.checkUser= (CheckBox) convertView.findViewById(R.id.checkUser);
			convertView.setTag(viewHolder);
		} else {
			viewHolder= (ViewHolder) convertView.getTag();
		}
		viewHolder.checkUser.setContentDescription(String.valueOf(position));
		if (inGroupUsers!=null&&inGroupUsers.containsKey(entity.getId())){
			viewHolder.checkUser.setChecked(true);
			viewHolder.checkUser.setEnabled(false);
		}
		viewHolder.tvUserName.setText(String.format("%s %s",entity.getFirstName(),entity.getLastName()));
		if (Tools.isNullString(entity.getAvatarStandard())){
			viewHolder.userHead.setImageResource(R.drawable.user_image);
			viewHolder.userHead.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.appElementColor));
		}else{
			Glide.with(convertView.getContext()).load(entity.getAvatarStandard()).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.userHead);
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView tvUserName;
		CircleImageView userHead;
		CheckBox checkUser;
	}


}
