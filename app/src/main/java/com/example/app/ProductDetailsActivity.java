package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.app.Sql.DBHelper;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :商品详情
 * 版本: 1.0
 */
public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_Details_Url;
    private TextView tv_Details_Title, tv_Details_Price, tv_Details_Amount, tv_Details_count;
    private int id;
    private int Details_count = 1, Remaining_amount = 1;
    private String Url, title, amount, price, uploadsphonenumber;
    private Button bt_Details_Buy, bt_Details_add, bt_Details_subtract;
    private String userphonenumber, username, shoppingid, shoppingurl, shoppingtitle, shoppingprice, useraddress;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tv_Details_Title.setText("商品名称：" + title);
                    tv_Details_Amount.setText("仅剩" + amount + "件");
                    Remaining_amount = Integer.parseInt(amount);
                    tv_Details_Price.setText("¥" + price);
                    Picasso.with(ProductDetailsActivity.this).load(Url).into(iv_Details_Url);//设置图片Url
                    break;
                case 2:
                    Toast.makeText(ProductDetailsActivity.this, "购买成功", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(ProductDetailsActivity.this, "没货了，找其他商品购买吧", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(ProductDetailsActivity.this, "不能自己购买自己的商品！", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 1);
        Log.e("id==", "" + id);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            //NetworkOnMainThreadException类是从Android API 11开始增加的异常类（Android 3.0），从Android3.0开始网络访问的代码就不能写在主线程中了
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        initView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sqlString = "select * from commodity where id=?";
                Object[] paObjects = new Object[]{id};
                List<HashMap<String, Object>> newslist = DBHelper.getList(sqlString, paObjects);
                if (newslist != null) {
//                    Log.e("数据库", "有数据");
                } else {
//                    Log.e("数据库", "无数据");
                }
                for (HashMap row : newslist) {
                    Url = row.get("url").toString().trim();
                    title = row.get("title").toString().trim();
                    amount = row.get("amount").toString();
                    price = row.get("price").toString().trim();
                    uploadsphonenumber = row.get("uploadsphonenumber").toString().trim();
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    private void initView() {
        iv_Details_Url = findViewById(R.id.iv_Details_Url);
        tv_Details_Title = findViewById(R.id.tv_Details_Title);
        tv_Details_Amount = findViewById(R.id.tv_Details_Amount);
        tv_Details_Price = findViewById(R.id.tv_Details_Price);
        bt_Details_Buy = findViewById(R.id.bt_Details_Buy);
        tv_Details_count = findViewById(R.id.tv_Details_count);
        bt_Details_subtract = findViewById(R.id.bt_Details_subtract);
        bt_Details_add = findViewById(R.id.bt_Details_add);
        bt_Details_Buy.setOnClickListener(this);
        bt_Details_add.setOnClickListener(this);
        bt_Details_subtract.setOnClickListener(this);
    }

    private void getUserAddress() {
        String sqlString = "select * from user where phonenumber=?";
        Object[] paObjects = new Object[]{userphonenumber};
        List<HashMap<String, Object>> newslist = DBHelper.getList(sqlString, paObjects);
        if (newslist != null) {
//            Log.e("数据库", "有数据");
        } else {
//            Log.e("数据库", "无数据");
        }
        for (HashMap row : newslist) {
            useraddress = row.get("address").toString().trim();
            username = row.get("name").toString().trim();
        }
    }

    /**
     * 更新商品数量
     */
    private void updateAmount() {
        String sqlString = "update commodity set amount=amount-? where id =?";
        Object[] paObjects = new Object[]{Details_count, id};
        DBHelper.Update(sqlString, paObjects);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_Details_Buy:
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(ProductDetailsActivity.this);
                normalDialog.setTitle("确定购买?");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                                userphonenumber = sp.getString("phonenumber", "");
                                Log.e("TAg", "uploadsphonenumber=" + uploadsphonenumber + "--userphonenumber" + userphonenumber);
                                if (uploadsphonenumber.equals(userphonenumber)) {//不能自己购买自己的商品
                                    handler.sendEmptyMessage(4);
                                } else {
                                    if (Remaining_amount != 0) {//商品数量不为0
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                shoppingid = id + "";
                                                shoppingurl = Url;
                                                shoppingtitle = title;
                                                shoppingprice = price;
                                                getUserAddress();
                                                String sqlString = "insert into usershopping(userphonenumber,updatephonenumber,username,shoppingid,shoppingurl,shoppingtitle,shoppingamount,shoppingprice,useraddress,sellphonenumber) values(?,?,?,?,?,?,?,?,?,?)";
                                                Object[] paObjects = new Object[]{userphonenumber,userphonenumber, username, shoppingid, shoppingurl, shoppingtitle, Details_count, shoppingprice, useraddress, uploadsphonenumber};
                                                if (DBHelper.Update(sqlString, paObjects)) {
                                                    updateAmount();//更新数据库中商品的数量
                                                    handler.sendEmptyMessage(2);
                                                    Intent intent = new Intent(ProductDetailsActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    Log.e("TAG", "购买成功！");
                                                } else {
                                                    Log.e("TAG", "购买失败！");
                                                }
                                            }
                                        }).start();
                                    } else {
                                        handler.sendEmptyMessage(3);
                                    }
                                }
                            }
                        });
                normalDialog.setNegativeButton("取消",
                        null);
                // 显示
                normalDialog.show();

                break;
            case R.id.bt_Details_add:
                if (Details_count < Remaining_amount) {
                    Details_count++;
                }
                tv_Details_count.setText(Details_count + "");
                break;
            case R.id.bt_Details_subtract:
                if (Details_count >= 2) {
                    Details_count--;
                }
                tv_Details_count.setText(Details_count + "");
                break;
        }
    }
}