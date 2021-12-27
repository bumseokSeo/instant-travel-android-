package com.sample.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MapView.POIItemEventListener {


    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증처리
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스

    Button btn_go_main2;
    //Button btn_logout;
    Button btn_my_location;
    Button btn_find_search;
    MapView mapView;
    EditText et_text;
    ArrayList<String> place_name;
    ArrayList<String> y;
    ArrayList<String> x;
    ArrayList<String> address;


    HashMap<String, String> map;
    HashMap<String, String> map_x;
    HashMap<String, String> map_y;
    ArrayList<MapPOIItem> marker_food;
    ArrayList<MapPOIItem> marker_hotel;
    ArrayList<MapPOIItem> marker_spot;
    MapPolyline polyline;//polyline

    private String str;

    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = new MapView(this);
        GpsTracker gt=new GpsTracker(this.getApplicationContext());
        Location location=gt.getLocation();



//파이어베이스 연동
        mFirebaseAuth = mFirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Login");


        polyline = new MapPolyline();
        polyline.setTag(1000);
        polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline 컬러 지정.

        Intent intent = getIntent();







        ArrayList<String> latitude_arr =  intent.getExtras().getStringArrayList("latitude_arr");
        ArrayList<String> longitude_arr=intent.getExtras().getStringArrayList("longitude_arr");



        latitude = intent.getDoubleExtra("latitude", 0); //내위도
        longitude = intent.getDoubleExtra("longitude", 0); //내경도


        //좌표 리스트가 null이 아니라면 마커와 poly라인을 긋는다
        if(latitude_arr!=null) {
            latitude=location.getLatitude();
            longitude=location.getLongitude();
            Log.d("latitude",Double.toString(latitude));
            Log.d("longitude",Double.toString(longitude));
            //  latitude=Double.parseDouble(latitude_arr.get(0));//좌표 초기화
            // longitude=Double.parseDouble(longitude_arr.get(0));//좌표 초기화
            MapPolyline polyline = new MapPolyline();
            for(int i=0;i<latitude_arr.size();i++){//마커찍기
                MapPOIItem marker=new MapPOIItem();
                marker.setItemName(Integer.toString(i+1)+" 번째 경유지");
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(latitude_arr.get(i)),Double.parseDouble(longitude_arr.get(i))));
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                mapView.addPOIItem(marker);
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(latitude_arr.get(i)),Double.parseDouble(longitude_arr.get(i))));
            }

            mapView.addPolyline(polyline);

            // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정
            MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
            int padding = 100; // px
            mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
        }


        marker_food = new ArrayList<>();
        marker_hotel = new ArrayList<>();
        marker_spot = new ArrayList<>();
        map=new HashMap<>();
        map_x=new HashMap<>();
        map_y=new HashMap<>();

        CheckBox cb_food = findViewById(R.id.cb_food);
        CheckBox cb_hotel = findViewById(R.id.cb_hotel);
        CheckBox cb_spot = findViewById(R.id.cb_spot);

        //맛집 마커 생성
        cb_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((CheckBox) view).isChecked();

                if (checked) {
                    searchKeyword("맛집", latitude, longitude);
                } else {
                    for (int i = 0; i < marker_food.size(); i++) {
                        mapView.removePOIItem(marker_food.get(i));
                    }
                    marker_food = new ArrayList<>();
                }
            }
        });
//숙박 마커 생성
        cb_hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((CheckBox) view).isChecked();

                if (checked) {
                    searchKeyword("숙박", latitude, longitude);
                } else {
                    for (int i = 0; i < marker_hotel.size(); i++) {
                        mapView.removePOIItem(marker_hotel.get(i));
                    }
                    marker_hotel = new ArrayList<>();
                }
            }
        });
//관광 마커 생성
        cb_spot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((CheckBox) view).isChecked();

                if (checked) {
                    searchKeyword("관광지", latitude, longitude);
                } else {
                    for (int i = 0; i < marker_spot.size(); i++) {
                        mapView.removePOIItem(marker_spot.get(i));
                    }
                    marker_spot = new ArrayList<>();
                }
            }
        });




        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);//중심점 변경


        mFirebaseAuth = FirebaseAuth.getInstance();
        btn_go_main2 = findViewById(R.id.btn_go_main2);
        //btn_logout = findViewById(R.id.btn_logout);
        btn_my_location = findViewById(R.id.btn_my_location);
        btn_find_search = findViewById(R.id.btn_find_search);
        et_text = findViewById(R.id.et_text);
        str = et_text.getText().toString();

        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());//커스텀 말풍선 등록

        mapView.setPOIItemEventListener((MapView.POIItemEventListener) this);//마커 클릭 이벤트 등록

        //내위치에 맵 마커 표시
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("내 위치");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
        map_y.put("내 위치",Double.toString(latitude));
        map_x.put("내 위치",Double.toString(longitude));
        marker.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker);




