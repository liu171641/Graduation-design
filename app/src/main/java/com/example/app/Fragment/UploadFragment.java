package com.example.app.Fragment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.app.R;
import com.example.app.Sql.DBHelper;
import com.example.app.UploadSuccessActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;

import okhttp3.Call;
import okhttp3.Request;

import static android.content.Context.MODE_PRIVATE;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :上传商品Fragment
 * 版本: 1.0
 */
public class UploadFragment extends Fragment implements View.OnClickListener {
    private UploadViewModel mViewModel;
    private ImageView iv_Upload_getphoto;
    private EditText ed_Upload_title, ed_Upload_price, ed_Upload_amount;
    private Spinner sp_Upload_category;
    private static final String[] m = {"手机", "电脑", "显示器", "相机", "图书", "食品", "饮料", "冰箱", "空调", "电视", "热水器", "洗衣机", "电饭煲", "其他"};
    private ProgressBar br_Upload;
    private Button bt_Upload_Upload, button, bt_Upload_Success;
    private File file;//文件路径
    private String filename, category;//图片名字
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(getActivity(), "添加商品成功", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(getActivity(), "添加商品失败", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(getActivity(), "请输入完整的信息", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public static UploadFragment newInstance() {
        return new UploadFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_fragment, container, false);
        bt_Upload_Upload = view.findViewById(R.id.bt_Upload_Upload);
        iv_Upload_getphoto = view.findViewById(R.id.iv_Upload_getphoto);
        ed_Upload_title = view.findViewById(R.id.ed_Upload_title);
        ed_Upload_price = view.findViewById(R.id.ed_Upload_price);
        ed_Upload_amount = view.findViewById(R.id.ed_Upload_amount);
        sp_Upload_category = view.findViewById(R.id.sp_Upload_category);
        br_Upload = view.findViewById(R.id.br_Upload);
        bt_Upload_Success = view.findViewById(R.id.bt_Upload_Success);
        button = view.findViewById(R.id.bt_choose);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UploadViewModel.class);
        // TODO: Use the ViewModel
        bt_Upload_Upload.setOnClickListener(this);
        button.setOnClickListener(this);
        bt_Upload_Success.setOnClickListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, m);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        sp_Upload_category.setAdapter(adapter);
        sp_Upload_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //这个方法里可以对点击事件进行处理
                //i指的是点击的位置,通过i可以取到相应的数据源
                category = adapterView.getItemAtPosition(i).toString();//获取i所在的文本
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
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
                    Toast.makeText(getActivity(), "权限授取成功！", Toast.LENGTH_SHORT).show();
                    initpermission();
                } else {
                    Toast.makeText(getActivity(), "您未授取权限！", Toast.LENGTH_SHORT).show();
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
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                file = uri2File(uri);//全路径转path
                String fl = file + "";
                String[] strs = fl.split("/");//以/切割
                filename = strs[strs.length - 1];//获得切割后的最后一位
                iv_Upload_getphoto.setImageURI(uri);//设置为选择的那张图片
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
        Cursor actualimagecursor = getActivity().managedQuery(uri, proj, null,
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_choose:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    initpermission();
                }

                break;
            case R.id.bt_Upload_Upload:
                SharedPreferences sp = getActivity().getSharedPreferences("data", MODE_PRIVATE);
                final String uploadsphonenumber = sp.getString("phonenumber", "");
                final String title = ed_Upload_title.getText().toString();
                final String amount = ed_Upload_amount.getText().toString();
                final String price = ed_Upload_price.getText().toString();
                if (filename != null && title != null && amount != null && price != null) {
                    FileUpload();//上传图片
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String url = "http://182.254.230.137:8080/admintes//images/" + filename;
                            String sqlString = "insert into commodity(category,title,amount,price,url,uploadsphonenumber) values(?,?,?,?,?,?)";
                            Log.e("TAG", "上传：" + uploadsphonenumber);
                            Object[] paObjects = new Object[]{category, title, amount, price, url, uploadsphonenumber};
                            if (DBHelper.Update(sqlString, paObjects)) {
//                                handler.sendEmptyMessage(1);
                                Log.e("TAG", "添加成功！");
                            } else {
                                handler.sendEmptyMessage(2);
                                Log.e("TAG", "添加失败！");
                            }
                        }
                    }).start();
                } else {
                    handler.sendEmptyMessage(3);
                }
                break;
            case R.id.bt_Upload_Success:
                Intent intent1 = new Intent(getActivity(), UploadSuccessActivity.class);
                startActivity(intent1);
                break;
        }
    }

    private void initpermission() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 2);
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
            br_Upload.setProgress((int) (100 * progress));
            if (progress == 1.0) {
                handler.sendEmptyMessage(1);
            }
        }
    }

}