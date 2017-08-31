package com.troopar.trooparapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.myview.MovieRecorderView;

public class RecordMovieActivity extends Activity {

    private MovieRecorderView mRecorderView;
    private boolean isFinish;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RecordMovieActivity","RecordMovieActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_movie);
        mRecorderView = (MovieRecorderView) findViewById(R.id.movieRecorderView);
        Button mShootBtn = (Button) findViewById(R.id.shoot_button);
        mRecorderView.setOnRecordFinishListener(new MovieRecorderView.OnRecordFinishListener() {
            @Override
            public void onRecordFinish() {
                isFinish=true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent data=new Intent();
                        data.putExtra("recordFile",mRecorderView.getMVecordFile().getAbsolutePath());
                        setResult(RESULT_OK, data);
                        onBackPressed();
                    }
                });
            }
        });
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (isFinish){
                    Log.d("RecordMovieActivity","RecordMovieActivity onLongPress");
                    isFinish=false;
                    mRecorderView.record();
                }
            }
        });
        mShootBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               if (event.getAction() == MotionEvent.ACTION_UP) {// release to finish recording
                    if (mRecorderView.getTimeCount() > 1){
                        if (!isFinish) {
                            Log.d("RecordMovieActivity","RecordMovieActivity ACTION_UP");
                            isFinish=true;
                            mRecorderView.stop();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent data=new Intent();
                                    data.putExtra("recordFile",mRecorderView.getMVecordFile().getAbsolutePath());
                                    setResult(RESULT_OK, data);
                                    onBackPressed();
                                }
                            });
                        }
                   }else {
                        isFinish=true;
                        mRecorderView.stop();
                        Toast.makeText(RecordMovieActivity.this, "Time too short", Toast.LENGTH_SHORT).show();
                   }
                   return true;
                }
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        Log.d("RecordMovieActivity","RecordMovieActivity onResume");
        super.onResume();
        isFinish = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("RecordMovieActivity","RecordMovieActivity onSaveInstanceState");
        isFinish = true;
        mRecorderView.stop();
    }

    @Override
    public void onPause() {
        Log.d("RecordMovieActivity","RecordMovieActivity onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d("RecordMovieActivity","RecordMovieActivity onDestroy");
        super.onDestroy();
    }

//    /**
//     * 录制完成回调
//     *
//     * @author liuyinjun
//     *
//     * @date 2015-2-9
//     */
//    public interface OnShootCompletionListener {
//        public void OnShootSuccess(String path, int second);
//        public void OnShootFailure();
//    }
}
