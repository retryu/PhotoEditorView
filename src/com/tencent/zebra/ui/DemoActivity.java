package com.tencent.zebra.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


//import com.tencent.cameraui.MqqCameraActivity;
//import com.tencent.zebra.util.Util;
import com.tencent.zebra.crop.CropImageActivity;
import com.tencent.zebra.doodle.DoodleActivity;
import com.tencent.zebra.doodle.MyActivity;
import com.tencent.zebra.effect.PhotoEffectActivity;
import com.tencent.zebra.util.log.ZebraLog;
import com.tencent.photoplus.R;

/**
 * Created by yonnielu on 13-5-26.
 */
public class DemoActivity extends ZebraBaseActivity {
	private static final String TAG = DemoActivity.class.getSimpleName();
	
	public static int FROM_PIC_CROP = 2000;
    public static int FROM_PIC_DOODLE = 2001;
    public static int FROM_PIC_BEAUTY = 2002;

//	private Button mButton;
	private Button mButton2;
	private ImageView mImage;
	private Context mContext;
	private Button mButton3;

	//You can specify any code here, not only 0x00090
	public static final int REQ_START_WATERMARK_CAMERA = 0x00090;
	public static final int REQ_START_PHOTO_EDIT = 0x00091;
	private int act_type;

    private Bitmap mBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getThisActivity();
		setContentView(R.layout.zebra_demo);
//		mButton = (Button) findViewById(android.R.id.button1);
		mImage = (ImageView) findViewById(R.id.image);
//		mButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(final View v) {
//				ZebraLog.d(TAG, "CameraMainActivity onCreate onClick:");
//				ZebraLog.d("STARTUPTIMELOG", "[STARTUPTIMELOG] DemoActivity mButton onClick, time=" + System.currentTimeMillis());
//				v.setClickable(false);
//				// 避免快速点击，重复进入，延迟一定时间，恢复按钮可点
//				new Handler().postDelayed(new Runnable() {
//
//					@Override
//					public void run() {
//						v.setClickable(true);
//					}
//				}, 1000 * 5);
//				
//				Intent intent = new Intent();
//				intent.setClass(mContext, MqqCameraActivity.class);
//				intent.putExtra(MqqCameraActivity.KEY_LOAD_FROM_THIRD_APP, true);
//				startActivityForResult(MqqCameraActivity.class, intent, REQ_START_WATERMARK_CAMERA);
//			}
//		});
		
		mButton2 = (Button) findViewById(R.id.button2);
		mButton2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
			    act_type = FROM_PIC_CROP;
				Intent intent = new Intent();
	            intent.setType("image/*");
	            intent.setAction(Intent.ACTION_GET_CONTENT);
	            intent = Intent.createChooser(intent, getResources().getString(R.string.zebra_choose_picture));
	            startActivityForResult(intent, REQ_START_PHOTO_EDIT);
			}
		});
		
		mButton3 = (Button) findViewById(R.id.button3);
        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                act_type = FROM_PIC_DOODLE;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, getResources().getString(R.string.zebra_choose_picture));
                startActivityForResult(intent, REQ_START_PHOTO_EDIT);
            }
        });
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                act_type = FROM_PIC_BEAUTY;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, getResources().getString(R.string.zebra_choose_picture));
                startActivityForResult(intent, REQ_START_PHOTO_EDIT);
            }
        });
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQ_START_WATERMARK_CAMERA:
//				switch (resultCode) {
//					case RESULT_OK:
//						if (data != null) {
//							Uri uri = data.getData();
//							String imagePath = uri.getPath();
//							Toast.makeText(getApplicationContext(), "IMAGEPATH:" + imagePath, Toast.LENGTH_SHORT).show();
//                            Bitmap newBitmap = Util.decodeBitmap(imagePath, 320, 320);
//							mImage.setImageBitmap(newBitmap);
//                            if (mBitmap != null && !mBitmap.isRecycled()) {
//                                mBitmap.recycle();
//                                System.gc();
//                            }
//                            mBitmap = newBitmap;
//							ZebraLog.d(TAG, "imagePath:" + imagePath);
//						}
//						break;
//				}
				break;
			case REQ_START_PHOTO_EDIT: {
				if(resultCode == RESULT_OK) {
                	Uri uri = data.getData();
                	if(uri != null) {
//						Intent intent = new Intent(getThisActivity(), //CameraMainActivity.class);
//								ShowImageActivity.class);
						String path = "";
						Cursor cursor = this.getContentResolver().query(uri,
								new String[] { MediaStore.Images.Media.DATA },
								null, null, null);
						if (cursor != null) {
							int index = cursor
									.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
							cursor.moveToFirst();
							path = cursor.getString(index);
						} else {
							path = uri.getPath();
						}
//						intent.putExtra("image_path", path);
//						startActivityForResult(ShowImageActivity.class, intent, 4);
						dealAct(path);
                	}
				}
			}
//			case MqqCameraActivity.CAMERA_FROM_SELECTPHOTO_TO_CROP: {
//			    if (data != null && data.getExtras() != null) {
//			        String imagePath = data.getExtras().getString("image_path");
//			        if (!TextUtils.isEmpty(imagePath)) {
//			            Toast.makeText(getThisActivity(), "IMAGEPATH:" + imagePath, Toast.LENGTH_SHORT).show();
//	                    mImage.setImageBitmap(Util.decodeBitmap(imagePath, 320, 320));
//	                    ZebraLog.d(TAG, "imagePath:" + imagePath);
//			        }
//			    }
//			}
		}
	}
	
	private void dealAct(String mImagePath) {
	    if (act_type == FROM_PIC_CROP) {
	        dealImageCrop(mImagePath);
	    } else if (act_type == FROM_PIC_DOODLE) {
	        doodleImage(mImagePath);
	    }
        else  if(act_type == FROM_PIC_BEAUTY){
            beautyImage(mImagePath);
        }
	}
	
    private void dealImageCrop(String mImagePath) {
        if (mImagePath == null) {
            return;
        }
        Intent intent = new Intent(getThisActivity(), CropImageActivity.class);
        // // here you have to pass absolute path to your file
//        Uri mUri = Uri.fromFile(new File(mImagePath));
        intent.putExtra("image_path", mImagePath);
        intent.putExtra("scale", true);
        startActivityForResult(CropImageActivity.class, intent, FROM_PIC_CROP);
    }
    
    private void doodleImage(String mImagePath){
        Intent intent = new Intent(getThisActivity(), DoodleActivity.class);
//        Uri mUri = Uri.fromFile(new File(mImagePath));
        intent.putExtra("image_path", mImagePath);//mUri.getPath());
        intent.putExtra("scale", true);
        startActivityForResult(DoodleActivity.class, intent, FROM_PIC_DOODLE);
    }
    private void beautyImage(String mImagePath){
        Intent intent = new Intent(getThisActivity(), PhotoEffectActivity.class);
//        Uri mUri = Uri.fromFile(new File(mImagePath));
        intent.putExtra("image_path", mImagePath);//mUri.getPath());
        intent.putExtra("scale", true);
        startActivityForResult(PhotoEffectActivity.class, intent, FROM_PIC_BEAUTY);
//        startActivity(intent);
    }
}
