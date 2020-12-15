package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.Sql.DBHelper;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyUpdateActivity extends AppCompatActivity implements View.OnClickListener {
    private int id;//订单id
    private ImageView iv_Buy_geturl;
    private TextView tv_Buy_Title, tv_Buy_Price, tv_Buy_Amount, tv_Buy_Courier;
    private EditText ed_Buy_Name, ed_Buy_Phonenumber, ed_Buy_Address;
    private Button button_Buy_Cancel, button_Buy_Submit, button_Buy_Receipts;
    private String userphonenumber, username, shoppingurl, shoppingtitle, shoppingamount, state, shoppingprice, useraddress;
    private String updateuserphonenumber, updateusername, updateuseraddress;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Picasso.with(BuyUpdateActivity.this).load(shoppingurl).into(iv_Buy_geturl);//设置图片Url
                    tv_Buy_Title.setText(shoppingtitle);
                    ed_Buy_Name.setText(username);
                    ed_Buy_Phonenumber.setText(userphonenumber);
                    ed_Buy_Address.setText(useraddress);
                    tv_Buy_Price.setText(shoppingprice);
                    tv_Buy_Amount.setText(shoppingamount);
                    if (state.equals("已收货")) {
                        tv_Buy_Courier.setText(state);
                        button_Buy_Submit.setEnabled(false);
                        button_Buy_Receipts.setEnabled(false);
                        ed_Buy_Phonenumber.setEnabled(false);
                        ed_Buy_Name.setEnabled(false);
                        ed_Buy_Address.setEnabled(false);
                    }else {
                        tv_Buy_Courier.setText(state);
                    }
                    break;
                case 2:
                    Toast.makeText(BuyUpdateActivity.this, "更改信息成功", Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case 3:
                    Toast.makeText(BuyUpdateActivity.this, "已经发货了，不能再修改了！", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(BuyUpdateActivity.this, "收货成功", Toast.LENGTH_LONG).show();
                    break;
                case 5:
                    Toast.makeText(BuyUpdateActivity.this, "还没发货，请耐心等待", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_update);
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 1);
        Log.e("id==", "" + id);
        initView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sqlString = "select * from usershopping where id=?";
                Object[] paObjects = new Object[]{id};
                List<HashMap<String, Object>> newslist = DBHelper.getList(sqlString, paObjects);
                if (newslist != null) {
//                    Log.e("数据库", "有数据");
                } else {
//                    Log.e("数据库", "无数据");
                }
                for (HashMap row : newslist) {
                    userphonenumber = row.get("updatephonenumber").toString().trim();
                    username = row.get("username").toString().trim();
                    shoppingurl = row.get("shoppingurl").toString().trim();
                    shoppingtitle = row.get("shoppingtitle").toString();
                    shoppingamount = row.get("shoppingamount").toString().trim();
                    shoppingprice = row.get("shoppingprice").toString().trim();
                    useraddress = row.get("useraddress").toString().trim();
                    state = row.get("state").toString().trim();
                    updateuserphonenumber = userphonenumber;
                    updateusername = username;
                    updateuseraddress = useraddress;
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    private void initView() {
        iv_Buy_geturl = findViewById(R.id.iv_Buy_geturl);
        tv_Buy_Title = findViewById(R.id.tv_Buy_Title);
        tv_Buy_Price = findViewById(R.id.tv_Buy_Price);
        tv_Buy_Amount = findViewById(R.id.tv_Buy_Amount);
        tv_Buy_Courier = findViewById(R.id.tv_Buy_Courier);
        ed_Buy_Name = findViewById(R.id.ed_Buy_Name);
        ed_Buy_Phonenumber = findViewById(R.id.ed_Buy_Phonenumber);
        ed_Buy_Address = findViewById(R.id.ed_Buy_Address);
        button_Buy_Cancel = findViewById(R.id.button_Buy_Cancel);
        button_Buy_Submit = findViewById(R.id.button_Buy_Submit);
        button_Buy_Receipts = findViewById(R.id.button_Buy_Receipts);
        button_Buy_Cancel.setOnClickListener(this);
        button_Buy_Submit.setOnClickListener(this);
        button_Buy_Receipts.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_Buy_Cancel:
                finish();
                break;
            case R.id.button_Buy_Submit:
                if (state.equals("未发货")) {
                    UpdateUserBuyData();//修改购买人信息
                } else if (state.equals("已收货")) {
                    button_Buy_Submit.setEnabled(false);
                    handler.sendEmptyMessage(3);
                } else {
                    handler.sendEmptyMessage(3);//已经发货了
                }
                break;
            case R.id.button_Buy_Receipts:
                if (state.equals("未发货")) {
                    handler.sendEmptyMessage(5);
                } else if (state.equals("已收货")) {
                    handler.sendEmptyMessage(3);
                } else {
                    final AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(BuyUpdateActivity.this);
                    normalDialog.setTitle("是否已经收货?");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String updatesql = "update usershopping set state='已收货' where id =  " + id;
                                    if (DBHelper.Update(updatesql, null)) {
                                        handler.sendEmptyMessage(4);
                                        Log.e("数据库", id + "----收货成功!");
                                    } else {
                                        Log.e("数据库", id + "----收货失败!");
                                    }

                                }
                            });
                    normalDialog.setNegativeButton("取消",
                            null);
                    // 显示
                    normalDialog.show();
                }
                break;
        }
    }

    private void UpdateUserBuyData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateuserphonenumber = ed_Buy_Phonenumber.getText().toString();
                updateusername = ed_Buy_Name.getText().toString();
                updateuseraddress = ed_Buy_Address.getText().toString();
                String sqlString = "update usershopping set updatephonenumber = ?,username=?,useraddress=? where id = ?";
                Object[] paObjects = new Object[]{updateuserphonenumber, updateusername, updateuseraddress, id};
                if (DBHelper.Update(sqlString, paObjects)) {
                    handler.sendEmptyMessage(2);
                    Log.e("TAG", "更新成功！");
                } else {
                    Log.e("TAG", "更新失败！");
                }
            }
        }).start();
    }
}