//내위치로 이동하는 버튼
        btn_my_location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

//                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
//                startActivity(intent);
//                finish();


                //맵 마커 표시
                Toast.makeText(MainActivity.this, "내 위치", Toast.LENGTH_SHORT).show();
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);

                mapView.removeAllPolylines();//경유지 전부 지우기
                mapView.removeAllPOIItems();

                MapPOIItem marker = new MapPOIItem();
                marker.setItemName("내 위치");
                marker.setTag(0);
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
                marker.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                mapView.addPOIItem(marker);



            }
        });




    }

    //저장한 경로리스트로 이동하는 버튼
    public void setBtn_go_main2(View view) {
        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        startActivity(intent);
        finish();
    }
/*
    //로그아웃 버튼
    public void setBtn_logout(View view) {
        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        mFirebaseAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

 */


    //검색기능
    public void setBtn_find_search(View view) {
        str = et_text.getText().toString();// STR=입력된 값

        if (et_text == null || et_text.length() == 0) {
            Toast.makeText(MainActivity.this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }


        //Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        searchKeyword(str);

    }









    // CalloutBalloonAdapter 인터페이스 구현 - 커스텀 말풍선

    public class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;


        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.balloon_layout, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            //마커 클릭시 나오는 말풍선
            ((TextView) mCalloutBalloon.findViewById(R.id.ball_name)).setText(poiItem.getItemName());
            ((TextView) mCalloutBalloon.findViewById(R.id.ball_address)).setText(map.get(poiItem.getItemName()));//주소넣기 !!!필수!!!

            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return mCalloutBalloon;
        }


    }









    //말풍선 상호작용
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle(mapPOIItem.getItemName()); //제목
        dlg.setItems(R.array.balloon_list, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int pos)
            {
                String[] items = getResources().getStringArray(R.array.balloon_list);//배열 불러오기

                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();//내계정 연동

                if(pos==0){
                    //새로 만들기

                    mapView.removeAllPolylines();
                    polyline = new MapPolyline();
                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(map_y.get(mapPOIItem.getItemName())),Double.parseDouble(map_x.get(mapPOIItem.getItemName()))));
                    Toast.makeText(MainActivity.this,"경로만들기 시작" ,Toast.LENGTH_LONG).show();


                }
                if(pos==1){
                    //추가

                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(map_y.get(mapPOIItem.getItemName())),Double.parseDouble(map_x.get(mapPOIItem.getItemName()))));
                    mapView.addPolyline(polyline);

                }

                if(pos==2){
                    //종료지점


                    final EditText edittext = new EditText(MainActivity.this);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("경로이름 설정하기");
                    builder.setMessage("경로 이름을 입력해주세요");
                    builder.setView(edittext);
                    builder.setPositiveButton("입력",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(map_y.get(mapPOIItem.getItemName())),Double.parseDouble(map_x.get(mapPOIItem.getItemName()))));
                            mapView.addPolyline(polyline);

                            mapView.setTag(edittext.getText());//경로 이름설정 입력된 경로 이름<edittext.getText()>
                            String path_name = String.valueOf(edittext.getText());

                            Map<String,Object> map = new HashMap<>(); //즉석으로 객체 만들어서 사용하기
                            map.put(String.valueOf(edittext.getText()), Arrays.asList(polyline.getObjects()));

                            if(polyline.getObjects() == null){
                                Toast.makeText(MainActivity.this,"경로가 잘못 입력되었습니다." ,Toast.LENGTH_LONG).show();
                                return;
                            }

                            if(polyline.getObjects().length == 1){
                                Toast.makeText(MainActivity.this,"경로가 잘못 입력되었습니다." ,Toast.LENGTH_LONG).show();
                                return;
                            }

                            mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child("path").updateChildren(map);//login -> useraccount -> firebaseUser.getUid() -> 값

                            Map<String,Object> map2 = new HashMap<>(); //리싸이클러뷰에 보여주기위한 저장경로설정
                            map2.put("path_name", path_name);
                            mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child("path2").child(path_name).updateChildren(map2);

                            Toast.makeText(MainActivity.this,"저장되었습니다." ,Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog AL = builder.create();
                    AL.show();


                }
                if(pos==3){
                    //맵마커 지우기
                    mapView.removePOIItem(mapPOIItem);
                }
            }

        });


        AlertDialog alertDialog = dlg.create();
        alertDialog.show();
    }



    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }











    private void searchKeyword(String keyword, double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(KakaoApi.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        KakaoApi kakaoApi = retrofit.create(KakaoApi.class);
        Call<ResponseBody> call = kakaoApi.getSearchKeyword("KakaoAK 7bfe7f74d144c84dd8b100dc106a766c", keyword, Double.toString(latitude), Double.toString(longitude), 20000);
        /* KakaoAK 7bfe7f74d144c84dd8b100dc106a766c */
        /* KakaoAK 우석 9faef51802431c71b7c185137def2dc6 */
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Test", response.raw().toString());
                try {
                    //Log.d("Test", response.body().string());
                    String data = response.body().string();
                    place_name = getData(data, "<place_name>");
                    y = getData(data, "<y>");
                    x = getData(data, "<x>");
                    address=getData(data,"<road_address_name>");

                    for (int i = 0; i < place_name.size(); i++) {
                        MapPOIItem marker = new MapPOIItem();
                        marker.setItemName(place_name.get(i).toString());
                        marker.setTag(0);
                        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(y.get(i).toString()), Double.parseDouble(x.get(i).toString())));
                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                        mapView.addPOIItem(marker);
                        map.put(place_name.get(i),address.get(i)) ;
                        map_x.put(place_name.get(i), x.get(i));
                        map_y.put(place_name.get(i),y.get(i));
                        if (keyword == "맛집") {
                            marker_food.add(marker);
                        } else if (keyword == "숙박") {
                            marker_hotel.add(marker);
                        } else {
                            marker_spot.add(marker);
                        }

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }


        });
    }


    private void searchKeyword(String keyword) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(KakaoApi.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        KakaoApi kakaoApi = retrofit.create(KakaoApi.class);
        Call<ResponseBody> call = kakaoApi.getSearchOnlyKeyword("KakaoAK 7bfe7f74d144c84dd8b100dc106a766c", keyword);
        /* KakaoAK 7bfe7f74d144c84dd8b100dc106a766c */
        /* KakaoAK 우석 9faef51802431c71b7c185137def2dc6 */
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Test", response.raw().toString());
                try {
                    //Log.d("Test", response.body().string());
                    String data = response.body().string();
                    place_name = getData(data, "<place_name>");
                    y = getData(data, "<y>");
                    x = getData(data, "<x>");
                    address=getData(data,"<road_address_name>");


                    for (int i = 0; i < place_name.size(); i++) {
                        MapPOIItem marker = new MapPOIItem();
                        marker.setItemName(place_name.get(i).toString());
                        marker.setTag(0);
                        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(y.get(i).toString()), Double.parseDouble(x.get(i).toString())));
                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                        mapView.addPOIItem(marker);
                        map_x.put(place_name.get(i), x.get(i));
                        map_y.put(place_name.get(i),y.get(i));
                        map.put(place_name.get(i),address.get(i)) ;
                        if (i == 0) {
                            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(y.get(i).toString()), Double.parseDouble(x.get(i).toString())), true);
                        }


                        if (keyword == "맛집") {
                            marker_food.add(marker);
                        } else if (keyword == "숙박") {
                            marker_hotel.add(marker);
                        } else {
                            marker_spot.add(marker);
                        }

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }


        });
    }


    private ArrayList<String> getData(String dataStr, String target) {
        ArrayList<String> al = new ArrayList<>();

        int start = 0;
        int end = target.length();

        while (end <= dataStr.length() - 1) {
            String subStr = dataStr.substring(start, end);
            if (target.equals(subStr)) {
                StringBuilder stringBuilder = new StringBuilder();
                int idx = end;

                while (dataStr.charAt(idx) != '<') {
                    stringBuilder.append(dataStr.charAt(idx));
                    idx++;
                }

                al.add(stringBuilder.toString());
            }
            start++;
            end++;
        }
        return al;
    }



}
