package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.LitePal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;

    //碎片对应布局里的控件
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    //适配器
    private ArrayAdapter<String > adapter;
    private List<String> dataList=new ArrayList<>();//ListView上的数据

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;

    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.choose_area,container,false);   //加载布局
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);//适配器
        listView.setAdapter(adapter); //为ListView添加适配器

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //为ListView的子项添加点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE){  //当前列表是省份的列表
                    selectedProvince=provinceList.get(i);//获取所选的省份
                    queryCities();//查询所选省份的所有城市
                }else if (currentLevel==LEVEL_CITY){  //当前列表是市的列表
                    selectedCity=cityList.get(i);//获取所选的市
                    queryCounties();//查询所选市的所有县
                }else if (currentLevel==LEVEL_COUNTY){//当前列表是县的列表
                    String weatherId=countyList.get(i).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);//向WeatherActivity传递数据
                        startActivity(intent);
                        getActivity().finish();//关闭当前活动，跳转到WeatherActivity
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);//下拉刷新
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });

        //后退按钮点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTY){ //当前列表是县，后退后，应该列出的城市
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){////当前列表是市，后退后，应该列出的省份
                    queryProvinces();
                }
            }
        });
        queryProvinces();//首先，界面的Listview直接列出所有的省份
    }
    /*  查询所有的省份，优先从本地数据库获取，如果没有就请求服务器获取*/
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);    //省份，后退也没东西，所以隐藏按钮
        provinceList= LitePal.findAll(Province.class);//查询数据库中所有的省份
        if (provinceList.size()>0){ //数据库中已有数据,刷新ListView的显示
            dataList.clear(); //先清空ListView中数据
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//更新数据
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;  //当前ListView是省份的列表
        }else{      //数据库中没数据，改为访问服务器获取
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }


    /*  查询所有的城市，优先从本地数据库获取，如果没有就请求服务器获取*/
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE); //后退按钮可见，因为当前是城市的ListVIew，后退是省份
        cityList=LitePal.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);//查询特定省份的所有城市
        if (cityList.size()>0){//数据库中已有数据,刷新ListView的显示
            dataList.clear();//先清空ListView中数据
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();//更新数据
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;  //当前ListView是市的列表
        }else{      //数据库中没数据，改为访问服务器获取
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /*  查询所有的县，优先从本地数据库获取，如果没有就请求服务器获取*/
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE); //后退按钮可见，因为当前是城市的ListVIew，后退是省份
        countyList=LitePal.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);//查询特定省份的所有城市
        if (countyList.size()>0){//数据库中已有数据,刷新ListView的显示
            dataList.clear();//先清空ListView中数据
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();//更新数据
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;  //当前ListView是县的列表
        }else{      //数据库中没数据，改为访问服务器获取
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //根据传入的地址和类型从服务器上查询省市县的数据
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //切回主线程
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                //解析Json数据
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result==true){
                    getActivity().runOnUiThread(new Runnable() {//切回主线程
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
