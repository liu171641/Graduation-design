package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.app.Sql.DBHelper;
import com.example.app.Tools.NetworkChangeReceiver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :已上传的订单
 * 版本: 1.0
 */
public class UploadSuccessActivity extends AppCompatActivity {
    private ListView listview_upload_success;
    private int id;
    private List<HashMap<String, Object>> list;
    private SimpleAdapter adapter;
    private SwipeRefreshLayout mRefreshLayout;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter = new SimpleAdapter(UploadSuccessActivity.this, list, R.layout.listviewstyle,
                            new String[]{"url", "title", "price"},
                            new int[]{R.id.iv_Shopping_Icon, R.id.tv_Shopping_title, R.id.tv_Shopping_Price});
                    adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                        public boolean setViewValue(View view, Object data,
                                                    String textRepresentation) {
                            if (view instanceof ImageView) {
                                URL url = null;
                                try {
                                    url = new URL((String) data);
                                    ImageView iv = (ImageView) view;
                                    iv.setImageBitmap(BitmapFactory.decodeStream(url.openStream()));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            } else
                                return false;
                        }
                    });
                    listview_upload_success.setAdapter(adapter);
                    break;
                case 2:
                    String sqlString = "delete from commodity where id =  " + id;
                    if (DBHelper.Update(sqlString, null)) {
                        Log.e("数据库", id + "----删除成功!");
                    } else {
                        Log.e("数据库", id + "----删除失败!");
                    }
                    break;
                case 3:
                    //点击商品后跳转，把id转过去
                    Intent intent = new Intent(UploadSuccessActivity.this, CommodityUpdate.class);
                    Log.e("TAG", "" + id);
                    intent.putExtra("id", id);//把id传过去
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_success);
        listview_upload_success = findViewById(R.id.listview_upload_success);
        int netWorkStart = NetworkChangeReceiver.getNetWorkStart(this);
        if (netWorkStart == 1) {
        } else {//如果有网
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                    String UserPhoneNumber = sp.getString("phonenumber", "");
                    Log.e("TAG", "" + UserPhoneNumber);
                    Object[] paObjects = new Object[]{UserPhoneNumber};
                    String sqlString = "select * from commodity where  uploadsphonenumber=?";
                    list = DBHelper.getList(sqlString, paObjects);
                    if (list != null) {
                        handler.sendEmptyMessage(1);
                        Log.e("数据库", "查询成功");
                    } else {
                        Log.e("数据库", "无数据");
                    }
                }
            }).start();
        }
        listview_upload_success.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final Map<String, Object> map = list.get(i);
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(UploadSuccessActivity.this);
                normalDialog.setTitle("是否下架商品?");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                id = Integer.parseInt(map.get("id").toString());
                                list.remove(i);//list也要删除，不然不刷新
                                handler.sendEmptyMessage(2);
                                adapter.notifyDataSetChanged();//刷新适配器
                            }
                        });
                normalDialog.setNegativeButton("取消",
                        null);
                // 显示
                normalDialog.show();
                return true;
            }
        });
        listview_upload_success.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> map = list.get(i);
                id = Integer.parseInt(map.get("id").toString());
                handler.sendEmptyMessage(3);
            }
        });
    }
}