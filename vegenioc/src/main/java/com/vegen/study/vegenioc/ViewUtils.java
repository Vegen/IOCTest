package com.vegen.study.vegenioc;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @creation_time: 2017/5/7
 * @author: Vegen
 * @e-mail: vegenhu@163.com
 * @describe: View工具类
 */

public class ViewUtils {
    public static void inject(Activity activity){
        inject(new ViewFinder(activity), activity);
    }
    public static void inject(View view){
        inject(new ViewFinder(view), view);
    }
    public static void inject(View view, Object object){
        inject(new ViewFinder(view), object);
    }
    // 兼容上面三个方法   object是反射需要执行的类
    public static void inject(ViewFinder finder, Object object){
        // 注入属性
        injectFile(finder, object);
        // 注入事件
        injectEvent(finder, object);
    }

    /**
     * 事件注入
     * @param finder
     * @param object
     */
    private static void injectEvent(ViewFinder finder, Object object) {
        // 1、获取类的所有方法
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        // 2、获取OnClick里面的value值
        for (Method method : methods){
            OnClick onClick = method.getAnnotation(OnClick.class);
            if (onClick != null){
                int[] viewIds = onClick.value();
                for (int viewId : viewIds){
                    // 3、findViewById找到View
                    View view = finder.findViewById(viewId);

                    //拓展功能 检测网络
                    boolean isCheckNet = method.getAnnotation(CheckNet.class) != null;

                    if (view != null){
                        // 4、setOnClickListener
                        view.setOnClickListener(new DeclaredOnClickListener(method, object, isCheckNet));
                    }
                }
            }
        }
    }

    private static class DeclaredOnClickListener implements View.OnClickListener{

        private Object mObject;
        private Method mMethod;
        private boolean mIsCheckNet;

        public DeclaredOnClickListener(Method method, Object object, boolean isCheckNet) {
            this.mMethod = method;
            this.mObject = object;
            this.mIsCheckNet = isCheckNet;
        }

        @Override
        public void onClick(View view) {
            //判断是否需要检测网络
            if (mIsCheckNet){
                //需要
                if (!isNetConnected(view.getContext())){
                    Toast.makeText(view.getContext(), "请检查网络，请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //点击会调用该方法
            try {
                // 所有方法都可以 包括私有公有
                mMethod.setAccessible(true);
                // 5、反射注入执行方法
                mMethod.invoke(mObject, view);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    mMethod.invoke(mObject, null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 检测手机数据是否可用
     */
    public static boolean isMobileNetConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
    }

    /**
     * 检测WIFI是否开启可用
     */
    public static boolean isWifiNetConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
    }

    /**
     * 检测网络是否可用
     */
    public static boolean isNetConnected(Context context){
        return isMobileNetConnected(context) || isWifiNetConnected(context);
    }

    /**
     * 注入属性
     * @param finder
     * @param object
     */
    private static void injectFile(ViewFinder finder, Object object) {
        // 1、获取类里面的所有的属性
        Class<?> clazz = object.getClass();
        // 获取所有属性包括私有和公有
        Field[] fields = clazz.getDeclaredFields();

        // 2、获取ViewById里面的value值
        for (Field field : fields){
            ViewById viewById = field.getAnnotation(ViewById.class);
            if (viewById != null){
                // 获取注解里面的id值
                int viewId = viewById.value();

                // 3、findViewById找到View
                View view = finder.findViewById(viewId);
                if (view != null) {
                    // 能够注入所有的修饰符 private public
                    field.setAccessible(true);

                    // 4、动态注入找到的View
                    try {
                        field.set(object, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }



    }
}
