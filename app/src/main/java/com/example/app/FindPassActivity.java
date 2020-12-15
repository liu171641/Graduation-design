package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.Sql.DBHelper;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :找回密码
 * 版本: 1.0
 */
public class FindPassActivity extends AppCompatActivity {
    private String phone;
    private EditText ed_Find_Pass_NewPass;
    private ImageView iv_Find_Pass_Submit;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(FindPassActivity.this, "更新成功！为您跳转至主页面", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(FindPassActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case 2:
                    Toast.makeText(FindPassActivity.this, "更新失败！", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(FindPassActivity.this, "密码少于8位", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pass);
        sendCode(this);
        ed_Find_Pass_NewPass = findViewById(R.id.ed_Find_Pass_NewPass);
        iv_Find_Pass_Submit = findViewById(R.id.iv_Find_Pass_Submit);
        iv_Find_Pass_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String NewPassword = ed_Find_Pass_NewPass.getText().toString();
                        if (NewPassword.length() >= 8) {
                            String sqlString = "update user set password = ? where phonenumber = ?";
                            Object[] paObjects = new Object[]{NewPassword, phone};
                            if (DBHelper.Update(sqlString, paObjects)) {
                                handler.sendEmptyMessage(1);
                            } else {
                                handler.sendEmptyMessage(2);
                            }
                        } else {
                            handler.sendEmptyMessage(3);
                        }
                    }
                }).start();
            }
        });
    }

    public void sendCode(Context context) {
        RegisterPage page = new RegisterPage();
        //如果使用我们的ui，没有申请模板编号的情况下需传null
        page.setTempCode(null);
        page.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 处理成功的结果
                    HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                    // 国家代码，如“86”
                    String country = (String) phoneMap.get("country");
                    // 手机号码，如“13800138000”
                    phone = (String) phoneMap.get("phone");
                    SMSSDK.getVerificationCode(country, phone);
                    // TODO 利用国家代码和手机号码进行后续的操作
                } else {
                    // TODO 处理错误的结果
                }
            }
        });
        page.show(context);
    }
}

