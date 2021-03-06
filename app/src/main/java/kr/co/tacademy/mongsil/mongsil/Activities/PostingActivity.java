package kr.co.tacademy.mongsil.mongsil.Activities;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import kr.co.tacademy.mongsil.mongsil.Utils.BitmapUtil;
import kr.co.tacademy.mongsil.mongsil.Fragments.BottomPicDialogFragment;
import kr.co.tacademy.mongsil.mongsil.Fragments.MiddleAloneDialogFragment;
import kr.co.tacademy.mongsil.mongsil.JSONParsers.NetworkDefineConstant;
import kr.co.tacademy.mongsil.mongsil.JSONParsers.Models.POIData;
import kr.co.tacademy.mongsil.mongsil.JSONParsers.Models.Post;
import kr.co.tacademy.mongsil.mongsil.Fragments.PostingPreviewDialogFragment;
import kr.co.tacademy.mongsil.mongsil.Fragments.ProgressDialogFragment;
import kr.co.tacademy.mongsil.mongsil.Managers.PropertyManager;
import kr.co.tacademy.mongsil.mongsil.R;
import kr.co.tacademy.mongsil.mongsil.Fragments.SearchPOIDialogFragment;
import kr.co.tacademy.mongsil.mongsil.JSONParsers.Models.TimeData;
import kr.co.tacademy.mongsil.mongsil.JSONParsers.Models.WeatherData;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostingActivity extends BaseActivity
        implements SearchPOIDialogFragment.OnPOISearchListener,
        BottomPicDialogFragment.OnBottomPicDialogListener {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    // 툴바 필드
    TextView tbLocation, tbSave;

    // 날씨
    ImageView imgPreview, leftWeather, rightWeather;
    ViewPager selectWeatherPager;

    // 배경
    ScrollView scrollPosting;
    ImageView imgPostingBackground;

    // 포스팅
    EditText editPosting;

    // 카메라
    ImageView imgCamera;

    Post modifingPost;

    private String area1 = "";
    private String area2 = "";

    private UpLoadValueObject upLoadFile = null;
    private int currentPos = 0;
    private Handler handler = new Handler();
    private boolean isDeleteProfileImg = false;
    private boolean isImageShow = false;

    class UpLoadValueObject {
        File file; //업로드할 파일
        boolean tempFiles; //임시파일 유무

        public UpLoadValueObject(File file, boolean tempFiles) {
            this.file = file;
            this.tempFiles = tempFiles;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        Intent intent = getIntent();
        // 툴바
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.icon_camera_close);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        tbLocation = (TextView) findViewById(R.id.text_posting_location);
        tbLocation.setText(getResources().getString(R.string.select_location));
        tbLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .add(SearchPOIDialogFragment.newInstance(), "search_posting").commit();
            }
        });
        tbSave = (TextView) findViewById(R.id.text_save);
        tbSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(getCurrentFocus() != null) {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        savePost();
                    }
                }, 500);
            }
        });

        // 미리보기
        imgPreview = (ImageView) findViewById(R.id.img_preview);
        imgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (upLoadFile == null) {
                    getSupportFragmentManager().beginTransaction()
                            .add(PostingPreviewDialogFragment.newInstance(
                                    area1,
                                    editPosting.getText().toString(),
                                    currentPos,
                                    null, null), "preview").commit();
                } else {
                    Bitmap imgPreview = BitmapFactory.decodeFile(upLoadFile.file.getAbsolutePath());
                    getSupportFragmentManager().beginTransaction()
                            .add(PostingPreviewDialogFragment.newInstance(
                                    area1,
                                    editPosting.getText().toString(),
                                    currentPos,
                                    imgPreview, null), "preview").commit();
                }
            }
        });

        // 백그라운드 이미지
        scrollPosting = (ScrollView) findViewById(R.id.scroll_posting);
        scrollPosting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        imgPostingBackground = (ImageView) findViewById(R.id.img_posting_background);

        // 날씨 선택
        selectWeatherPager =
                (ViewPager) findViewById(R.id.viewpager_posting_select_weather);
        selectWeatherPager.setAdapter(new WeatherPagerAdapter());
        selectWeatherPager.setPageTransformer(false, new NoPageTransformer());
        selectWeatherPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPos = position;
                if (upLoadFile == null) {
                    imgPostingBackground.setBackgroundResource(
                            WeatherData.imgFromWeatherCode(String.valueOf(position), 4));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        leftWeather = (ImageView) findViewById(R.id.img_left_weather);
        leftWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPos > 0 && currentPos < 15) {
                    selectWeatherPager.setCurrentItem(currentPos - 1);
                }
            }
        });
        rightWeather = (ImageView) findViewById(R.id.img_right_weather);
        rightWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPos >= 0 && currentPos <= 14) {
                    selectWeatherPager.setCurrentItem(currentPos + 1);
                }
            }
        });

        // 포스팅
        editPosting = (EditText) findViewById(R.id.edit_posting);
        editPosting.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    editPosting.setHint("");
                } else {
                    editPosting.setHint(getResources().getText(R.string.posting));
                }
            }
        });
        // 카메라
        imgCamera = (ImageView) findViewById(R.id.img_posting_camera);
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSDCardAvailable()) {
                    Toast.makeText(getApplicationContext(), "SD 카드가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                imageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "MongSil");
                if(!imageDir.exists()) {
                    imageDir.mkdirs();
                }

                if(!PropertyManager.getInstance().getWarning()) {
                    getSupportFragmentManager().beginTransaction()
                            .add(MiddleAloneDialogFragment.newInstance(100),
                                    "middle_warning").commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(BottomPicDialogFragment.newInstance(), "bottom_pic").commit();
                }
            }
        });

        if (intent.hasExtra("postdata")) {
            modifingPost = intent.getParcelableExtra("postdata");
            currentPos = modifingPost.getWeatherCode();
            modifyPosting();
        }
    }

    private void savePost() {
        String postContent = editPosting.getText().toString();
        if (tbLocation.getText().toString().isEmpty() ||
                tbLocation.getText().toString().equals
                        (getResources().getString(R.string.select_location))) {
            getSupportFragmentManager().beginTransaction()
                    .add(MiddleAloneDialogFragment.newInstance(11),
                            "middle_none_location").commit();
        } else if (postContent.isEmpty()) {
            getSupportFragmentManager().beginTransaction()
                    .add(MiddleAloneDialogFragment.newInstance(12),
                            "middle_none_post").commit();
            editPosting.requestFocus();
        } else {
            tbSave.setEnabled(false);
            // 글 쓸 때
            if (modifingPost == null) {
                new AsyncPostingRequest(upLoadFile).execute(
                        area1,  // 지역1
                        PropertyManager.getInstance().getUserId(), // 아이디
                        String.valueOf(currentPos), // 날씨 테마 코드
                        postContent    // 글 내용
                );
            } else { // 글 수정할 때
                tbLocation.setEnabled(false);
                new AsyncModifyPostingRequest(modifingPost.getBgImg(), upLoadFile).execute(
                        PropertyManager.getInstance().getUserId(), // 아이디
                        String.valueOf(currentPos), // 날씨 테마 코드
                        postContent     // 글 내용
                );
            }
        }
    }

    private String modifyPostId = null;

    private void modifyPosting() {
        modifyPostId = String.valueOf(modifingPost.getPostId());
        tbLocation.setEnabled(false);
        if (modifingPost.getArea2() == null) {
            tbLocation.setText(String.valueOf(modifingPost.getArea1()));
        } else {
            tbLocation.setText(String.valueOf(modifingPost.getArea1() + ", " + modifingPost.getArea2()));
        }

        if (modifingPost.getBgImg() != null) {
            Glide.with(this)
                    .load(modifingPost.getBgImg())
                    .into(new ViewTarget<ImageView, GlideDrawable>(imgPostingBackground) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation anim) {
                            // Set your resource on myView and/or start your animation here.
                            imgPostingBackground.setBackgroundDrawable(resource);
                        }
                    });
        }
        selectWeatherPager.setCurrentItem(modifingPost.getWeatherCode());
        editPosting.setText(modifingPost.getContent());
        imgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modifingPost.getBgImg() == null) {
                    getSupportFragmentManager().beginTransaction()
                            .add(PostingPreviewDialogFragment.newInstance(
                                    area1,
                                    editPosting.getText().toString(),
                                    currentPos,
                                    null, null), "preview").commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(PostingPreviewDialogFragment.newInstance(
                                    area1,
                                    editPosting.getText().toString(),
                                    currentPos,
                                    null, modifingPost.getBgImg()), "preview").commit();
                }
            }
        });
    }

    // 날씨 뷰페이저 어뎁터
    private class WeatherPagerAdapter extends PagerAdapter {

        WeatherPagerAdapter() {
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 14;
        }

        ImageView imgWeatherIcon;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view =
                    getLayoutInflater().inflate(R.layout.layout_posting_select_weather, container, false);

            imgWeatherIcon = (ImageView) view.findViewById(R.id.img_weather_icon);
            imgWeatherIcon.setImageResource(
                    WeatherData.imgFromWeatherCode(String.valueOf(position), 0));
            imgWeatherIcon.setAnimation(AnimationApplyInterpolater(
                    R.anim.bounce_interpolator, new LinearInterpolator()));
            AnimationDrawable animation =
                    (AnimationDrawable) imgWeatherIcon.getDrawable();
            if (animation != null) {
                animation.start();
            }

            ((ViewPager) container).addView(view, 0);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }
    }

    private static class NoPageTransformer implements ViewPager.PageTransformer {
        public void transformPage(View view, float position) {
            if (position < 0) {
                view.setScrollX((int) ((float) (view.getWidth()) * position));
            } else if (position > 0) {
                view.setScrollX(-(int) ((float) (view.getWidth()) * -position));
            } else {
                view.setScrollX(0);
            }
        }
    }

    // 애니메이션 인터폴레이터 적용
    private Animation AnimationApplyInterpolater(
            int resourceId, final Interpolator interpolator) {
        Animation animation = AnimationUtils.loadAnimation(this, resourceId);
        animation.setInterpolator(interpolator);
        return animation;
    }

    /**
     * 카메라에서 이미지 가져오기
     */
    Uri currentSelectedUri; //업로드할 현재 이미지에 대한 Uri
    File imageDir; //카메라로 찍은 사진을 저장할 디렉토리
    String currentFileName;  //파일이름

    private void doTakePhotoAction() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //업로드할 파일의 이름
        currentFileName = "upload_" + String.valueOf(System.currentTimeMillis() / 1000) + ".jpg";
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentSelectedUri);
        startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
    }

    /**
     * 앨범에서 이미지 가져오기
     */
    private void doTakeAlbumAction() {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_ALBUM:
                currentSelectedUri = data.getData();
                if (currentSelectedUri != null) {
                    //실제 Image의 full path name을 얻어온다.
                    if (findImageFileNameFromUri(currentSelectedUri)) {
                        currentFileName = getRealPathFromURI(currentSelectedUri);
                        Bitmap scaledBitmap = BitmapUtil.SafeDecodeBitmapFile(currentFileName, "");
                        backGroundFileUpload = tempSavedBitmapFile(scaledBitmap, 1);
                        upLoadFile = new UpLoadValueObject(backGroundFileUpload, true);
                    } else {
                        Bundle extras = data.getExtras();
                        Bitmap returnedBitmap = (Bitmap) extras.get("data");
                        if (tempSavedBitmapFile(returnedBitmap)) {
                            Log.e("임시이미지파일저장", "저장됨");
                        } else {
                            Log.e("임시이미지파일저장", "실패");
                        }
                    }
                }
                isImageShow = true;
                if(backGroundFileUpload != null) {
                    Glide.with(this)
                            .load(backGroundFileUpload)
                            .into(new ViewTarget<ImageView, GlideDrawable>(imgPostingBackground) {
                                @Override
                                public void onResourceReady(GlideDrawable resource, GlideAnimation anim) {
                                    imgPostingBackground.setBackgroundDrawable(resource);
                                }
                            });
                }
                break;
            case PICK_FROM_CAMERA: {
                //카메라캡쳐를 이용해 가져온 이미지
                currentSelectedUri = data.getData();
                if (currentSelectedUri != null) {
                    currentFileName = getRealPathFromURI(currentSelectedUri);
                    Bitmap scaledBitmap = BitmapUtil.SafeDecodeBitmapFile(currentFileName, "");
                    backGroundFileUpload = tempSavedBitmapFile(scaledBitmap, 1);
                    upLoadFile = new UpLoadValueObject(backGroundFileUpload, true);
                }
                isImageShow = true;
                Glide.with(this)
                        .load(currentSelectedUri)
                        .into(new ViewTarget<ImageView, GlideDrawable>(imgPostingBackground) {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation anim) {
                                imgPostingBackground.setBackgroundDrawable(resource);
                            }
                        });
                break;
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
/*

    private File cameraResizingFile(final File file) {
        // 이미지 4배 압축 및 카메라일 경우 로테이션 돌림
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap orgImage = BitmapFactory.decodeFile(file.getPath(), options);
        Bitmap resize = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        resize = Bitmap.createBitmap(
                orgImage, 0, 0, orgImage.getWidth(), orgImage.getHeight(), matrix, true);
        // 리사이즈된 이미지 구함
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if(resize != null) {
                resize.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            return file;
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
*/


    private boolean tempSavedBitmapFile(Bitmap tempBitmap) {
        boolean flag = false;
        try {
            currentFileName = "upload_" + (System.currentTimeMillis() / 1000);
            String fileSuffix = ".jpg";
            //임시파일을 실행한다.
            File tempFile = File.createTempFile(
                    currentFileName,            // prefix
                    fileSuffix,                   // suffix
                    imageDir                   // directory
            );
            final FileOutputStream bitmapStream = new FileOutputStream(tempFile);
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
            upLoadFile = new UpLoadValueObject(tempFile, true);
            if (bitmapStream != null) {
                bitmapStream.close();
            }
            currentSelectedUri = Uri.fromFile(tempFile);
            flag = true;
        } catch (IOException i) {
            Log.e("저장중 문제발생", i.toString());
        }
        return flag;
    }

    File backGroundFileUpload;

    private  File tempSavedBitmapFile(Bitmap tempBitmap, int aaa) {

        File tempFile = null;
        try {
            currentFileName = "upload_back_" + (System.currentTimeMillis() / 1000);
            String fileSuffix = ".jpg";
            if(!imageDir.exists()) {
                imageDir.mkdirs();
            }
            //임시파일을 실행한다.
            Log.e("asdf", imageDir +"");
            tempFile = File.createTempFile(
                    currentFileName,            // prefix
                    fileSuffix,                   // suffix
                    imageDir                   // directory
            );
            Log.e("asdf", tempFile.getAbsolutePath()+"");
            final FileOutputStream bitmapStream = new FileOutputStream(tempFile);
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bitmapStream);
            if (bitmapStream != null) {
                bitmapStream.close();
            }

            return tempFile;
        } catch (IOException iii) {
            Log.e("저장중 문제발생!!", iii.toString(), iii);
        }
        return null;
    }

    private boolean findImageFileNameFromUri(Uri tempUri) {
        boolean flag = false;

        //실제 Image Uri의 절대이름
        String[] IMAGE_DB_COLUMN = {MediaStore.Images.ImageColumns.DATA};
        Cursor cursor = null;
            try {
                //Primary Key값을 추출
                String imagePK = String.valueOf(ContentUris.parseId(tempUri));
                //Image DB에 쿼리를 날린다.

                cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_DB_COLUMN,
                        MediaStore.Images.Media._ID + "=?",
                        new String[]{imagePK}, null);

                if(cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        currentFileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                        Log.e("fileName", String.valueOf(currentFileName));
                        flag = true;
                    }
                }
            } catch (SQLiteException sqle) {
                Log.e("findImage....", sqle.toString(), sqle);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        return flag;
    }

    // 이미지 편집 다이어로그 셀렉터 인터페이스에서 받음
    @Override
    public void onSelectBottomPic(int select) {
        switch (select) {
            case 0:
                doTakePhotoAction();
                break;
            case 1:
                doTakeAlbumAction();
                break;
            case 2:
                if(isImageShow) {
                    isDeleteProfileImg = true;
                    isImageShow = false;
                }
                upLoadFile = null;
                imgPostingBackground.setBackgroundResource(
                        WeatherData.imgFromWeatherCode(String.valueOf(currentPos), 4));
                break;
        }
    }

    // 포스팅 요청
    public class AsyncPostingRequest extends AsyncTask<String, String, String> {

        //업로드할 Mime Type 설정
        private final MediaType IMAGE_MIME_TYPE = MediaType.parse("image/*");

        private UpLoadValueObject object;

        AsyncPostingRequest(UpLoadValueObject object) {
            this.object = object;
        }

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getSupportFragmentManager().beginTransaction()
                    .add(progressDialogFragment, "progress_dialog").commit();
        }

        @Override
        protected String doInBackground(String... args) {
            Response response = null;
            Request request = null;
            try {
                OkHttpClient toServer = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();

                // 이미지 업로드 코드 설정
                String uploadCode = "1";
                if (object == null) {
                    uploadCode = "0";
                }

                Log.e("업로드코드", uploadCode);
                // 이미지가 있을 경우 MultipartBody
                if (uploadCode.equals("1")) {
                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    builder.setType(MultipartBody.FORM);
                    builder.addFormDataPart("uploadCode", uploadCode);
                    builder.addFormDataPart("area1", args[0]);
                    builder.addFormDataPart("userId", args[1]);
                    builder.addFormDataPart("weatherCode", args[2]);
                    builder.addFormDataPart("content", args[3]);
                    builder.addFormDataPart("date", TimeData.getNow());
                    if (area2 != null) {
                        if (!area2.equals("null")) {
                            builder.addFormDataPart("area2", area2);
                        }
                    }
                    if (object != null) {
                        File file = object.file;
                        builder.addFormDataPart("bgImg", file.getName(), RequestBody.create(IMAGE_MIME_TYPE, file));
                    }

                    RequestBody requestBody = builder.build();

                    //요청 세팅
                    request = new Request.Builder()
                            .url(NetworkDefineConstant.POST_SERVER_POST)
                            .post(requestBody)
                            .build();
                } else { // 이미지가 없을 경우 formBody
                    RequestBody formBody = null;
                    if (area2 != null) {
                        if (!area2.equals("null")) {
                            formBody = new FormBody.Builder()
                                    .add("uploadCode", uploadCode)
                                    .add("area1", args[0])
                                    .add("userId", args[1])
                                    .add("weatherCode", args[2])
                                    .add("content", args[3])
                                    .add("area2", area2)
                                    .add("date", TimeData.getNow())
                                    .build();
                        }
                    } else {
                        formBody = new FormBody.Builder()
                                .add("uploadCode", uploadCode)
                                .add("area1", args[0])
                                .add("userId", args[1])
                                .add("weatherCode", args[2])
                                .add("content", args[3])
                                .add("date", TimeData.getNow())
                                .build();
                    }
                    //요청 세팅
                    request = new Request.Builder()
                            .url(NetworkDefineConstant.POST_SERVER_POST)
                            .post(formBody)
                            .build();
                }

                response = toServer.newCall(request).execute();
                Log.e("요청 보내기 완료", "");
                boolean flag = response.isSuccessful();
                //응답 코드 200등등
                int responseCode = response.code();
                Log.e("응답코드 : ", responseCode + " ");
                if (flag) {
                    Log.e("response결과", responseCode + "---" + response.message()); //읃답에 대한 메세지(OK)
                    Log.e("response응답바디", response.body().string()); //json으로 변신
                    return "success";
                }
            } catch (UnknownHostException une) {
                Log.e("une", une.toString());
            } catch (UnsupportedEncodingException uee) {
                Log.e("uee", uee.toString());
            } catch (Exception e) {
                Log.e("e", e.toString());
            } finally {
                if (response != null) {
                    response.close();
                }
            }

            return "fail";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialogFragment.dismiss();
                }
            }, 1000);
            if (result.equalsIgnoreCase("success")) {
                Intent intent = new Intent(PostingActivity.this, MainActivity.class);
                intent.putExtra("area1", area1);
                setResult(RESULT_OK, intent);
                finish();
                if (upLoadFile != null) {
                    if (upLoadFile.tempFiles) {
                        upLoadFile.file.deleteOnExit(); //임시파일을 삭제한다
                    }
                }
                if (!PropertyManager.getInstance().getSaveGallery()) {
                    File f = new File(upLoadFile.file.getPath());
                    if (f != null) {
                        f.deleteOnExit();
                    }
                }
            } else if (result.equalsIgnoreCase("fail")) {
                getSupportFragmentManager().beginTransaction()
                        .add(MiddleAloneDialogFragment.newInstance(2), "middle_post_fail").commit();
            }
            tbSave.setEnabled(true);
        }
    }

    // 포스팅 수정 요청
    public class AsyncModifyPostingRequest extends AsyncTask<String, String, String> {
        //업로드할 Mime Type 설정
        private final MediaType IMAGE_MIME_TYPE = MediaType.parse("image/*");

        String beforeBgImg = null;
        UpLoadValueObject object;

        AsyncModifyPostingRequest(String beforeBgImg, UpLoadValueObject object) {
            if (beforeBgImg != null) {
                this.beforeBgImg = beforeBgImg;
            }
            this.object = object;
        }

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getSupportFragmentManager().beginTransaction()
                    .add(progressDialogFragment, "progress_dialog").commit();
        }

        @Override
        protected String doInBackground(String... args) {

            Response response = null;

            try {
                OkHttpClient toServer = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                String uploadCode;

                if (beforeBgImg != null && !beforeBgImg.equals("null")) {
                    uploadCode = "2";
                    if (object == null) {
                        if(isImageShow) {
                            uploadCode = "3";
                        } else {
                            uploadCode = "0";
                        }
                    }
                } else {
                    uploadCode = "1";
                    if (object == null) {
                        uploadCode = "0";
                    }
                }

                Request request = null;
                Log.e("업로드코드", uploadCode);
                // 이미지가 있을 경우 MultipartBody
                if (uploadCode.equals("1") || uploadCode.equals("2")) {
                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    builder.setType(MultipartBody.FORM);
                    builder.addFormDataPart("uploadCode", uploadCode);
                    builder.addFormDataPart("userId", args[0]);
                    builder.addFormDataPart("weatherCode", args[1]);
                    builder.addFormDataPart("content", args[2]);
                    builder.addFormDataPart("date", TimeData.getNow());
                    if (object != null) {
                        File file = object.file;
                        builder.addFormDataPart("bgImg", file.getName(), RequestBody.create(IMAGE_MIME_TYPE, file));
                    }

                    RequestBody requestBody = builder.build();

                    //요청 세팅
                    request = new Request.Builder()
                            .url(String.format(NetworkDefineConstant.PUT_SERVER_POST,
                                    modifyPostId))
                            .put(requestBody)
                            .build();
                } else { // 이미지가 없을 경우 formBody
                    RequestBody formBody = new FormBody.Builder()
                            .add("uploadCode", uploadCode)
                            .add("userId", args[0])
                            .add("weatherCode", args[1])
                            .add("content", args[2])
                            .add("date", TimeData.getNow())
                            .build();
                    //요청 세팅
                    request = new Request.Builder()
                            .url(String.format(NetworkDefineConstant.PUT_SERVER_POST, modifyPostId))
                            .put(formBody)
                            .build();
                }

                response = toServer.newCall(request).execute();
                boolean flag = response.isSuccessful();
                //응답 코드 200등등
                int responseCode = response.code();
                if (flag) {
                    Log.e("response결과", responseCode + "---" + response.message()); //읃답에 대한 메세지(OK)
                    Log.e("response응답바디", response.body().string()); //json으로 변신
                    return "success";
                }
            } catch (UnknownHostException une) {
                Log.e("une", une.toString());
            } catch (UnsupportedEncodingException uee) {
                Log.e("uee", uee.toString());
            } catch (Exception e) {
                Log.e("e", e.toString());
            } finally {
                if (response != null) {
                    response.close();
                }
            }

            return "fail";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialogFragment.dismiss();
                }
            }, 1000);
            if (result.equalsIgnoreCase("success")) {
                Intent intent = new Intent(PostingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                if (upLoadFile != null) {
                    if (upLoadFile.tempFiles) {
                        upLoadFile.file.deleteOnExit(); //임시파일을 삭제한다
                    }
                }
                if (!PropertyManager.getInstance().getSaveGallery()) {
                    File f = new File(upLoadFile.file.getPath());
                    if (f != null) {
                        f.deleteOnExit();
                    }
                }
            } else if (result.equalsIgnoreCase("fail")) {
                getSupportFragmentManager().beginTransaction()
                        .add(MiddleAloneDialogFragment.newInstance(2), "middle_post_fail").commit();
            }
            tbSave.setEnabled(true);
            tbLocation.setEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // POI 검색
    @Override
    public void onPOISearch(POIData poiData) {
        if (poiData != null) {
            area1 = poiData.getUpperAddrName();
            area2 = poiData.getMiddleAddrName();

            if (area2 != null) {
                if (!area2.equals("null")) {
                    tbLocation.setText(String.valueOf(area1 + ", " + area2));
                }
            } else {
                tbLocation.setText(area1);
            }
        }
    }

    public boolean isSDCardAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
