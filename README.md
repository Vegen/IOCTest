##### **Android中IOC框架就是注入控件和布局或者说是设置点击监听，网上有很多成熟的注解框架例如xUtils，afinal，butterknife等等。你可能会问，既然已经有好的框架为何还要造轮子？因为，首先我是学习，学习框架的设计以及实现，其次是拓展，适合自己的轮子才是好轮子，所以我添加了判断网络状态的注解。此处特别感谢辉哥，他的技术分享是我的楷模。**

首先看看最终完成的效果

```
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
    private void sayHello(){
        Toast.makeText(this, tv_hello.getText().toString(), Toast.LENGTH_SHORT).show();
    }

}
```

运行效果
![有网和无网状态](http://img.blog.csdn.net/20170507203504849?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvQW5kcm9pZHRhbGVudA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

有网状态下，可以正常点击然后提示，无网状态下提示网络的问题。

*下面看看具体实现*
我阅读了xUtils和butterknife的源码，xUtils使用有点麻烦，butterknife的定义（变量，方法等）只能是public，所以我结合两者，造出适合自己的，习惯是private定义，以及追求使用方便。

首先要了解一下反射机制，不懂的话百度一下
好，coding

#### 新建一个Module，添加以下的类

**View注解的Annotation**

```
/**
 * @creation_time: 2017/5/7
 * @author: Vegen
 * @e-mail: vegenhu@163.com
 * @describe: View注解的Annotation
 */
//@Target(ElementType.FIELD) 代表Annotation的位置  FIELD代表属性 TYPE类上  CONSTRUCTOR构造函数上  METHOD方法上面
@Target(ElementType.FIELD)
//@Retention(RetentionPolicy.CLASS) 什么时候生效 CLASS代表编译时 RUNTIME运行时 SOURCE源码资源
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewById {
    //@ViewById(R.id,xxx)
    int value();
}
```

**View辅助类**

```
/**
 * @creation_time: 2017/5/7
 * @author: Vegen
 * @e-mail: vegenhu@163.com
 * @describe: View辅助类
 */

public class ViewFinder {
    private Activity activity;
    private View view;

    public ViewFinder(Activity activity) {
        this.activity = activity;
    }

    public ViewFinder(View view) {
        this.view = view;
    }

    public View findViewById(int viewId){
        return activity != null ? activity.findViewById(viewId) : view.findViewById(viewId);
    }
}
```
**View工具类**

```
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

```

**View 事件注解的Annotation**

```
/**
 * @creation_time: 2017/5/7
 * @author: Vegen
 * @e-mail: vegenhu@163.com
 * @describe: View 事件注解的Annotation
 */
//@Target(ElementType.FIELD) 代表Annotation的位置  FIELD代表属性 TYPE类上  CONSTRUCTOR构造函数上  METHOD方法上面
@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.CLASS) 什么时候生效 CLASS代表编译时 RUNTIME运行时 SOURCE源码资源
@Retention(RetentionPolicy.RUNTIME)
public @interface OnClick {
    //@ViewById(R.id,xxx)
    int[] value();
}
```

**View 网络状态注解的Annotation**

```
/**
 * @creation_time: 2017/5/7
 * @author: Vegen
 * @e-mail: vegenhu@163.com
 * @describe: View 网络状态注解的Annotation
 */
//@Target(ElementType.FIELD) 代表Annotation的位置  FIELD代表属性 TYPE类上  CONSTRUCTOR构造函数上  METHOD方法上面
@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.CLASS) 什么时候生效 CLASS代表编译时 RUNTIME运行时 SOURCE源码资源
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckNet {
}
```

好，到此为止已经完成注解框架了，下面看看使用
**MainActivity**

```
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
```

布局文件activity_main.xml

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.vegen.study.ioctest.MainActivity"
    android:orientation="vertical">

    <TextView
        android:id="@+id/tv_hello"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        android:textSize="24sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <ImageView
        android:id="@+id/iv_img"
        android:layout_marginTop="20dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@mipmap/ic_launcher"/>

</LinearLayout>

```

别忘了添加网络访问权限

```
<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

具体代码以及demo已经开源到我的github，欢迎star，有问题交流一下，地址：[Vegen的github](https://github.com/Vegen/IOCTest)