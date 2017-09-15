package com.icetea09.vivid.imagepicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.icetea09.vivid.R
import com.icetea09.vivid.adapter.FolderPickerAdapter
import com.icetea09.vivid.adapter.FolderPickerAdapter.OnFolderClickListener
import com.icetea09.vivid.adapter.ImagePickerAdapter
import com.icetea09.vivid.adapter.ImagePickerAdapter.OnImageClickListener
import com.icetea09.vivid.camera.CameraHelper
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.EXTRA_SELECTED_IMAGES
import com.icetea09.vivid.model.Folder
import com.icetea09.vivid.model.Image
import com.icetea09.vivid.view.GridSpacingItemDecoration
import java.util.*

class ImagePickerActivity : AppCompatActivity() {

    internal var toolbar: Toolbar? = null
    internal var recyclerView: RecyclerView? = null
    internal var fabCamera: FloatingActionButton? = null
    internal var progressBar: ProgressBar? = null
    internal var tvEmptyImages: TextView? = null

    private var layoutManager: GridLayoutManager? = null
    private var itemOffsetDecoration: GridSpacingItemDecoration? = null
    private var imageAdapter: ImagePickerAdapter? = null
    private var folderAdapter: FolderPickerAdapter? = null

    private var presenter: ImagePickerPresenter? = null

    private var observer: ContentObserver? = null
    private var foldersState: Parcelable? = null

    private var imageColumns: Int = 0
    private var folderColumns: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent
        if (intent == null || intent.extras == null) {
            finish()
            return
        }

        val bundle = intent.extras
        var config = bundle.getParcelable<Configuration>(Configuration::class.java.simpleName)
        config = if (config != null) config else Configuration.create(this, intent)

        setTheme(config.theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)
        toolbar = findViewById(R.id.toolbar) as Toolbar
        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        fabCamera = findViewById(R.id.fab_camera) as FloatingActionButton
        progressBar = findViewById(R.id.progress_bar) as ProgressBar
        tvEmptyImages = findViewById(R.id.tv_empty_images) as TextView

