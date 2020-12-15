package com.example.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.app.MainActivity;
import com.example.app.PurchasingActivity;
import com.example.app.R;
import com.example.app.Sql.DBHelper;
import com.example.app.Tools.NetworkChangeReceiver;
import com.example.app.UpdateUserActivity;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :个人中心Fragment
 * 版本: 1.0
 */
public class MineFragment extends Fragment implements View.OnClickListener {

    private MineViewModel mViewModel;
    private Button bt_UpdateUser, bt_Cancellation, bt_Purchasing;
    private ImageView iv_UserPhoto;
    private TextView tv_UserName, tv_UserSex, tv_UserAge, tv_UserAddress;
    private String UserPhoneNumber, UserName, UserSex, UserAge, UserAddress, UserPhoto;

    public static MineFragment newInstance() {
        return new MineFragment();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tv_UserName.setText(UserName);
                    tv_UserSex.setText(UserSex);
                    tv_UserAge.setText(UserAge);
                    tv_UserAddress.setText(UserAddress);
                    Picasso.with(getActivity()).load(UserPhoto).into(iv_UserPhoto);//设置用户的头像
//                    Bitmap bitmap = getHttpBitmap(UserPhoto);
//                    //显示
//                    iv_UserPhoto.setImageBitmap(bitmap);
                    break;
                case 2:
                    Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
                    startActivity(intent);
                    break;
                case 3:

                    break;
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mine_fragment, container, false);
        tv_UserName = view.findViewById(R.id.tv_UserName);
        tv_UserSex = view.findViewById(R.id.tv_UserSex);
        tv_UserAge = view.findViewById(R.id.tv_UserAge);
        tv_UserAddress = view.findViewById(R.id.tv_UserAddress);
        bt_UpdateUser = view.findViewById(R.id.bt_UpdateUser);
        iv_UserPhoto = view.findViewById(R.id.iv_UserPhoto);
        bt_Purchasing = view.findViewById(R.id.bt_Purchasing);
        bt_Cancellation = view.findViewById(R.id.bt_Cancellation);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MineViewModel.class);
        // TODO: Use the ViewModel
        if (android.os.Build.VERSION.SDK_INT > 9) {
            //NetworkOnMainThreadException类是从Android API 11开始增加的异常类（Android 3.0），从Android3.0开始网络访问的代码就不能写在主线程中了
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        int netWorkStart = NetworkChangeReceiver.getNetWorkStart(getActivity());
        if (netWorkStart == 1) {
        } else {//如果有网
            Passwordverification();

        }
        bt_UpdateUser.setOnClickListener(this);
        bt_Purchasing.setOnClickListener(this);
        bt_Cancellation.setOnClickListener(this);
    }

    private void Passwordverification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getActivity().getSharedPreferences("data", MODE_PRIVATE);
                UserPhoneNumber = sp.getString("phonenumber", "");
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
                    UserName = row.get("name").toString().trim();
                    UserSex = row.get("sex").toString().trim();
                    UserAge = row.get("age").toString().trim();
                    UserAddress = row.get("address").toString().trim();
                    UserPhoto = row.get("photo").toString().trim();
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    public static Bitmap getHttpBitmap(String url) {
        URL myFileURL;
        Bitmap bitmap = null;
        try {
            myFileURL = new URL(url);
            //获得连接
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(6000);
            //连接设置获得数据流
            conn.setDoInput(true);
            //不使用缓存
            conn.setUseCaches(false);
            //这句可有可无，没有影响
            //conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            //关闭数据流
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_UpdateUser:
                Log.e("数据库", "正在跳转" + getActivity());
                Intent intent1 = new Intent(getActivity(), UpdateUserActivity.class);
                startActivity(intent1);
                break;
            case R.id.bt_Purchasing:
                Intent intent3 = new Intent(getActivity(), PurchasingActivity.class);
                startActivity(intent3);
                break;
            case R.id.bt_Cancellation:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                SharedPreferences preferences = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();//清空
                editor.commit();
                startActivity(intent);
                getActivity().finish();
                break;
        }
    }
}