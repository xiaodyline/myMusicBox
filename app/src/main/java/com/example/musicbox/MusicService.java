package com.example.musicbox;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service {

    MyReceiver serviceRecevicer;
    AssetManager assetManager;
    String[] musics = new String[] { "wish.mp3", "promise.mp3",
            "beautiful.mp3" };
    MediaPlayer mediaPlayer;
    // 当前的状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
    int status = 0x11;
    // 记录当前正在播放的音乐
    int current = 0;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        assetManager = getAssets();
        // 创建BroadcastReceiver
        serviceRecevicer = new MyReceiver();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);
        registerReceiver(serviceRecevicer,filter);
        //创建 MediaPlayer
        mediaPlayer = new MediaPlayer();
        // 为MediaPlayer 播放 完成事件绑定监听器
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                current = (current + 1)% musics.length;

                //发送广播 通知 Activity 更改文本框
                Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                sendIntent.putExtra("current",current);
                // 发送广，将被Activity组件中的BroadcastReceiver 接收到
                sendBroadcast(sendIntent);
                //准备并播放音乐
                prepareAndPlay(musics[current]);
            }
        });
    }

    public class MyReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control",-1);
            switch (control)
            {
                //播放或暂停
                case 1:
                    //原来处于没有播放状态
                    if(status == 0x11)
                    {
                        // 准备 并播放 音乐
                        prepareAndPlay(musics[current]);
                        status = 0x12;
                    }
                    //原来处于播放状态
                    else if (status == 0x12)
                    {
                        //暂停
                        mediaPlayer.pause();
                        //改为暂停状态
                        status = 0x13;
                    }
                    // 原来 处于 暂停状态
                    else if (status == 0x13)
                    {
                        //播放
                        mediaPlayer.start();
                        // 改变状态
                        status = 0x12;
                    }
                    break;
                //停止 播放
                case 2:
                    //如果原来正在播放或暂停
                    if (status == 0x12 || status == 0x13)
                    {
                        // 停止播放
                        mediaPlayer.stop();
                        status = 0x11;
                    }
                    break;
                // 上一首
                case 3:
                    if (status == 0x12 ||status == 0x13)
                    {
                        current = ((current - 1)+ musics.length) % musics.length;
                        prepareAndPlay(musics[current]);
                        status = 0x12;
                    }
                    break;
                // 下一首
                case 4:
                    if (status == 0x12 ||status == 0x13)
                    {
                        current = (current + 1) % musics.length;
                        prepareAndPlay(musics[current]);
                        status = 0x12;
                    }
                    break;
            }
            // 广播通知 Activity 更改图标、文本框
            Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
            sendIntent.putExtra("update",status);
            sendIntent.putExtra("current",current);
            //发送广播,将被Activity组件中的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        }
    }

    private void prepareAndPlay(String music){
        // 打开指定音乐文件
        try {
            AssetFileDescriptor afd = assetManager.openFd(music);
            mediaPlayer.reset();
            // 使用MediaPlayer加载指定的声音文件。
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            // 准备声音
            mediaPlayer.prepare();
            //播放
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
