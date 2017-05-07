package com.vegen.study.vegenioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
