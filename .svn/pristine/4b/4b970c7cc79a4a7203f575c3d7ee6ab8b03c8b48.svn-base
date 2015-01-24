package com.tencent.zebra.ui;
/**
 * Version 1.0
 *
 * Date: 2013-11-26 17:07
 * Author: yonnielu
 *
 * Copyright © 1998-2013 Tencent Technology (Shenzhen) Company Ltd.
 *
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.photoplus.R;
import com.tencent.zebra.util.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *  支持水平滚动按钮的view，可以自定义按钮的文本，图片，以及按钮对应的id
 */
public class HorizontalButtonView extends HorizontalScrollView implements View.OnClickListener {
    private static final String TAG = HorizontalButtonView.class.getSimpleName();
    private static final int SCROLL_DELAY = 100;

    private Context mContext;

//    private int mBtnBgResId;
//    private int mBtnPressedBgId;
    private SparseArrayCompat2<View> mButtons;

    private LinearLayout mRootContainer;
//    private float mBtnWidth;
//    private int mBtnMarginLeft;
//    private int mBtnMarginTop;
//    private int mBtnMarginRight;
//    private int mBtnMarginBottom;

    private int mItemLayoutId;

    public int mCurBtnId = -1;

    private int mBtnWidth, mBtnHeight;

    private int mHorizontalSpace;

    private int mGravity;

    private boolean mToggleSelf = false;

    private boolean mDistribute = false;

    private volatile boolean mClicking = false;

    int mLeftMargin = 0, mRightMargin = 0;

    private boolean mNeedSmoothScroll = true;
//    private View mCurView;
    private ButtonChangeListener mListener;
    private LayoutInflater mInflater;

    public HorizontalButtonView(Context context) {
        this(context, null);
    }

    public HorizontalButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mRootContainer = new LinearLayout(mContext);
        mRootContainer.setOrientation(LinearLayout.HORIZONTAL);
        mRootContainer.setGravity(Gravity.CENTER_VERTICAL);
        mButtons = new SparseArrayCompat2<View>();

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalButtonView, defStyle, 0);
            mItemLayoutId = array.getResourceId(R.styleable.HorizontalButtonView_itemLayoutId, 0);
            int textArrayResId = array.getResourceId(R.styleable.HorizontalButtonView_btnTextArray, 0);
            TypedArray textArray = textArrayResId != 0 ? mContext.getResources().obtainTypedArray(textArrayResId) : null;

            int imageArrayResId = array.getResourceId(R.styleable.HorizontalButtonView_btnImageArray, 0);
            TypedArray imageArray = imageArrayResId != 0 ? mContext.getResources().obtainTypedArray(imageArrayResId) : null;

            int idArrayResId = array.getResourceId(R.styleable.HorizontalButtonView_btnIdArray, 0);
            TypedArray idArray = idArrayResId != 0 ? mContext.getResources().obtainTypedArray(idArrayResId) : null;

            int gravityIndex = array.getInt(R.styleable.HorizontalButtonView_android_gravity, -1);
            setGravity(gravityIndex);

            mLeftMargin = array.getDimensionPixelSize(R.styleable.HorizontalButtonView_leftMargin, 0);
            mRightMargin = array.getDimensionPixelSize(R.styleable.HorizontalButtonView_rightMargin, 0);

            mBtnWidth = array.getDimensionPixelSize(R.styleable.HorizontalButtonView_btnWidth, -1);
            mBtnHeight = array.getDimensionPixelSize(R.styleable.HorizontalButtonView_btnHeight, -1);

            mHorizontalSpace = array.getDimensionPixelSize(R.styleable.HorizontalButtonView_horizontalSpace, 0);
            mDistribute = array.getBoolean(R.styleable.HorizontalButtonView_distribute, false);
//            mBtnWidth = array.getDimension(R.styleable.HorizontalButtonView_btnWidth, 0.0f);
//            mBtnMarginLeft = array.getLayoutDimension(R.styleable.HorizontalButtonView_btnMarginLeft, 0);
//            mBtnMarginTop = array.getLayoutDimension(R.styleable.HorizontalButtonView_btnMarginTop, 0);
//            mBtnMarginRight = array.getLayoutDimension(R.styleable.HorizontalButtonView_btnMarginRight, 0);
//            mBtnMarginBottom = array.getLayoutDimension(R.styleable.HorizontalButtonView_btnMarginBottom, 0);
//
//            mBtnBgResId = array.getResourceId(R.styleable.HorizontalButtonView_btnBg, 0);
//            mBtnPressedBgId = array.getResourceId(R.styleable.HorizontalButtonView_btnPressedBg, 0);

            if (textArray != null && textArray.length() > 0) {
                List<ViewModel> models = new ArrayList<ViewModel>();
                for (int i = 0; i < textArray.length(); i++) {
                    ViewModel model = new ViewModel();
                    model.id = idArray.getResourceId(i, 0);
                    model.label = (String) textArray.getText(i);
                    model.imageRes = imageArray.getResourceId(i, 0);
                    models.add(model);
                }
                init(models);
            }

            if (idArray != null) {
                idArray.recycle();
            }
            if (imageArray != null) {
                imageArray.recycle();
            }
            if (textArray != null) {
                textArray.recycle();
            }
            if (array != null) {
                array.recycle();
            }
        }
        FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        lp.leftMargin = mLeftMargin;
