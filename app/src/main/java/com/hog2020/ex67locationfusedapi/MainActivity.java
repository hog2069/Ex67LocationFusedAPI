package com.hog2020.ex67locationfusedapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    TextView tv;

    //Fused API : Google 지도에 사용되고 있는 위치정보 제공자 최적화 라이브러리

    //Google 라이브러리추가: play-Services

    GoogleApiClient googleApiClient;    //위치정보 관리객체(LocationManager 역할)
    FusedLocationProviderClient providerClient; //위치정보제공자 객체(알아서 적절한 위치정보제공자를 선택)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);

        //위치 정보제공에 대한 동적 퍼미션
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkresult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkresult == PackageManager.PERMISSION_DENIED) {
                String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permission, 0);
            }
        }
    }//oncreate

    //requestpermission()을 다이얼로그 선택환료시 자동 발동하는 콜백메소드

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "위치정보제공에 동의 하셨습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "이앱을 사용할 수 없습니다", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public void clickBtn(View view) {
        //위치 정보 관리객체 생성을 위한 빌더객체 생성
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        //1.구글 API 사용 키 설정
        builder.addApi(LocationServices.API);//범용키 적용
        //2.위치정보 연결 성공 리스너
        builder.addConnectionCallbacks(connectionCallbacks);

        //3.위치정보 연결 실패 리스너
        builder.addOnConnectionFailedListener(connectionFailedListener);

        //위치정보 관리 객체 생성
        googleApiClient = builder.build();

        //위치정보 접속하기 -연결이 성공되면 성공리스너가 발동
        googleApiClient.connect();

        //위치정보 제공자 객체 생성
        providerClient = LocationServices.getFusedLocationProviderClient(this);
    }

    //위치정보  갱신 내역을 듣는 리스너
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            //마지막에 확인된 위치정보 얻기
            Location location =locationResult.getLastLocation();
            double laitude=location.getLatitude();
            double longitude=location.getLongitude();
            tv.setText(laitude+","+longitude);
        }
    };

    //위치정보를 얻기 위한 접근에 성공하였을 때 반응하는 리스너
    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            //연결에 성공
            Toast.makeText(MainActivity.this, "위치정보 탐색이 가능합니다.", Toast.LENGTH_SHORT).show();

            //위치 정보 요청객체를 생성 및 설정
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //높은 정확도를 우선:gps
            locationRequest.setInterval(5000); //위치 정보 탐색주기 : 5초마다

            //위치정보제공자 객체에게 실시간 위치정보업데이트를 요청
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            providerClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    //위치정보 접근 실패 리스너
    GoogleApiClient.OnConnectionFailedListener connectionFailedListener= new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(MainActivity.this, "위치정보 탐색을 할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    //액티비티가 화면에서 보이지않으면 더이상 위치탐색을 안하도록 업데이트를 안하도록


    @Override
    protected void onPause() {
        super.onPause();

        if (providerClient!=null && locationCallback!= null){
            providerClient.removeLocationUpdates(locationCallback);
        }
    }
}//mainactivity