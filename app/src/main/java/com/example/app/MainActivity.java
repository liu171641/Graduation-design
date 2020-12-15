package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.Sql.DBHelper;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private long mExitTime;

    /**
     * 点击两次返回退出app
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                handler.sendEmptyMessage(4);
                //System.currentTimeMillis()系统当前时间
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private EditText ed_PhoneNumber, ed_Password;
    private Button bt_Login;
    private TextView tv_Register, tv_FindPass;
    private String UserPhoneNumber, UserPassWord;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(MainActivity.this, "请输入正确的账号和密码", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "请检查手机号码是否正确！", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(MainActivity.this, "账号和密码正确，正在为你跳转至主页", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(MainActivity.this, "再按一次退出APP", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        String isAutoLogin = sp.getString("phonenumber", null);
        if (isAutoLogin != null) {
            Intent intent = new Intent(MainActivity.this, indexActivity.class);
            startActivity(intent);
            this.finish();
        }
        initView();
    }

    private void initView() {
        ed_PhoneNumber = findViewById(R.id.et_PhonenNmber);
        ed_Password = findViewById(R.id.et_Password);
        bt_Login = findViewById(R.id.bt_Login);
        tv_Register = findViewById(R.id.tv_Register);
        tv_FindPass = findViewById(R.id.tv_FindPass);
        bt_Login.setOnClickListener(this);
        tv_Register.setOnClickListener(this);
        tv_FindPass.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_Login:
                Passwordverification();
                break;
            case R.id.tv_Register:
                Intent intentRegisterActivity = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intentRegisterActivity);
                break;
            case R.id.tv_FindPass:
                Intent intent = new Intent(MainActivity.this, FindPassActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 输入的账号密码和数据库的密码比对
     */
    private void Passwordverification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserPhoneNumber = ed_PhoneNumber.getText().toString();
                UserPassWord = ed_Password.getText().toString();
                if (UserPhoneNumber.length() != 11) {
                    handler.sendEmptyMessage(2);//如果长度不为11位
                } else {
                    String DBpass = null;
                    String sqlString = "select password from user where phonenumber=?";
                    Object[] paObjects = new Object[]{UserPhoneNumber};
                    List<HashMap<String, Object>> newslist = DBHelper.getList(sqlString, paObjects);
                    if (newslist.size()!=0) {//判断是否存在该手机号码
                        Log.e("数据库", "有数据");
                        for (HashMap row : newslist) {
                            DBpass = row.get("password").toString().trim();
                            Log.e("数据库", "数据库查询的密码：" + DBpass);
                        }
                        if (DBpass.equals(UserPassWord) && DBpass != null) {
                            SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("phonenumber", UserPhoneNumber);//存入本地文件，
                            editor.commit();
                            handler.sendEmptyMessage(3);//账号密码正确
                            Intent intent = new Intent(MainActivity.this, indexActivity.class);
                            startActivity(intent);
                        } else {
                            Log.e("数据库", "密码错误");
                            handler.sendEmptyMessage(1);//账号密码错误
                        }
                    } else {
                        Log.e("数据库", "无数据");
                        handler.sendEmptyMessage(1);//账号密码错误
                    }
                }
            }
        }).start();
    }
}