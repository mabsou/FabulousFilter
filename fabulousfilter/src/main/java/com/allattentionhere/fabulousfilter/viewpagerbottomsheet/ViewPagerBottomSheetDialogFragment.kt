package com.allattentionhere.fabulousfilter.viewpagerbottomsheet

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


open class ViewPagerBottomSheetDialogFragment : BottomSheetDialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ViewPagerBottomSheetDialog(context!!, theme)
    }

}
