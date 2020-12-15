package com.example.app.Fragment;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.app.ProductDetailsActivity;
import com.example.app.R;
import com.example.app.Sql.DBHelper;
import com.example.app.Tools.NetworkChangeReceiver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :类别Fragment
 * 版本: 1.0
 */
public class CategoryFragment extends Fragment {
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://查询后设置适配器
                    SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), list, R.layout.listviewstyle, new String[]{"url", "title", "price"},
                            new int[]{R.id.iv_Shopping_Icon, R.id.tv_Shopping_title, R.id.tv_Shopping_Price});
                    simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                        @Override
                        public boolean setViewValue(View view, Object o, String s) {
                            if (view instanceof ImageView) {
                                URL url = null;
                                try {
                                    url = new URL((String) o);
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
                    Log.e("数据库", "查询成功2");
                    lv_right.setAdapter(simpleAdapter);
                    break;
                case 3:
                    //点击商品后跳转，把id转过去
                    Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                    Log.e("TAG", "" + id);
                    intent.putExtra("id", id);//把id传过去
                    startActivity(intent);
                    break;
            }
        }
    };

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    private CategoryViewModel mViewModel;
    private List<HashMap<String, Object>> list;
    private int id;//商品id
    private ListView lv_left, lv_right;//左右两边的ListView
    private CategotyAdapter categotyAdapter;//自定义适配器
    private int mCurrentPos;//左边ListView选中的元素
    private String[] str = new String[]{"所有", "手机", "电脑", "显示器", "相机", "图书", "食品", "饮料", "冰箱", "空调", "电视", "热水器", "洗衣机", "电饭煲", "其他"};//左边ListView的元素

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_fragment, container, false);
        lv_left = view.findViewById(R.id.lv_left);
        lv_right = view.findViewById(R.id.lv_right);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            //NetworkOnMainThreadException类是从Android API 11开始增加的异常类（Android 3.0），从Android3.0开始网络访问的代码就不能写在主线程中了
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        int netWorkStart = NetworkChangeReceiver.getNetWorkStart(getActivity());//判断网络状态
        if (netWorkStart == 1) {//如果没有连接数据和wifi
        } else {
            // TODO: Use the ViewModel
            categotyAdapter = new CategotyAdapter();//实例化适配器
            lv_left.setAdapter(categotyAdapter);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String sqlString = "select * from commodity where amount != 0";//向数据库查询那些商品数量不为0的商品
                    list = DBHelper.getList(sqlString, null);
                    handler.sendEmptyMessage(1);
                    Log.e("数据库", "查询成功");
                }
            }).start();
            lv_left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    getDate(str[i]);
                    // 更新当前被选中的位置
                    mCurrentPos = i;
                    //刷新适配器
                    categotyAdapter.notifyDataSetChanged();
                }
            });
            lv_right.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Map<String, Object> map = list.get(i);
                    id = Integer.parseInt(map.get("id").toString());
                    handler.sendEmptyMessage(3);
                }
            });
        }
    }

    class CategotyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO 自动生成的方法存根
            return str.length;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO 自动生成的方法存根
            return str[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            // TODO 自动生成的方法存根
            return arg0;
        }

        @Override
        //关键点
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            // TODO 自动生成的方法存根
            ViewHolder holder;//创建Holder
            if (arg1 == null) {
                arg1 = LayoutInflater.from(getActivity()).
                        inflate(R.layout.listviewstyle_2, arg2, false);//实例化zz布局
                holder = new ViewHolder();//实例化类
                holder.mTextView = (TextView) arg1.findViewById(R.id.tv_listviewstyle2);
                arg1.setTag(holder);//添加标签
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.mTextView.setText(str[arg0]);
            // 只有当更新的位置等于当前位置时，更改颜色
            if (mCurrentPos == arg0) {
                holder.mTextView.setBackgroundColor(Color.rgb(35, 154, 237));
            } else {
                holder.mTextView.setBackgroundColor(Color.TRANSPARENT);
            }
            return arg1;
            //用来定义布局中的控件
        }

        class ViewHolder {
            TextView mTextView;
        }
    }

    /**
     * 根据listview点击中的元素进行查询
     *
     * @param category
     */
    public void getDate(final String category) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (category.equals("所有")) {
                    String sqlString = "select * from commodity where amount != 0";
                    list = DBHelper.getList(sqlString, null);
                    handler.sendEmptyMessage(1);
                } else {
                    String sqlString = "select * from commodity where category=?  and amount != 0";
                    Object[] paObjects = new Object[]{category};
                    list = DBHelper.getList(sqlString, paObjects);
                    handler.sendEmptyMessage(1);
                    Log.e("数据库", "查询成功");
                }
            }
        }).start();
    }
}