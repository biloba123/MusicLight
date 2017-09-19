package com.lvqingyang.musiclight;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.RecyclerView rvled;
    private android.support.design.widget.FloatingActionButton fabadditem;
    private FloatingActionButton fabsw;
    private FloatingActionButton fabsend;
    private List<LedItem> mLedItemList=new ArrayList<>();
    private SolidRVBaseAdapter mAdapter;
    private static final String TOPIC = "musicLight";
    private Gson mGson=new Gson();
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mqtt
        MqttService.start(this);

        MqttService.addMqttListener(new MqttListener() {
            @Override
            public void onConnected() {
                Toast.makeText(MainActivity.this, "连接成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail() {
                Toast.makeText(MainActivity.this, "连接失败，正在进行重连",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLost() {
                Toast.makeText(MainActivity.this, "连接丢失，正在进行重连",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRecieive(String message) {

            }

            @Override
            public void onSend() {
                Toast.makeText(MainActivity.this, "发送成功！",Toast.LENGTH_SHORT).show();
                mAdapter.clearAllItems();
            }
        });

        //init
        this.fabadditem = (FloatingActionButton) findViewById(R.id.fab_add_item);
        this.fabsend = (FloatingActionButton) findViewById(R.id.fab_send);
        this.fabsw = (FloatingActionButton) findViewById(R.id.fab_sw);
        this.rvled = (RecyclerView) findViewById(R.id.rv_led);

        fabsw.setTag(R.drawable.ic_play);

        //listener
        fabadditem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.addItem(new LedItem());
            }
        });

        fabsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabsw.getTag().equals(R.drawable.ic_play)) {//开
                    String msg=mGson.toJson(new Command(0,1,null,0));
                    MqttService.getMyMqtt().pubMsg(TOPIC,msg,0);
                    fabsw.setImageResource(R.drawable.ic_pause);
                    fabsw.setTag(R.drawable.ic_pause);
                }else if (fabsw.getTag().equals(R.drawable.ic_pause)) {//关
                    String msg=mGson.toJson(new Command(0,0,null,0));
                    MqttService.getMyMqtt().pubMsg(TOPIC,msg,0);
                    fabsw.setImageResource(R.drawable.ic_play);
                    fabsw.setTag(R.drawable.ic_play);
                }
            }
        });

        fabsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < rvled.getChildCount(); i++) {
                    LedItem ledItem=mLedItemList.get(i);
                    View itemView=rvled.getChildAt(i);

                    int num=0;
                    LinearLayout llLed=itemView.findViewById(R.id.ll_led);
                    int chlidCount=llLed.getChildCount();
                    for (int j = 0; j < chlidCount; j++) {
                        final ImageView iv = (ImageView) llLed.getChildAt(j);
                        if (iv.getTag().equals(R.drawable.ic_light_on)) {
                            num+=Math.pow(2,j);
                        }
                    }
                    ledItem.setNum(num);

                    final EditText etTime=itemView.findViewById(R.id.et_time);
                    String duration=etTime.getText().toString();
                    if (!TextUtils.isEmpty(duration)) {
                        ledItem.setDur(Float.parseFloat(duration));
                    }
                }

                for (LedItem ledItem : mLedItemList) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "onClick: "+ledItem.getNum()+" "+ledItem.getDur());
                }

                Command c=new Command(1,0,mLedItemList,mLedItemList.size());
                MqttService.getMyMqtt().pubMsg(TOPIC,mGson.toJson(c),0);
            }
        });


        //data
        mAdapter=new SolidRVBaseAdapter<LedItem>(this,mLedItemList) {
            @Override
            protected void onBindDataToView(SolidCommonViewHolder holder, final LedItem bean) {
                //iv led
                LinearLayout llLed=holder.getView(R.id.ll_led);
                for (int i = 0; i < llLed.getChildCount(); i++) {
                    final ImageView iv= (ImageView) llLed.getChildAt(i);
                    iv.setImageResource(R.drawable.ic_light_off);
                    iv.setTag(R.drawable.ic_light_off);
                    iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (iv.getTag().equals(R.drawable.ic_light_off)) {
                                    iv.setImageResource(R.drawable.ic_light_on);
                                    iv.setTag(R.drawable.ic_light_on);
                                }else if (iv.getTag().equals(R.drawable.ic_light_on)) {
                                    iv.setImageResource(R.drawable.ic_light_off);
                                    iv.setTag(R.drawable.ic_light_off);
                                }
                            }
                        });
                }

                //time
                final EditText etTime=holder.getView(R.id.et_time);
                etTime.setText(bean.getDur()+"");

                LinearLayout lltime=holder.getView(R.id.ll_time);
                for (int i = 0; i < lltime.getChildCount(); i++) {
                    lltime.getChildAt(i)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    TextView tv=(TextView)view;
                                    etTime.setText(tv.getText());
                                }
                            });
                }

                //close
                holder.getView(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAdapter.removeItem(bean);
                    }
                });
            }

            @Override
            public int getItemLayoutID(int viewType) {
                return R.layout.item_light;
            }
        };

        //set data
        rvled.setAdapter(mAdapter);
        rvled.setLayoutManager(new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqttService.stop(this);
    }
}
