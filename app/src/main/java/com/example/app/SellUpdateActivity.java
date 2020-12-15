package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
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

public class SellUpdateActivity extends AppCompatActivity implements View.OnClickListener {
    private int id;//订单id
    private ImageView iv_Sell_geturl;
    private TextView tv_Sell_Title, tv_Sell_Name, tv_Sell_Phonenumber, tv_Sell_Address, tv_Sell_Price, tv_Sell_Amount;
    private EditText edit_Sell_Courier;
    private Button button_Sell_Cancel, button_Sell_Submit;
    private String userphonenumber, username, shoppingurl, shoppingtitle, shoppingamount, state, shoppingprice, useraddress;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Picasso.with(SellUpdateActivity.this).load(shoppingurl).into(iv_Sell_geturl);//设置图片Url
                    tv_Sell_Title.setText(shoppingtitle);
                    tv_Sell_Name.setText(username);
                    tv_Sell_Phonenumber.setText(userphonenumber);
                    tv_Sell_Address.setText(useraddress);
                    tv_Sell_Amount.setText(shoppingamount + "件");
                    tv_Sell_Price.setText("¥" + shoppingprice);
                    if (state.equals("已收货")) {
                        edit_Sell_Courier.setText("用户已收货");
                        edit_Sell_Courier.setEnabled(false);//禁止编辑
                        edit_Sell_Courier.setTextColor(Color.RED);
                        button_Sell_Submit.setText("用户已收货");
                        button_Sell_Submit.setEnabled(false);
                    }else if (state.equals("未发货")) {
                        edit_Sell_Courier.setText("");
                    } else {
                        edit_Sell_Courier.setText(state);
                    }
                    break;
                case 2:
                    Toast.makeText(SellUpdateActivity.this, "请输入正确的订单编号！", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(SellUpdateActivity.this, "更新快递编号成功！", Toast.LENGTH_LONG).show();

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_update);
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
                    Log.e("数据库", "有数据");
                } else {
                    Log.e("数据库", "无数据");
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
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    private void initView() {
        iv_Sell_geturl = findViewById(R.id.iv_Sell_geturl);
        tv_Sell_Title = findViewById(R.id.tv_Sell_Title);
        tv_Sell_Name = findViewById(R.id.tv_Sell_Name);
        tv_Sell_Phonenumber = findViewById(R.id.tv_Sell_Phonenumber);
        tv_Sell_Address = findViewById(R.id.tv_Sell_Address);
        tv_Sell_Amount = findViewById(R.id.tv_Sell_Amount);
        tv_Sell_Price = findViewById(R.id.tv_Sell_Price);
        edit_Sell_Courier = findViewById(R.id.edit_Sell_Courier);
        button_Sell_Cancel = findViewById(R.id.button_Sell_Cancel);
        button_Sell_Submit = findViewById(R.id.button_Sell_Submit);
        button_Sell_Cancel.setOnClickListener(this);
        button_Sell_Submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_Sell_Cancel:
                finish();
                break;
            case R.id.button_Sell_Submit:
                state = edit_Sell_Courier.getText().toString();
                if (state.length() >= 5) {
                    String sql = "update usershopping set state=? where id = ?";
                    Object[] paObjects = new Object[]{state, id};
                    if (DBHelper.Update(sql, paObjects)) {
                        handler.sendEmptyMessage(3);
                        finish();
                    } else {
                        Log.e("数据库", "更新商品状态失败");
                    }

                } else {
                    handler.sendEmptyMessage(2);
                }
                break;
        }
    }
}