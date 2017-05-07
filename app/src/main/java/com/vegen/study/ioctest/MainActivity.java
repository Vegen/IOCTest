package com.vegen.study.ioctest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vegen.study.vegenioc.CheckNet;
import com.vegen.study.vegenioc.OnClick;
import com.vegen.study.vegenioc.ViewById;
import com.vegen.study.vegenioc.ViewUtils;

public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.tv_hello)
    private TextView tv_hello;

    @ViewById(R.id.iv_img)
    private ImageView iv_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
        tv_hello.setText("你好，IOC");
    }

    @OnClick({R.id.tv_hello, R.id.iv_img})
    @CheckNet
    //方法名随意
    private void sayHello(){
        Toast.makeText(this, tv_hello.getText().toString(), Toast.LENGTH_SHORT).show();
    }

}
