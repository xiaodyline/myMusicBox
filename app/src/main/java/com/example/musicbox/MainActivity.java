package com.example.musicbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 获取界面中显示歌曲标题、作者文本框
    TextView title,author;
    // 播放/暂停、停止按钮
    ImageButton play,stop;
    // 上一首/下一首
    Button ptrack,ntrack;

    ActivityReceiver activityReceiver;

    public static final String  CTL_ACTION =
            "org.crazyit.action.CTL_ACTION";
    public static final String UPDATE_ACTION =
            "org.crazyit.action.UPDATE_ACTION";
    //定义音乐的播放状态，0x11 代表没有播放，0x12代表正在播放；0x13代表暂停
    int status = 0x11;
    String[] titleStrs = new String[] { "心愿", "约定", "美丽新世界" };
    String[] authorStrs = new String[] { "未知艺术家", "周蕙", "伍佰" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 获取 程序界面 中的 控件
        play = (ImageButton) this.findViewById(R.id.play);
        stop = (ImageButton) this.findViewById(R.id.stop);
        title = (TextView) findViewById(R.id.title);
        author = (TextView) findViewById(R.id.author);
        ptrack = (Button) findViewById(R.id.btnptrack);
        ntrack = (Button) findViewById(R.id.btnntrack);

        //为按钮 的单击事件 添加 监听器
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        ptrack.setOnClickListener(this);
        ntrack.setOnClickListener(this);

        activityReceiver = new ActivityReceiver();
        // 创建 IntentFiler
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver 监听的Action
        filter.addAction(UPDATE_ACTION);
        //注册BroadcastReceiver
        registerReceiver(activityReceiver,filter);

        // 启动后台服务
        Intent intent = new Intent( this,MusicService.class);
        startService(intent);
    }




    //自定义的BroadcastReceiver ,负责监听从Service传回来的广播
    public class ActivityReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取 Intent 中的update消息，update 代表播放状态
            int update = intent.getIntExtra("update",-1);
            // 获取 Intent 中的current消息，current代表当前正在播放的歌曲
            int current = intent.getIntExtra("current",-1);
            if(current >=0)
            {
                title.setText(titleStrs[current]);
                author.setText(authorStrs[current]);
            }
            switch (update)
            {
                //没有 播放状态
                case 0x11:
                    play.setImageResource(R.drawable.play);
                    status = 0x11;
                    title.setText("");
                    author.setText("");
                    break;
                //控制系统进入 播放状态
                case  0x12:
                    //播放状态下设置使用暂停图标
                    play.setImageResource(R.drawable.pause);
                    //设置当前状态
                    status = 0x12;
                    break;
                //控制系统进入暂停状态
                case 0x13:
                    //暂停状态下设置使用播放图标
                    play.setImageResource(R.drawable.play);
                    //设置当前状态
                    status = 0x13;
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        // 创建 Intent
        Intent intent = new Intent("org.crazyit.action.CTL_ACTION");
        switch (v.getId())
        {
            //按下播放/暂停按钮
            case R.id.play:
                intent.putExtra("control",1);
                break;
            // 按下 停止 按钮
            case R.id.stop:
                intent.putExtra("control",2);
                break;
            // 按下 上一首
            case R.id.btnptrack:
                intent.putExtra("control",3);
                break;
            // 按下 下一首
            case R.id.btnntrack:
                intent.putExtra("control",4);
                break;
        }
        // 发送广播 ， 将被Service 组件 中的BroadcastReceiver 接收到
        sendBroadcast(intent);
    }

}
