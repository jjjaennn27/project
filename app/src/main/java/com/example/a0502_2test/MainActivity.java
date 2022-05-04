package com.example.a0502_2test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView imageView;
    Button btnCamera;
    final static int TAKE_PICTURE = 1;
    final static int CROP_PICTURE = 2;
    private Uri pictureUri;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.iv_takepicture);
        btnCamera = (Button)findViewById(R.id.btn_takepicture);
        btnCamera.setOnClickListener(this); //Uri exposure 무시
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build()); //권한 요청

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //안드로이드 버전확인
            // 권한 허용이 됐는지 확인
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) { }
            else { //권한 허용 요청
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
        }
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            // 버튼 눌렀을 때, 카메라에서 이미지 가져오기
            case R.id.btn_takepicture:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 임시로 사용할 파일의 경로를 생성
                String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                pictureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, pictureUri);
                cameraIntent.putExtra("return-data", true);
                startActivityForResult(cameraIntent, CROP_PICTURE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            //크롭된 이미지 가져와서 이미지뷰에 보여주기
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK && data.hasExtra("data")) { //데이터를 가지고 있는지 확인
                    final Bundle extras = data.getExtras();
                    if(extras != null)
                    { Bitmap photo = extras.getParcelable("data"); //크롭한 이미지 가져오기
                        imageView.setImageBitmap(photo); //이미지뷰에 넣기
                    }

                    // 임시 파일 삭제
                    File f = new File(pictureUri.getPath());
                    if(f.exists())
                        f.delete();

                    break;
                }
                break;

                // 이미지 크롭
            case CROP_PICTURE: {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정
                // 이후에 이미지 크롭 어플리케이션을 호출
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(pictureUri, "image/*");

                intent.putExtra("outputX", 200); //크롭한 이미지 x축 크기
                intent.putExtra("outputY", 200); //크롭한 이미지 y축 크기
                intent.putExtra("aspectX", 1); //크롭 박스의 x축 비율
                intent.putExtra("aspectY", 1); //크롭 박스의 y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, TAKE_PICTURE);
                break;
            }
        }
    }
}
