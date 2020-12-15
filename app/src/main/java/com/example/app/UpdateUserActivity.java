package com.example.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.app.Sql.DBHelper;
import com.squareup.picasso.Picasso;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :更新个人信息页面
 * 版本: 1.0
 */
public class UpdateUserActivity extends AppCompatActivity implements View.OnClickListener {
    private RadioGroup radioGroup_Update_sex;//性别
    private RadioButton rb_Update_Boy, rb_Update_Girl;
    private Button button_Update_Cancel, button_Update_Submit, bt_update_choose;//取消，提交,选择图片
    private EditText edit_Update_Name, edit_Update_Age, edit_Update_Address;//输入的用户信息
    private ImageView iv_Update_getphoto;
    private ProgressBar br_Update_user;
    private String Username, UserPhoneNumber, Usersex, Userage, Useraddress, Userphoto = null;//查询到的数据
    private String UserUpdatename, UserUpdatesex, UserUpdateage, UserUpdateaddress, UserUpdatephoto;//更改的数据
    private File file;//文件路径
    private String filename = null;//图片名字
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    edit_Update_Name.setText(Username);
                    edit_Update_Age.setText(Userage);
                    if (Usersex.equals("男")) {
                        rb_Update_Boy.setChecked(true);
                    } else {
                        rb_Update_Girl.setChecked(true);
                    }
                    edit_Update_Address.setText(Useraddress);
                    Picasso.with(UpdateUserActivity.this).load(Userphoto).into(iv_Update_getphoto);//设置用户的头像
                    break;
                case 2:
                    Toast.makeText(UpdateUserActivity.this, "更新成功！", Toast.LENGTH_LONG).show();//跳转到主页
                    Intent intent = new Intent(UpdateUserActivity.this, indexActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        UserPhoneNumber = sp.getString("phonenumber", "");
        initView();
        initEvent();
        getUserData();
    }

    private void initView() {
        button_Update_Cancel = findViewById(R.id.button_Update_Cancel);
        button_Update_Submit = findViewById(R.id.button_Update_Submit);
        edit_Update_Name = findViewById(R.id.edit_Update_Name);
        radioGroup_Update_sex = findViewById(R.id.radioGroup_Update_sex);
        edit_Update_Age = findViewById(R.id.edit_Update_Age);
        edit_Update_Address = findViewById(R.id.edit_Update_Address);
        bt_update_choose = findViewById(R.id.bt_update_choose);
        iv_Update_getphoto = findViewById(R.id.iv_Update_getphoto);
        br_Update_user = findViewById(R.id.br_Update_user);
        rb_Update_Boy = findViewById(R.id.rb_Update_Boy);
        rb_Update_Girl = findViewById(R.id.rb_Update_Girl);
    }


    private void getUserData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String DBpass = null;
                String sqlString = "select * from user where phonenumber=?";
                Object[] paObjects = new Object[]{UserPhoneNumber};
                List<HashMap<String, Object>> newslist = DBHelper.getList(sqlString, paObjects);
                if (newslist != null) {
                    Log.e("数据库", "有数据");
                } else {
                    Log.e("数据库", "无数据");
                }
                for (HashMap row : newslist) {
                    Log.e("数据库", "数据:" + row.get("name").toString() + row.get("age").toString() + row.get("address").toString());
                    Username = row.get("name").toString();
                    Userage = row.get("age").toString();
                    Usersex = row.get("sex").toString();
                    Useraddress = row.get("address").toString();
                    Userphoto = row.get("photo").toString();
                    //防止用户只修改了一样东西
                    UserUpdatename = Username;
                    UserUpdateage = Userage;
                    UserUpdatesex = Usersex;
                    UserUpdateaddress = Useraddress;
                    UserUpdatephoto = Userphoto;
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    private void UpdateUserData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                UserUpdatename = edit_Update_Name.getText().toString();
                UserUpdateaddress = edit_Update_Address.getText().toString();
                UserUpdateage = edit_Update_Age.getText().toString();
                if (filename != null) {
                    Log.e("TAG", "文件名：" + filename);
                    UserUpdatephoto = "http://182.254.230.137:8080/admintes//images/" + filename;
                }
                String sqlString = "update user set name = ?,photo=?,sex=?,age=?,address=? where phonenumber = ?";
                Object[] paObjects = new Object[]{UserUpdatename, UserUpdatephoto, UserUpdatesex, UserUpdateage, UserUpdateaddress, UserPhoneNumber};
                if (DBHelper.Update(sqlString, paObjects)) {
                    handler.sendEmptyMessage(2);
                    Log.e("TAG", "更新成功！");
                } else {
                    Log.e("TAG", "更新失败！");
                }
            }
        }).start();
    }

    private void initEvent() {
        button_Update_Cancel.setOnClickListener(this);
        button_Update_Submit.setOnClickListener(this);
        bt_update_choose.setOnClickListener(this);
        radioGroup_Update_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (rb_Update_Boy.isChecked()) {
                    UserUpdatesex = "男";
                    Log.e("TAG", "" + UserUpdatesex);
                } else {
                    UserUpdatesex = "女";
                    Log.e("TAG", "" + UserUpdatesex);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_Update_Cancel:
                finish();
                break;
            case R.id.button_Update_Submit:
                if (filename != null) {//文件名不为空就上传图片
                    Log.e("TAG", "文件名：" + filename);
                    FileUpload();
                }
                UpdateUserData();
                break;
            case R.id.bt_update_choose:
                if (ContextCompat.checkSelfPermission(UpdateUserActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UpdateUserActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    initpermission();
                }
                break;
        }
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
                    Toast.makeText(UpdateUserActivity.this, "权限授取成功！", Toast.LENGTH_SHORT).show();
                    initpermission();
                } else {
                    Toast.makeText(UpdateUserActivity.this, "您未授取权限！", Toast.LENGTH_SHORT).show();
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
                iv_Update_getphoto.setImageURI(uri);//设置为选择的那张图片
            }
        }
    }

    /**
     * 全路径转换为file文件
     * 返回值为file类型
     *
     * @param uri
     * @return
     */
    private File uri2File(Uri uri) {
        String img_path;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = UpdateUserActivity.this.managedQuery(uri, proj, null,
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
        Log.e("TAG", "开始上传");
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
            br_Update_user.setProgress((int) (100 * progress));
            if (progress == 1.0) {
                handler.sendEmptyMessageDelayed(2, 1000);
            }
        }
    }
}
