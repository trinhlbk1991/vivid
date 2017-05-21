package com.icetea09.vivid.imagepicker;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.icetea09.vivid.R;
import com.icetea09.vivid.adapter.FolderPickerAdapter;
import com.icetea09.vivid.adapter.ImagePickerAdapter;
import com.icetea09.vivid.camera.CameraHelper;
import com.icetea09.vivid.databinding.ActivityImagePickerBinding;
import com.icetea09.vivid.model.Folder;
import com.icetea09.vivid.model.Image;
import com.icetea09.vivid.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static com.icetea09.vivid.imagepicker.ImagePicker.EXTRA_SELECTED_IMAGES;

public class ImagePickerActivity extends AppCompatActivity {

    private static final String TAG = ImagePickerActivity.class.getSimpleName();
    private static final int RC_CAPTURE = 2000;
    public static final int RC_PERMISSION_WRITE_EXTERNAL_STORAGE = 9653;
    public static final int RC_PERMISSION_CAMERA = 3524;

    public static Intent newIntent(Context context, Configuration config) {
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(Configuration.class.getSimpleName(), config);
        return intent;
    }

    Toolbar toolbar;
    RecyclerView recyclerView;
    FloatingActionButton fabCamera;
    ProgressBar progressBar;
    TextView tvEmptyImages;

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
        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }

        Bundle bundle = intent.getExtras();
        Configuration config = bundle.getParcelable(Configuration.class.getSimpleName());
        config = config != null ? config : Configuration.create(this, intent);

        setTheme(config.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        tvEmptyImages = (TextView) findViewById(R.id.tv_empty_images);

        presenter = new ImagePickerPresenter(config);
        presenter.attachView(this);
        orientationBasedUI(getResources().getConfiguration().orientation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                presenter.loadImages();
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
                    presenter.loadImages();
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

    /**
     * When press back button, show folders if view is displaying images
     */
    @Override
    public void onBackPressed() {
        if (!isDisplayingFolderView()) {
            setUpFolderAdapter(null);
            return;
        }
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    public void setUpView(String title) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        imageAdapter = new ImagePickerAdapter(this, new ImagePickerAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {
                Image image = imageAdapter.getItem(position);
                presenter.handleImageClick(position, image);
            }
        });

        folderAdapter = new FolderPickerAdapter(this, new FolderPickerAdapter.OnFolderClickListener() {
            @Override
            public void onFolderClick(Folder folder) {
                foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
                setUpImageAdapter(folder.getImages());
            }
        });

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageWithPermission();
            }
        });
    }

    public void updateTitle(String title, int mode, int noSelectedImages, int limit) {
        supportInvalidateOptionsMenu();
        toolbar.setTitle(title);
        if (mode == ImagePicker.MULTIPLE) {
            toolbar.setTitle(limit == ImagePicker.MAX_LIMIT
                    ? String.format(getString(R.string.format_selected), noSelectedImages)
                    : String.format(getString(R.string.format_selected_with_limit), noSelectedImages, limit));
        }
    }

    /**
     * Check if displaying folders view
     */
    public boolean isDisplayingFolderView() {
        return ((recyclerView.getAdapter() == null || recyclerView.getAdapter() instanceof FolderPickerAdapter));
    }

    public void finishPickImages(List<Image> images) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_SELECTED_IMAGES, (ArrayList<? extends Parcelable>) images);
        setResult(RESULT_OK, data);
        finish();
    }

    public void showCapturedImage() {
        getDataWithPermission();
    }

    public void showFetchCompleted(List<Folder> folders) {
        setUpFolderAdapter(folders);
    }

    public void showError(Throwable throwable) {
        String message = throwable != null ? throwable.getMessage() : "Unknown Error";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvEmptyImages.setVisibility(View.GONE);
    }

    public void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvEmptyImages.setVisibility(View.VISIBLE);
    }

    public void updateSelectedImage(Image image) {
        imageAdapter.addSelected(image);
    }

    public void showErrorExceedLimit() {
        Toast.makeText(this, R.string.limit_images, Toast.LENGTH_SHORT).show();
    }

    public void removeImage(Image image, int clickPosition) {
        imageAdapter.removeSelectedPosition(image, clickPosition);
    }

    public void removeAllImages() {
        imageAdapter.removeAllSelectedSingleClick();
    }

    private void setUpImageAdapter(List<Image> images) {
        imageAdapter.setData(images);
        setItemDecoration(imageColumns);
        recyclerView.setAdapter(imageAdapter);
    }

    private void setUpFolderAdapter(List<Folder> folders) {
        if (folders != null) {
            folderAdapter.setData(folders);
        }
        setItemDecoration(folderColumns);
        recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
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
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        setItemDecoration(columns);
    }

    /**
     * Set item decoration
     */
    private void setItemDecoration(int columns) {
        layoutManager.setSpanCount(columns);
        if (itemOffsetDecoration != null)
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        itemOffsetDecoration = new GridSpacingItemDecoration(columns, getResources().getDimensionPixelSize(R.dimen.item_padding), false);
        recyclerView.addItemDecoration(itemOffsetDecoration);
    }

    /**
     * Check permission
     */
    private void getDataWithPermission() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            presenter.loadImages();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RC_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
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
