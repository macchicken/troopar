package com.troopar.trooparapp.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtil {

	private Context mCtx;

	private static class FileUtilHolder{
		private static final FileUtil my=new FileUtil();
	}

	private FileUtil(){
		System.out.println("FileUtil create");
	}

	public static FileUtil getInstance(){
		return FileUtilHolder.my;
	}

	public String getAbsolutePath(){
		File root = mCtx.getExternalFilesDir(null);
		String absPath = null;
		if(root != null){
			absPath=root.getAbsolutePath();
		}
		return absPath;
	}

	public String containBitmap(String name){
		File root = mCtx.getExternalFilesDir(null);
		File file = new File(root,name);
		return file.exists()?file.getAbsolutePath():null;
	}

	public void saveBitmap(String name,Bitmap bitmap){
		if(bitmap == null){
			return;
		}
		String bitPath = getAbsolutePath()+"/"+name;
		try {
			FileOutputStream fos = new FileOutputStream(bitPath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setMCtx(Context mCtx) {
		FileUtilHolder.my.mCtx = mCtx;
	}


}