//        lp.rightMargin = mRightMargin;

        if (mGravity == -1) {
            lp.gravity = Gravity.CENTER_VERTICAL;
        } else {
            lp.gravity = mGravity;
        }
        addView(mRootContainer, lp);
    }

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.START;
            }

            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }

            mGravity = gravity;
        }
    }


    public void setItemLayout(int resId) {
        mItemLayoutId = resId;
    }

    public void init(List<ViewModel> models) {
        mRootContainer.removeAllViews();
        mButtons.clear();
        int distributeWidth = 0;

        if (mDistribute) {
            int size = models.size();
            int totalWidth = DeviceUtils.getScreenWidth(mContext) - mLeftMargin - mRightMargin - mHorizontalSpace * (size - 1);
            distributeWidth = (int) (totalWidth * 1.0f / size);
        }

        int maxIndex = models.size() - 1;
        for (int i = 0; i < models.size(); i++) {
            ViewModel model = models.get(i);
            ViewGroup container = (ViewGroup) mInflater.inflate(mItemLayoutId, null);

            container.setId(model.id == -1 ? i : model.id);

            mButtons.put(container.getId(), container);

            ImageView image = (ImageView) container.findViewById(R.id.image);
            if (image != null) {
                image.setImageResource(model.imageRes);
            }

            ImageView imageShuffle = (ImageView) container.findViewById(R.id.imageShuffle);
            if (imageShuffle != null && model.shuffle) {
            	imageShuffle.setVisibility(VISIBLE);
            }

            TextView text = (TextView) container.findViewById(R.id.text);
            if (text != null) {
                text.setText(model.label);
            }

            //mark by castiel cause V2.0 don't lock filter
//            ImageView lock = (ImageView) container.findViewById(R.id.lock_btn);
//            if (lock != null){
//                if (BitUtils.checkBit(model.mask, BitUtils.BIT_ONE)) {
//                    lock.setVisibility(View.VISIBLE);
//                } else {
//                    lock.setVisibility(View.GONE);
//                }
//            }

//            if (container.getId() != R.id.divider) {
//                container.setOnClickListener(this);
//            } else {
            container.setOnClickListener(this);
//                if (image != null) {
//                    ViewGroup.LayoutParams lp = image.getLayoutParams();
//                    if (lp instanceof RelativeLayout.LayoutParams) {
//                        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) lp;
//                        rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
//                        image.setPadding(0, 0, 0, 0);
//                    }
//                }
//            }
            container.setTag(model.tag);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (mBtnWidth > 0) {
                lp.width = mBtnWidth;
            }

            if (mDistribute) {
                lp.width = distributeWidth;
            }

            if (mBtnHeight > 0) {
                lp.height = mBtnHeight;
            }
            if (mHorizontalSpace > 0) {
                if(i == 0){
                    lp.leftMargin = mLeftMargin;
                }
                if(i != maxIndex){
                    lp.rightMargin = mHorizontalSpace;
                }
                else{
                    lp.rightMargin= mRightMargin;
                }
            }
            if (mDistribute) {
                lp.weight = 1;
            }
            mRootContainer.addView(container, lp);
        }
    }

    @Override
    public void onClick(View v) {
        if (mClicking || !isEnabled()) {
            return;
        }
        mClicking = true;
        int id = v.getId();

        if (mListener != null) {
            mListener.onButtonClick(v);
        }
         setButton(id, mButtons.get(id), false, true);
        if(mNeedSmoothScroll){
            smoothScrollToDisplay(v);
        }
        mClicking = false;
    }

    private void smoothScrollToDisplay(View view){
        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int visibleWidth = r.width();
        final int width = view.getWidth();
        final int left = r.left;
        if(visibleWidth < width){
            this.post(new Runnable() {
                @Override
                public void run() {
                    int offset = 0;
                    if(left > width){   //右侧未显示完全
                        offset = width + mLeftMargin + mRightMargin + mHorizontalSpace;
                    }
                    else{       //左侧未显示完全
                        offset = -width - mLeftMargin - mRightMargin - mHorizontalSpace;
                    }
                    smoothScrollBy(offset,0);
                }
            });
        }
    }
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < mRootContainer.getChildCount(); i++) {
            mRootContainer.getChildAt(i).setEnabled(enabled);
        }
    }

    public void reset() {
        mCurBtnId = -1;
    }

    /**
     * 对于当前已经选中的按钮，如果再次点击是否可以进入非选中状态
     *
     * @param toggleSelf
     */
    public void setToggleSelf(boolean toggleSelf) {
        mToggleSelf = toggleSelf;
    }

    public int getCurBtnId() {
    	return mCurBtnId;
    }

    /**
     * Set which button was selected
     *
     * @param indexOrId     This param may hold the index or the id
     * @param noCallback    Whether callback needed or not
     * @param isManual      Is this call manual
     * @param isId          Indicate indexOrId is id or not
     */
    public void setButton(int indexOrId, boolean noCallback, boolean isManual, boolean isId) {
        setButton(indexOrId, noCallback, isManual, isId, true);
    }


    public void setButton(final int indexOrId, boolean noCallback, boolean isManual, boolean isId, boolean animate) {
        int key = -1;
        if (isId) {
            key = indexOrId;
        } else {
            if (indexOrId >= 0) {
                key = mButtons.keyAt(indexOrId);
            }
        }
        final View view = mButtons.get(key);
        setButton(key, view, noCallback, isManual);
        if (animate) {
            if (view != null) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int offset = indexOrId > 0 ? view.getLeft() : 0;
                        HorizontalButtonView.this.smoothScrollTo(offset, 0);
                    }
                }, SCROLL_DELAY);
            } else {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HorizontalButtonView.this.smoothScrollTo(0, 0);
                    }
                }, SCROLL_DELAY);
            }
        }
    }

    private void setButton(int id, final View view, boolean noCallback, boolean isManual) {
        if (mCurBtnId == id && !mToggleSelf) {// 对于已经是当前的button，则不执行任何操作，产品的需求
            if (id == -1) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HorizontalButtonView.this.smoothScrollTo(0, 0);
                    }
                }, SCROLL_DELAY);
            }
            return;
        }

        if (view != null) {
            if (mListener != null && !noCallback) {
                if(!mListener.onButtonChanged(mCurBtnId, id, view)){
                	return;
                }
            }
            for (int i = 0; i < mButtons.size(); i++) {
                View v = mButtons.get(mButtons.keyAt(i));
                v.setSelected(v.getId() == id && mCurBtnId != v.getId());
            }

            mCurBtnId = mCurBtnId == id ? -1 : id;
        }
        if (isManual) return;

