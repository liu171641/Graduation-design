package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.app.Sql.DBHelper;

import java.util.HashMap;
import java.util.List;

public class PurchasingActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edit_Purchasing_title, edit_Purchasing_amount, edit_Purchasing_price;
    private Button bt_purchasing_cancel, bt_purchasing_submit, bt_purchasing_Buy;
    private ImageView iv_purchasing_back;
    private String purchasingphonenumber, purchasingaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchasing);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                purchasingphonenumber = sp.getString("phonenumber", "");
                String sqlString = "select * from user where phonenumber=?";
                Object[] paObjects = new Object[]{purchasingphonenumber};
                List<HashMap<String, Object>> newslist = DBHelper.getList(sqlString, paObjects);
                if (newslist != null) {
                    Log.e("数据库", "有数据");
                } else {
                    Log.e("数据库", "无数据");
                }
                for (HashMap row : newslist) {
                    purchasingaddress = row.get("address").toString().trim();
                }
            }
        }).start();
        initview();
    }

    private void initview() {
        edit_Purchasing_title = findViewById(R.id.edit_Purchasing_title);
        edit_Purchasing_amount = findViewById(R.id.edit_Purchasing_amount);
        edit_Purchasing_price = findViewById(R.id.edit_Purchasing_price);
        bt_purchasing_cancel = findViewById(R.id.bt_purchasing_cancel);
        bt_purchasing_submit = findViewById(R.id.bt_purchasing_submit);
        iv_purchasing_back = findViewById(R.id.iv_purchasing_back);
        bt_purchasing_Buy = findViewById(R.id.bt_purchasing_Buy);
        bt_purchasing_cancel.setOnClickListener(this);
        bt_purchasing_submit.setOnClickListener(this);
        bt_purchasing_Buy.setOnClickListener(this);
        iv_purchasing_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_purchasing_cancel:
            case R.id.iv_purchasing_back:
                finish();
                break;
            case R.id.bt_purchasing_submit:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String purchasingtitle = edit_Purchasing_title.getText().toString();
                        String purchasingamount = edit_Purchasing_amount.getText().toString();
                        String purchasingprice = edit_Purchasing_price.getText().toString();
                        Log.e("TAg", "" + purchasingtitle + purchasingamount + purchasingprice + purchasingphonenumber + purchasingaddress);
                        if (purchasingtitle.length() != 0 && purchasingamount.length() != 0 && purchasingprice.length() != 0) {
                            String sqlString = "insert into purchasing(purchasingtitle,purchasingamount,purchasingprice,purchasingphonenumber,purchasingaddress) values(?,?,?,?,?)";
                            Object[] paObjects = new Object[]{purchasingtitle, purchasingamount, purchasingprice, purchasingphonenumber, purchasingaddress};
                            if (DBHelper.Update(sqlString, paObjects)) {
                                Intent intent = new Intent(PurchasingActivity.this, SubmittedActivity.class);
                                startActivity(intent);
                            } else {
                                Log.e("TAG", "添加失败！");
                            }
                        } else {
                            Log.e("TAg", "请输入剩余的信息");
                        }
                    }
                }).start();
                break;
            case R.id.bt_purchasing_Buy:
                Intent intent = new Intent(PurchasingActivity.this, SubmittedActivity.class);
                startActivity(intent);
                break;

        }
    }
}