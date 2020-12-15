package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.example.app.Sql.DBHelper;
import com.example.app.Tools.NetworkChangeReceiver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmittedActivity extends AppCompatActivity {
    private ListView listview_Submitted;
    private List<HashMap<String, Object>> list;
    private SimpleAdapter adapter;
    private int id;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter = new SimpleAdapter(SubmittedActivity.this, list, R.layout.listviewstyle_submitted,
                            new String[]{"purchasingtitle", "purchasingprice", "purchasingamount", "purchasingurl"},
                            new int[]{R.id.tv_purchasingtitle, R.id.tv_purchasingprice, R.id.tv_purchasingamount, R.id.tv_purchasingurl});
                    listview_Submitted.setAdapter(adapter);
                    break;
                case 2:
                    Toast.makeText(SubmittedActivity.this, "已复制链接", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submitted);
        listview_Submitted = findViewById(R.id.listview_Submitted);
        int netWorkStart = NetworkChangeReceiver.getNetWorkStart(this);
        if (netWorkStart == 1) {
        } else {//如果有网
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                    String UserPhoneNumber = sp.getString("phonenumber", "");
                    Object[] paObjects = new Object[]{UserPhoneNumber};
                    String sqlString = "select * from purchasing where  purchasingphonenumber=?";
                    list = DBHelper.getList(sqlString, paObjects);
                    Log.e("数据库", "查询成功");
                    handler.sendEmptyMessage(1);
                }
            }).start();
        }
        listview_Submitted.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {//长按监听事件
                final Map<String, Object> map = list.get(i);
                String purchasingurl = map.get("purchasingurl").toString();
                Log.e("TAG", "" + purchasingurl);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, purchasingurl);// 把数据复制到剪贴板
                clipboard.setPrimaryClip(clipData);
                handler.sendEmptyMessage(2);
                return true;
            }
        });
    }
}