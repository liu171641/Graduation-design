package com.example.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.app.Sql.DBHelper;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;
import okhttp3.Call;
import okhttp3.Request;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :注册页面
 * 版本: 1.0
 */

public class RegisterActivity extends AppCompatActivity {
    private RadioGroup sex;//性别
    private Button cancel, submit, bt_register_choose;//取消，提交
    private ImageView iv_register_getphoto;//头像
    private EditText name, password, age, address;//输入框的用户信息
    private String Username, Userphonenumber, Userpassword, Usersex, Userage, Useraddress = null;
    private File file;//文件路径
    private String filename;//图片名字
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(RegisterActivity.this, "请输入完整的注册信息,密码不少于8位", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sendCode(this);
        initView();
        initEvent();
    }

    private void initView() {
        cancel = findViewById(R.id.button_cancel);
        submit = findViewById(R.id.button_submit);
        name = findViewById(R.id.edit_name);
        sex = findViewById(R.id.radioGroup_sex);
        password = findViewById(R.id.edit_password);
        age = findViewById(R.id.edit_age);
        address = findViewById(R.id.tv_address);
        bt_register_choose = findViewById(R.id.bt_register_choose);
        iv_register_getphoto = findViewById(R.id.iv_register_getphoto);
    }


    /**
     * 保存用户数据到数据库
     */
    private void SubmitData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Username = name.getText().toString();
                Userpassword = password.getText().toString();
                Userage = age.getText().toString();
                Useraddress = address.getText().toString();
                String url = "http://182.254.230.137:8080/admintes//images/" + filename;
                if (Userpassword.length() >= 8 && Username.length() != 0 && age.length() != 0 && Useraddress.length() != 0 && Userphonenumber.length() != 0) {
                    FileUpload();
                    String sqlString = "insert into user(name,photo,phonenumber,password,sex,age,address) values(?,?,?,?,?,?,?)";
                    Object[] paObjects = new Object[]{Username, url, Userphonenumber, Userpassword, Usersex, Userage, Useraddress};
                    if (DBHelper.Update(sqlString, paObjects)) {
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        handler.sendEmptyMessage(2);
                        Log.e("TAG", "添加成功！");
                    } else {
                        Log.e("TAG", "添加失败！");
                    }
                } else {
                    handler.sendEmptyMessage(1);

                }
            }
        }).start();
    }

    private void initEvent() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubmitData();
            }
        });
        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButtonsex = findViewById(radioGroup.getCheckedRadioButtonId());
                Usersex = radioButtonsex.getText().toString();
//                Toast.makeText(RegisterActivity.this, String.format("你选择了%s", radioButtonsex.getText().toString()), Toast.LENGTH_SHORT).show();
            }
        });
        bt_register_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    initpermission();
                }
            }
        });

    }

    private void initpermission() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 2);
    }

    /**
     * @param requestCode  请求码
     * @param permissions  权限名
     * @param grantResults 请求结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//ERMISSION_GRANTED为已授取
                    Toast.makeText(RegisterActivity.this, "权限授取成功！", Toast.LENGTH_SHORT).show();
                    initpermission();
                } else {
                    Toast.makeText(RegisterActivity.this, "您未授取权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    /**
     * 返回相册选择后的图片
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                file = uri2File(uri);//全路径转path
                String fl = file + "";
                String[] strs = fl.split("/");//以/切割
                filename = strs[strs.length - 1];//获得切割后的最后一位
                iv_register_getphoto.setImageURI(uri);//设置为选择的那张图片
            }
        }
    }

    /**
     * user转换为file文件
     * 返回值为file类型
     *
     * @param uri
     * @return
     */
    private File uri2File(Uri uri) {
        String img_path;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = RegisterActivity.this.managedQuery(uri, proj, null,
                null, null);
        if (actualimagecursor == null) {
            img_path = uri.getPath();
        } else {
            int actual_image_column_index = actualimagecursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            img_path = actualimagecursor
                    .getString(actual_image_column_index);
        }
        File file = new File(img_path);
        return file;
    }


    /**
     * 使用okhttp-utils上传多个或者单个文件
     */
    public void FileUpload() {
        String Url = "http://182.254.230.137:8080/admintes/FileUploadServlet";//服务器Url
        OkHttpUtils.post()//
                .addFile("mFile", filename, file)//
                .url(Url)
                .build()//
                .execute(new MyStringCallback());
    }

    /**
     * 回调
     */
    public class MyStringCallback extends StringCallback {
        @Override
        public void onBefore(Request request, int id) {
        }

        @Override
        public void onAfter(int id) {
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        /**
         * 得到数据
         *
         * @param response
         * @param id
         */
        @Override
        public void onResponse(String response, int id) {
            Log.e("TAG", "上传成功");
        }

        /**
         * 上传进度
         *
         * @param progress
         * @param total
         * @param id
         */
        @Override
        public void inProgress(float progress, long total, int id) {
            Log.e("TAG", "当前进度:" + progress);
//            mProgressBar.setProgress((int) (100 * progress));
        }
    }

    /**
     * 发送验证码
     *
     * @param context
     */
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
                    Userphonenumber = (String) phoneMap.get("phone");
                    SMSSDK.getVerificationCode(country, Userphonenumber);
                    // TODO 利用国家代码和手机号码进行后续的操作
                } else {
                    // TODO 处理错误的结果
                }
            }
        });
        page.show(context);
    }
}
