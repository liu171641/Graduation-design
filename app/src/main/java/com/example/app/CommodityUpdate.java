package com.example.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

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
 * 日期  : 2020/11/30 15:39
 * 内容   :更新商品信息
 * 版本: 1.0
 */
public class CommodityUpdate extends AppCompatActivity implements View.OnClickListener {

    private Button bt_choose_Commo, bt_Commo_Cancel, bt_Commo_Update;
    private ImageView iv_Commo_getphoto;
    private EditText ed_Commo_title, ed_Commo_price, ed_Commo_amount;
    private int id;
    private String Url, category, title, amount, price;
    private String UpdateUrl, Updatecategory, Updatetitle, Updateamount, Updateprice, Updatephoto;
    private ProgressBar br_Commo;
    private Spinner sp_Commo_category;
    private File file;//文件路径
    private String filename = null;//图片名字
    private static final String[] m = {"手机", "电脑", "显示器", "相机", "图书", "食品", "饮料", "冰箱", "空调", "电视", "热水器", "洗衣机", "电饭煲", "其他"};
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ed_Commo_title.setText(title);
                    ed_Commo_amount.setText(amount);
                    ed_Commo_price.setText(price);
                    int equals = StringByEquals(m, category);
                    sp_Commo_category.setSelection(equals);
                    Picasso.with(CommodityUpdate.this).load(Url).into(iv_Commo_getphoto);//设置图片Url
                    break;
                case 2:
                    Toast.makeText(CommodityUpdate.this, "更新成功！", Toast.LENGTH_LONG).show();//跳转到主页
                    Intent intent = new Intent(CommodityUpdate.this, UploadSuccessActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_update);
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
                    category = row.get("category").toString().trim();
                    title = row.get("title").toString().trim();
                    amount = row.get("amount").toString();
                    price = row.get("price").toString().trim();
                    UpdateUrl = Url;
                    Updatetitle = title;
                    Updateamount = amount;
                    Updateprice = price;
                    Updatephoto = Url;
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    private void initView() {
        bt_choose_Commo = findViewById(R.id.bt_choose_Commo);
        bt_Commo_Cancel = findViewById(R.id.bt_Commo_Cancel);
        bt_Commo_Update = findViewById(R.id.bt_Commo_Update);
        iv_Commo_getphoto = findViewById(R.id.iv_Commo_getphoto);
        ed_Commo_title = findViewById(R.id.ed_Commo_title);
        ed_Commo_price = findViewById(R.id.ed_Commo_price);
        ed_Commo_amount = findViewById(R.id.ed_Commo_amount);
        br_Commo = findViewById(R.id.br_Commo);
        sp_Commo_category = findViewById(R.id.sp_Commo_category);
        bt_choose_Commo.setOnClickListener(this);
        bt_Commo_Cancel.setOnClickListener(this);
        bt_Commo_Update.setOnClickListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CommodityUpdate.this, android.R.layout.simple_spinner_item, m);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        sp_Commo_category.setAdapter(adapter);
        sp_Commo_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //这个方法里可以对点击事件进行处理
                //i指的是点击的位置,通过i可以取到相应的数据源
                Updatecategory = adapterView.getItemAtPosition(i).toString();//获取i所在的文本
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_Commo_Cancel:
                finish();
                break;
            case R.id.bt_Commo_Update:
                if (filename != null) {//文件名不为空就上传图片
                    Log.e("TAG", "文件名：" + filename);
                    FileUpload();
                }
                UpdateCommData();
                break;
            case R.id.bt_choose_Commo:
                if (ContextCompat.checkSelfPermission(CommodityUpdate.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CommodityUpdate.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    initpermission();
                }
                break;
        }
    }

    private void UpdateCommData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Updatetitle = ed_Commo_title.getText().toString();
                Updateamount = ed_Commo_amount.getText().toString();
                Updateprice = ed_Commo_price.getText().toString();
                if (filename != null) {
                    Log.e("TAG", "文件名：" + filename);
                    Updatephoto = "http://182.254.230.137:8080/admintes//images/" + filename;
                }
                String sqlString = "update commodity set category = ?,title=?,amount=?,price=?,url=? where id = ?";
                Object[] paObjects = new Object[]{Updatecategory, Updatetitle, Updateamount, Updateprice, Updatephoto, id};
                if (DBHelper.Update(sqlString, paObjects)) {
                    handler.sendEmptyMessage(2);
                    Log.e("TAG", "更新成功！");
                } else {
                    Log.e("TAG", "更新失败！");
                }
            }
        }).start();
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
                    Toast.makeText(CommodityUpdate.this, "权限授取成功！", Toast.LENGTH_SHORT).show();
                    initpermission();
                } else {
                    Toast.makeText(CommodityUpdate.this, "您未授取权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void initpermission() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 2);
    }

    private int StringByEquals(String[] mlist, String item) {
        int count = 0;
        for (int i = 0; i < mlist.length; i++) {
            if (item.equals(mlist[i])) {
                Log.e("TAG", "" + i);
                return i;
            }
        }
        Log.e("TAG", "" + count);
        return count;
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
                iv_Commo_getphoto.setImageURI(uri);//设置为选择的那张图片
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
        Cursor actualimagecursor = CommodityUpdate.this.managedQuery(uri, proj, null,
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
                .execute(new CommMyStringCallback());
    }


    /**
     * 回调
     */
    public class CommMyStringCallback extends StringCallback {
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
            br_Commo.setProgress((int) (100 * progress));
            if (progress == 1.0) {
                handler.sendEmptyMessageDelayed(2, 1000);
            }
        }
    }

}