        presenter = ImagePickerPresenter(config)
        (presenter as ImagePickerPresenter).attachView(this)
        orientationBasedUI(resources.configuration.orientation)
    }

    override fun onStart() {
        super.onStart()
        observer = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                presenter?.loadImages()
            }
        }
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer)
    }

    override fun onResume() {
        super.onResume()
        getDataWithPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.abortLoad()
        presenter?.detachView()

        if (observer != null) {
            contentResolver.unregisterContentObserver(observer)
            observer = null
        }
    }

    /**
     * Create option menus and update title
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_picker_menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuDone = menu.findItem(R.id.menu_done)
        presenter?.updateMenuDoneVisibility(menuDone)
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Handle option menu's click event
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        } else if (id == R.id.menu_done) {
            presenter?.onDoneSelectImages()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Config recyclerView when configuration changed
     */
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationBasedUI(newConfig.orientation)
    }

    /**
     * Handle permission results
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter?.loadImages()
                    return
                }
                finish()
            }
            RC_PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImage()
                    return
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CAPTURE && resultCode == Activity.RESULT_OK) {
            presenter?.finishCaptureImage(this, data)
        }
    }

    /**
     * When press back button, show folders if view is displaying images
     */
    override fun onBackPressed() {
        if (!isDisplayingFolderView) {
            setUpFolderAdapter(null)
            return
        }
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }


    fun setUpView(title: String) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = title
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            actionBar.setDisplayShowTitleEnabled(true)
        }

        imageAdapter = ImagePickerAdapter(this, itemClickListener = object : OnImageClickListener {
            override fun onImageClick(view: View, position: Int) {
                val image = imageAdapter?.getItem(position)
                image?.let { presenter?.handleImageClick(position, image) }
            }
        })

        folderAdapter = FolderPickerAdapter(this, folderClickListener = object : OnFolderClickListener {
            override fun onFolderClick(folder: Folder) {
                foldersState = recyclerView?.layoutManager?.onSaveInstanceState()
                setUpImageAdapter(folder.images)
            }
        })
        fabCamera?.setOnClickListener { captureImageWithPermission() }
    }

    fun updateTitle(title: String, mode: Int, noSelectedImages: Int, limit: Int) {
        supportInvalidateOptionsMenu()
        toolbar?.title = title
        if (mode == ImagePicker.MULTIPLE) {
            toolbar?.title = if (limit == ImagePicker.MAX_LIMIT)
                String.format(getString(R.string.format_selected), noSelectedImages)
            else
                String.format(getString(R.string.format_selected_with_limit), noSelectedImages, limit)
        }
    }

    /**
     * Check if displaying folders view
     */
    private val isDisplayingFolderView: Boolean
        get() = recyclerView?.adapter == null || recyclerView?.adapter is FolderPickerAdapter

    fun finishPickImages(images: List<Image>) {
        val data = Intent()
        data.putParcelableArrayListExtra(EXTRA_SELECTED_IMAGES, images as ArrayList<out Parcelable>)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    fun showCapturedImage() {
        getDataWithPermission()
    }

    fun showFetchCompleted(folders: List<Folder>) {
        setUpFolderAdapter(folders)
    }

    fun showError(throwable: Throwable?) {
        val message = if (throwable != null) throwable.message else "Unknown Error"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showLoading(isLoading: Boolean) {
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView?.visibility = if (isLoading) View.GONE else View.VISIBLE
        tvEmptyImages?.visibility = View.GONE
    }

    fun showEmpty() {
        progressBar?.visibility = View.GONE
        recyclerView?.visibility = View.GONE
        tvEmptyImages?.visibility = View.VISIBLE
    }

    fun updateSelectedImage(image: Image) {
        imageAdapter?.addSelected(image)
    }

    fun showErrorExceedLimit() {
        Toast.makeText(this, R.string.limit_images, Toast.LENGTH_SHORT).show()
    }

    fun removeImage(image: Image, clickPosition: Int) {
        imageAdapter?.removeSelectedPosition(image, clickPosition)
    }

    fun removeAllImages() {
        imageAdapter?.removeAllSelectedSingleClick()
    }

    private fun setUpImageAdapter(images: List<Image>) {
        imageAdapter?.setData(images)
        setItemDecoration(imageColumns)
        recyclerView?.adapter = imageAdapter
    }

    private fun setUpFolderAdapter(folders: List<Folder>?) {
        if (folders != null) {
            folderAdapter?.setData(folders)
        }
        setItemDecoration(folderColumns)
        recyclerView?.adapter = folderAdapter

        if (foldersState != null) {
            layoutManager?.spanCount = folderColumns
            recyclerView?.layoutManager?.onRestoreInstanceState(foldersState)
        }
    }

    /**
     * Set item size, column size base on the screen orientation
     */
    private fun orientationBasedUI(orientation: Int) {
        imageColumns = if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) 3 else 5
        folderColumns = if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) 2 else 4

        val columns = if (isDisplayingFolderView) folderColumns else imageColumns
        layoutManager = GridLayoutManager(this, columns)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.setHasFixedSize(true)
        setItemDecoration(columns)
    }

    /**
     * Set item decoration
     */
    private fun setItemDecoration(columns: Int) {
        layoutManager?.spanCount = columns
        if (itemOffsetDecoration != null)
            recyclerView?.removeItemDecoration(itemOffsetDecoration)
        itemOffsetDecoration = GridSpacingItemDecoration(columns, resources.getDimensionPixelSize(R.dimen.item_padding), false)
        recyclerView?.addItemDecoration(itemOffsetDecoration)
    }

    /**
     * Check permission
     */
    private fun getDataWithPermission() {
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            presenter?.loadImages()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    RC_PERMISSION_WRITE_EXTERNAL_STORAGE)
        }
    }

    /**
     * Request for camera permission
     */
    private fun captureImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (rc == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                        RC_PERMISSION_CAMERA)
            }
        } else {
            captureImage()
        }
    }

    /**
     * Start camera intent
     * Create a temporary file and pass file Uri to camera intent
     */
    private fun captureImage() {
        if (!CameraHelper.checkCameraAvailability(this)) {
            return
        }
        presenter?.captureImage(this, RC_CAPTURE)
    }

    companion object {
        private val TAG = ImagePickerActivity::class.java.simpleName
        private val RC_CAPTURE = 2000
        val RC_PERMISSION_WRITE_EXTERNAL_STORAGE = 9653
        val RC_PERMISSION_CAMERA = 3524

        fun newIntent(context: Context, config: Configuration?): Intent {
            val intent = Intent(context, ImagePickerActivity::class.java)
            intent.putExtra(Configuration::class.java.simpleName, config)
            return intent
        }
    }

}
