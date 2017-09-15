package com.icetea09.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.icetea09.vivid.imagepicker.ImagePicker;
import com.icetea09.vivid.model.Image;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_VIVID_PICKER = 2000;

    private TextView tvSelectedImages;
    private List<Image> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSelectedImages = (TextView) findViewById(R.id.tv_selected_images);
        final Switch switchSingleMode = (Switch) findViewById(R.id.switch_mode_single);
        final Switch switchReturnAfterFirst = (Switch) findViewById(R.id.switch_return_after_first);
        final EditText etLimitImages = (EditText) findViewById(R.id.et_limit_images);

        findViewById(R.id.btn_launch_vivid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mode = switchSingleMode.isChecked() ? ImagePicker.SINGLE : ImagePicker.MULTIPLE;
                int limit = Integer.parseInt(etLimitImages.getText().toString());

                ImagePicker.create(MainActivity.this)
                        .mode(mode)
                        .limit(limit)
                        .returnAfterFirst(switchReturnAfterFirst.isChecked())
                        .start(RC_VIVID_PICKER);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (requestCode == RC_VIVID_PICKER && resultCode == RESULT_OK && data != null) {
            images = data.getParcelableArrayListExtra(ImagePicker.Companion.getEXTRA_SELECTED_IMAGES());
            printImages(images);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void printImages(List<Image> images) {
        if (images == null) return;

        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < images.size(); i++) {
            stringBuffer.append(images.get(i).getPath()).append("\n");
        }
        tvSelectedImages.setText(stringBuffer.toString());
    }
}
