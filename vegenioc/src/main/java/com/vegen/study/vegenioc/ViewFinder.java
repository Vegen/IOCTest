package com.vegen.study.vegenioc;

import android.app.Activity;
import android.view.View;

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
