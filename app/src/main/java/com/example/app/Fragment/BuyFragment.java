package com.example.app.Fragment;

import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.app.BuyUpdateActivity;
import com.example.app.R;
import com.example.app.SellUpdateActivity;
import com.example.app.Sql.DBHelper;
import com.example.app.Tools.NetworkChangeReceiver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :买入Fragment
 * 版本: 1.0
 */
public class BuyFragment extends Fragment {
    private ListView lv_User;
    private int id;
    private List<HashMap<String, Object>> list;
    private SimpleAdapter adapter;
    private SwipeRefreshLayout mRefreshLayout;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter = new SimpleAdapter(getActivity(), list, R.layout.listviewstyle_order,
                            new String[]{"shoppingurl", "state", "shoppingtitle", "shoppingprice"},
                            new int[]{R.id.iv_Order_Icon, R.id.tv_Order_State, R.id.tv_Order_Title, R.id.tv_Order_Price});
                    adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                        public boolean setViewValue(View view, Object data,
                                                    String textRepresentation) {
                            if (view instanceof ImageView) {
                                URL url = null;
                                try {
                                    url = new URL((String) data);
                                    ImageView iv = (ImageView) view;
                                    iv.setImageBitmap(BitmapFactory.decodeStream(url.openStream()));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            } else
                                return false;
                        }
                    });
                    lv_User.setAdapter(adapter);
                    break;
                case 2:
                    String sqlString = "delete from usershopping where id =  " + id;
                    if (DBHelper.Update(sqlString, null)) {
                        Log.e("数据库", id + "----删除成功!");
//                        Intent intent = new Intent(UserOrderActivity.this, UserOrderActivity.class);
//                        startActivity(intent);
//                        finish();
                    } else {
                        Log.e("数据库", id + "----删除失败!");
                    }
                    break;
                case 3:
                    //点击商品后跳转，把id转过去
                    Intent intent = new Intent(getActivity(), BuyUpdateActivity.class);
                    Log.e("TAG", "" + id);
                    intent.putExtra("id", id);//把id传过去
                    startActivity(intent);
                    break;
                case 4:
                    String updatesql = "update usershopping set state='已收货' where id =  " + id;
                    if (DBHelper.Update(updatesql, null)) {
                        Log.e("数据库", id + "----收货成功!");
                    } else {
                        Log.e("数据库", id + "----收货失败!");
                    }
                    break;


            }
        }
    };

    private BuyViewModel mViewModel;

    public static BuyFragment newInstance() {
        return new BuyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buy_fragment, container, false);
        lv_User = view.findViewById(R.id.lv_User2);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(BuyViewModel.class);
        // TODO: Use the ViewModel
        int netWorkStart = NetworkChangeReceiver.getNetWorkStart(getActivity());
        if (netWorkStart == 1) {
        } else {//如果有网
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sp = getActivity().getSharedPreferences("data", MODE_PRIVATE);
                    String UserPhoneNumber = sp.getString("phonenumber", "");
                    Object[] paObjects = new Object[]{UserPhoneNumber};
                    String sqlString = "select * from usershopping where  userphonenumber=?";
                    list = DBHelper.getList(sqlString, paObjects);
                    Log.e("数据库", "查询成功");
                    handler.sendEmptyMessage(1);
                }
            }).start();
        }
        lv_User.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Map<String, Object> map = list.get(i);
                id = Integer.parseInt(map.get("id").toString());
                handler.sendEmptyMessage(3);
//                final Map<String, Object> map = list.get(i);
//                final AlertDialog.Builder normalDialog =
//                        new AlertDialog.Builder(getActivity());
//                normalDialog.setTitle("是否已经收货?");
//                normalDialog.setPositiveButton("确定",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                id = Integer.parseInt(map.get("id").toString());
////                                list.clear();//list清空，不然不刷新
//                                handler.sendEmptyMessage(3);
//                                adapter.notifyDataSetChanged();//刷新适配器，貌似不管用
//                            }
//                        });
//                normalDialog.setNegativeButton("取消",
//                        null);
//                // 显示
//                normalDialog.show();
            }
        });
        lv_User.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final Map<String, Object> map = list.get(i);
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(getActivity());
                normalDialog.setTitle("是否取消购买?");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                id = Integer.parseInt(map.get("id").toString());
                                list.remove(i);//list也要删除，不然不刷新
                                handler.sendEmptyMessage(2);
                                adapter.notifyDataSetChanged();//刷新适配器
                            }
                        });
                normalDialog.setNegativeButton("取消",
                        null);
                // 显示
                normalDialog.show();
                return true;
            }
        });
    }

}