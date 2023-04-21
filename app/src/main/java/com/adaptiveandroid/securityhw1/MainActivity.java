package com.adaptiveandroid.securityhw1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final String TARGET_NETWORK = "Tareq5GHZ";

    private boolean flashStatus = false;
    private EditText login_ET_password;
    private Button login_BTN_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       initVars();
    }

    private void initVars() {
        login_BTN_login = findViewById(R.id.login_BTN_login);
        login_ET_password = findViewById(R.id.login_ET_password);

        ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA},
                PackageManager.PERMISSION_GRANTED);

        initFlash();

        login_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(check()){
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                }else{
                    Toast.makeText(MainActivity.this, "Failed to login!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public boolean check() {

        String password = login_ET_password.getText().toString();
        if(!this.checkNetworkConnection(this)){
            return false;
        }

        if(!this.checkPasswordTime(password)){
            return false;
        }

        if(!this.flashStatus){
            return false;
        }

        return true;
    }

    /** Detect you are connected to a specific network. */
    private boolean checkNetworkConnection(Context context) {
        boolean connected = false;
        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifi = wifiManager.getConnectionInfo();
        if (wifi != null) {
            // get current router Mac address
            if(wifi.getSupplicantState() == SupplicantState.COMPLETED ){
                String bssid = wifi.getSSID().replace("\"", "");
                connected = TARGET_NETWORK.equals(bssid);
            }
        }
        return connected;
    }


    private boolean checkPasswordTime(String password) {
        Date currentTime = Calendar.getInstance().getTime();
        int m = currentTime.getMinutes();
        return m == Integer.parseInt(password);
    }

    public void initFlash(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            String cameraId = cameraManager.getCameraIdList()[0];
            boolean isFlashOn = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (isFlashOn) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
                        @Override
                        public void onTorchModeUnavailable(@NonNull String cameraId) {
                            super.onTorchModeUnavailable(cameraId);
                        }

                        @Override
                        public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                            super.onTorchModeChanged(cameraId, enabled);
                            flashStatus = enabled;
                        }
                    };

                    cameraManager.registerTorchCallback(torchCallback, null);
                }
            }
        }catch (Exception e){
            Log.d("Error", e.toString());
        }

    }
}