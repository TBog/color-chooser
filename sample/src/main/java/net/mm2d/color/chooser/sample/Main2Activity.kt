/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.color.chooser.sample

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import net.mm2d.color.chooser.ColorChooserDialog

class Main2Activity : AppCompatActivity(), ColorChooserDialog.Callback {
    private var color: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        button1.setOnClickListener {
            ColorChooserDialog.show(this, REQUEST_CODE, color)
        }
        button2.setOnClickListener {
            ColorChooserDialog.show(this, REQUEST_CODE, color, true)
        }
        color = Color.parseColor("#B71C1C")
        sample.setBackgroundColor(color)
    }

    override fun onColorChooserResult(requestCode: Int, resultCode: Int, color: Int) {
        if (requestCode != REQUEST_CODE || resultCode != Activity.RESULT_OK) return
        this.color = color
        sample.setBackgroundColor(color)
    }

    companion object {
        private const val REQUEST_CODE = 10
    }
}
