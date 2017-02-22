package com.icetea09.vivid.features.imagepicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.icetea09.vivid.R;
import com.icetea09.vivid.adapter.FolderPickerAdapter;
import com.icetea09.vivid.adapter.ImagePickerAdapter;
import com.icetea09.vivid.databinding.ActivityImagePickerBinding;
import com.icetea09.vivid.features.ImageLoader;
import com.icetea09.vivid.features.camera.CameraHelper;
import com.icetea09.vivid.listeners.OnFolderClickListener;
import com.icetea09.vivid.listeners.OnImageClickListener;
import com.icetea09.vivid.model.Folder;
import com.icetea09.vivid.model.Image;
import com.icetea09.vivid.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_SELECTED_IMAGES;

public class ImagePickerActivity extends AppCompatActivity implements OnImageClickListener {

    private static final String TAG = ImagePickerActivity.class.getSimpleName();
    private static final int RC_CAPTURE = 2000;
    public static final int RC_PERMISSION_WRITE_EXTERNAL_STORAGE = 9653;
    public static final int RC_PERMISSION_CAMERA = 3524;

    public static Intent newIntent(Context context, Configuration config) {
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(Configuration.class.getSimpleName(), config);
        return intent;
    }

    ActivityImagePickerBinding binding;
    private GridLayoutManager layoutManager;
    private GridSpacingItemDecoration itemOffsetDecoration;
    private ImagePickerAdapter imageAdapter;
    private FolderPickerAdapter folderAdapter;

    private ImagePickerPresenter presenter;

    private ContentObserver observer;
    private Parcelable foldersState;

    private int imageColumns;
    private int folderColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_picker);

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }

        Bundle bundle = intent.getExtras();
        Configuration config = bundle.getParcelable(Configuration.class.getSimpleName());
        config = config != null ? config : Configuration.create(this, intent);
        presenter = new ImagePickerPresenter(new ImageLoader(this), config);
        presenter.attachView(this);
        orientationBasedUI(getResources().getConfiguration().orientation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                getData();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataWithPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.abortLoad();
            presenter.detachView();
        }

        if (observer != null) {
            getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }
    }

    /**
     * Create option menus and update title
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_picker_menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuDone = menu.findItem(R.id.menu_done);
        presenter.updateMenuDoneVisibility(menuDone);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handle option menu's click event
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.menu_done) {
            presenter.onDoneSelectImages();
        } else if (id == R.id.menu_camera) {
            captureImageWithPermission();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Config recyclerView when configuration changed
     */
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getData();
                    return;
                }
                finish();
            }
            break;
            case RC_PERMISSION_CAMERA: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImage();
                    return;
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAPTURE && resultCode == RESULT_OK) {
            presenter.finishCaptureImage(this, data);
        }
    }

    @Override
    public void onClick(View view, int position) {
        Image image = imageAdapter.getItem(position);
        presenter.handleImageClick(position, image);
    }


    /**
     * When press back button, show folders if view is displaying images
     */
    @Override
    public void onBackPressed() {
        if (!isDisplayingFolderView()) {
            setFolderAdapter(null);
            return;
        }
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }





    public void setUpView(String title) {
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        /** Init folder and image adapter */
        imageAdapter = new ImagePickerAdapter(this, this);
        folderAdapter = new FolderPickerAdapter(this, new OnFolderClickListener() {
            @Override
            public void onFolderClick(Folder folder) {
                foldersState = binding.recyclerView.getLayoutManager().onSaveInstanceState();
                setImageAdapter(folder.getImages());
            }
        });
    }

    /**
     * Update activity title
     * If we're displaying folder, set folder title
     * If we're displaying images, show number of selected images
     */
    public void updateTitle(String title, int mode, int noSelectedImages, int limit) {
        supportInvalidateOptionsMenu();
        binding.toolbar.setTitle(title);
        if (!isDisplayingFolderView() && mode == ImagePicker.MULTIPLE) {
            binding.toolbar.setTitle(limit == ImagePicker.MAX_LIMIT
                    ? String.format(getString(R.string.ef_selected), noSelectedImages)
                    : String.format(getString(R.string.ef_selected_with_limit), noSelectedImages, limit));
        }
    }

    /**
     * Check if displaying folders view
     */
    public boolean isDisplayingFolderView() {
        return ((binding.recyclerView.getAdapter() == null || binding.recyclerView.getAdapter() instanceof FolderPickerAdapter));
    }

    public void finishPickImages(List<Image> images) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_SELECTED_IMAGES,
                (ArrayList<? extends Parcelable>) images);
        setResult(RESULT_OK, data);
        finish();
    }

    public void showCapturedImage() {
        getDataWithPermission();
    }

    public void showFetchCompleted(List<Folder> folders) {
        setFolderAdapter(folders);
    }

    public void showError(Throwable throwable) {
        String message = throwable != null ? throwable.getMessage() : "Unknown Error";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.tvEmptyImages.setVisibility(View.GONE);
    }

    public void showEmpty() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.tvEmptyImages.setVisibility(View.VISIBLE);
    }

    public void updateSelectedImage(Image image) {
        imageAdapter.addSelected(image);
    }

    public void showErrorExceedLimit() {
        Toast.makeText(this, R.string.ef_msg_limit_images, Toast.LENGTH_SHORT).show();
    }

    public void removeImage(Image image, int clickPosition) {
        imageAdapter.removeSelectedPosition(image, clickPosition);
    }

    public void removeAllImages() {
        imageAdapter.removeAllSelectedSingleClick();
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setImageAdapter(List<Image> images) {
        imageAdapter.setData(images);
        setItemDecoration(imageColumns);
        binding.recyclerView.setAdapter(imageAdapter);
    }

    /**
     * Set folder adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setFolderAdapter(List<Folder> folders) {
        if (folders != null) {
            folderAdapter.setData(folders);
        }
        setItemDecoration(folderColumns);
        binding.recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            binding.recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
    }

    /**
     * Set item size, column size base on the screen orientation
     */
    private void orientationBasedUI(int orientation) {
        imageColumns = orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT ? 3 : 5;
        folderColumns = orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT ? 2 : 4;

        int columns = isDisplayingFolderView() ? folderColumns : imageColumns;
        layoutManager = new GridLayoutManager(this, columns);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setHasFixedSize(true);
        setItemDecoration(columns);
    }

    /**
     * Set item decoration
     */
    private void setItemDecoration(int columns) {
        layoutManager.setSpanCount(columns);
        if (itemOffsetDecoration != null)
            binding.recyclerView.removeItemDecoration(itemOffsetDecoration);
        itemOffsetDecoration = new GridSpacingItemDecoration(columns, getResources().getDimensionPixelSize(R.dimen.ef_item_padding), false);
        binding.recyclerView.addItemDecoration(itemOffsetDecoration);
    }

    /**
     * Check permission
     */
    private void getDataWithPermission() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            getData();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RC_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void getData() {
        presenter.abortLoad();
        presenter.loadImages(true);
    }

    /**
     * Request for camera permission
     */
    private void captureImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        RC_PERMISSION_CAMERA);
            }
        } else {
            captureImage();
        }
    }

    /**
     * Start camera intent
     * Create a temporary file and pass file Uri to camera intent
     */
    private void captureImage() {
        if (!CameraHelper.checkCameraAvailability(this)) {
            return;
        }
        presenter.captureImage(this, RC_CAPTURE);
    }

}