//        if (view != null) {
//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    int offset = view.getLeft();
//                    LogUtils.v(TAG, "setButton(), offset = %d", offset);
//                    HorizontalButtonView.this.smoothScrollTo(offset, 0);
//                }
//            }, SCROLL_DELAY);
//        } else {
//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    HorizontalButtonView.this.smoothScrollTo(0, 0);
//                }
//            }, SCROLL_DELAY);
//        }

    }

    public void setListener(ButtonChangeListener listener) {
        mListener = listener;
    }

    public SparseArrayCompat2<View> getButtons() {
        return mButtons;
    }

    public boolean hasSelectState(){
        if(mCurBtnId != -1){
            for (int i = 0; i < mButtons.size(); i++) {
                View v = mButtons.get(mButtons.keyAt(i));
                if(mCurBtnId == v.getId() && v.isSelected()){
                    return true;
                }
            }
        }
        return false;
    }

    public void clearSelectedState() {
        if (mListener != null) {
            mListener.onButtonChanged(mCurBtnId, -1, null);
        }
        for (int i = 0; i < mButtons.size(); i++) {
            View v = mButtons.get(mButtons.keyAt(i));
            v.setSelected(false);
        }
        mCurBtnId = -1;
    }

    public void setAllEnable(boolean enable){
    	for (int i = 0; i < mButtons.size(); i++) {
            View v = mButtons.get(mButtons.keyAt(i));
            v.setEnabled(enable);
        }
    }

    public void setEnableByID(int id, boolean enable){
    	ViewGroup v = (ViewGroup) mButtons.get(id);
    	v.findViewById(R.id.image).setEnabled(enable);
    	v.findViewById(R.id.text).setEnabled(enable);
    	v.setEnabled(enable);
    }

    public int getLeftMargin(){
        return mLeftMargin;
    }
    public int getRightMargin(){
        return mRightMargin;
    }
    /**
     * 设置是否水平方向平铺铺满
     *
     * @param value
     */
    public void setDistribute(boolean value) {
        mDistribute = value;
    }

    public static interface ButtonChangeListener {
        public boolean onButtonChanged(int oldId, int newId, View view);
        public void onButtonClick(View view);
    }
    public  void clearButtonState(int  oldId, int newId,View view){

            for(int i=0 ;i<getButtons().size();i++){
                Log.e("debug","clear"+i);
                ViewGroup viewGroup = (FrameLayout) getButtons().get(i);
                FrameLayout imgFrame = (FrameLayout) viewGroup.findViewById(R.id.image_frame);
                imgFrame.setForeground(null);
            }

        FrameLayout imgFrame = getViewById(newId);
        imgFrame.setForeground(getResources().getDrawable(R.drawable.filter_selected_bg) );



    }
    private FrameLayout  getViewById(int id){
        ViewGroup containner = (ViewGroup) getButtons().get(id);
        FrameLayout imgFrame = (FrameLayout) containner.findViewById(R.id.image_frame);
        return   imgFrame;
    }

}
