package com.bigkoo.pickerview.utils;

import android.util.Log;
import android.view.Gravity;

import com.bigkoo.pickerview.R;


/**
 * Created by Sai on 15/8/9.
 */
public class PickerViewAnimateUtil {
    private static final int INVALID = -1;
    
    /**
     * Get default animation resource when not defined by the user
     *
     * @param gravity       the gravity of the dialog
     * @param isInAnimation determine if is in or out animation. true when is is
     * @return the id of the animation resource
     */
    public static int getAnimationResource(int gravity, boolean isInAnimation) {
        switch (gravity) {
            case Gravity.BOTTOM:
                Log.e("FFFT", "000000");
                return isInAnimation ? R.anim.pickerview_slide_in_bottom : R.anim.pickerview_slide_out_bottom;
            
            case Gravity.CENTER:
                return isInAnimation ? R.anim.pickerview_dialog_scale_in : R.anim.pickerview_dialog_scale_out;
        }
        return INVALID;
    }
}
