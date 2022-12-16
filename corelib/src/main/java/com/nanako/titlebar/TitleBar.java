package com.nanako.titlebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nanako.R;

import java.util.HashMap;
import java.util.Map;

public class TitleBar extends FrameLayout implements View.OnClickListener {

    private final String TAG = getClass().getName();
    private Map<Integer, Long> mViewClicked = new HashMap<>();

    private Listener mListener;

    private View mViewStatus;

    private ViewGroup mVgTitle;

    private TextView mTxtTitle;
    private TextView mTvSubTitle;
    private ViewGroup mVgTitleLayout;
    private ViewGroup mVgSubTitleLayout;
    private ViewGroup mVgTitleBox;

    private ViewGroup mVgLeft;
    private TextView mTxtLeft;
    private ImageView mImgLeft;

    private ViewGroup mVgRight;
    private TextView mTxtRight;
    private ImageView mImgRight;

    private View mVBottomLine;

    private int mFastClickDuration = 500;

    public TitleBar(Context context) {
        super(context);
        init(null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);

    }

    private void init(AttributeSet attrs) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.tb_view_titlebar, this, true);

        mViewStatus = view.findViewById(R.id.view_status);
        mVgTitle = (ViewGroup) view.findViewById(R.id.layout_title);
        mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
        mVgLeft = (ViewGroup) view.findViewById(R.id.layout_left);
        mTxtLeft = (TextView) view.findViewById(R.id.txt_left);
        mImgLeft = (ImageView) view.findViewById(R.id.img_left);
        mVgRight = (ViewGroup) view.findViewById(R.id.layout_right);
        mTxtRight = (TextView) view.findViewById(R.id.txt_right);
        mImgRight = (ImageView) view.findViewById(R.id.img_right);
        mTvSubTitle = (TextView) view.findViewById(R.id.tv_sub_title);
        mVgTitleLayout = (ViewGroup) view.findViewById(R.id.layout_main_title);
        mVgSubTitleLayout = (ViewGroup) view.findViewById(R.id.layout_sub_title);
        mVgTitleBox = (ViewGroup) view.findViewById(R.id.vg_title_box);
        mVBottomLine = view.findViewById(R.id.v_bottom_line);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TitleBar);

        Drawable sbg = typedArray.getDrawable(R.styleable.TitleBar_tb_status_background);
        if (sbg != null) {
            mViewStatus.setBackgroundDrawable(sbg);
        }
        mViewStatus.setVisibility(typedArray.getBoolean(R.styleable.TitleBar_tb_status_visible,
                false) ? View.VISIBLE : View.GONE);
        if (!adjustStatusHeight()) {
            int sheight = typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_status_height,
                    -1);
            if (sheight == -1) {
                sheight = (int) getContext().getResources().getDimension(R.dimen.tb_status_height);
            }
            ViewGroup.LayoutParams paramsSh = mViewStatus.getLayoutParams();
            paramsSh.height = sheight;
            mViewStatus.setLayoutParams(paramsSh);
        }

        Drawable bg = typedArray.getDrawable(R.styleable.TitleBar_tb_backgroupd);
        if (bg != null) {
            view.setBackgroundDrawable(bg);
        }

        int defaultColor = getResources().getColor(android.R.color.white);

        mTxtTitle.setText(typedArray.getString(R.styleable.TitleBar_tb_title));
        mTxtTitle.setTextColor(typedArray.getColor(R.styleable.TitleBar_tb_title_color,
                defaultColor));
        mTxtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                typedArray.getDimension(R.styleable.TitleBar_tb_title_textsize, 55));
        mTxtTitle.setVisibility(typedArray.getBoolean(R.styleable.TitleBar_tb_title_visible,
                true) ? View.VISIBLE : View.INVISIBLE);
        boolean isBold = typedArray.getBoolean(R.styleable.TitleBar_tb_title_bold, true);
        if (isBold) {
            mTxtTitle.setTypeface(null, Typeface.BOLD);
        }
        mVgSubTitleLayout.setVisibility(typedArray.getBoolean(R.styleable.TitleBar_tb_sub_title_visible,
                false) ? VISIBLE : GONE);
        mTvSubTitle.setText(typedArray.getString(R.styleable.TitleBar_tb_sub_title));
        int defaultSubTitleColor = Color.parseColor("#999999");
        mTvSubTitle.setTextColor(typedArray.getColor(R.styleable.TitleBar_tb_sub_title_color,
                defaultSubTitleColor));
        mTvSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                typedArray.getDimension(R.styleable.TitleBar_tb_sub_title_size,
                        22));
        mVgLeft.setVisibility(typedArray.getBoolean(R.styleable.TitleBar_tb_left_visible,
                false) ? View.VISIBLE : View.GONE);
        mTxtLeft.setText(typedArray.getString(R.styleable.TitleBar_tb_left_text));
        mTxtLeft.setTextColor(typedArray.getColor(R.styleable.TitleBar_tb_left_text_color,
                defaultColor));
        mTxtLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                typedArray.getDimension(R.styleable.TitleBar_tb_left_text_size, 22));
        if (typedArray.getBoolean(R.styleable.TitleBar_tb_left_text_bold, false)) {
            mTxtLeft.setTypeface(null, Typeface.BOLD);
        }
        LinearLayout.LayoutParams tvLeftParams =
                (LinearLayout.LayoutParams) mTxtLeft.getLayoutParams();
        tvLeftParams.leftMargin =
                (int) typedArray.getDimension(R.styleable.TitleBar_tb_left_text_margin_left,
                        0);
        mTxtLeft.setLayoutParams(tvLeftParams);

        Drawable drawableLeft = typedArray.getDrawable(R.styleable.TitleBar_tb_left_image);
        mImgLeft.setImageDrawable(drawableLeft);
        mImgLeft.setVisibility(typedArray.getBoolean(R.styleable.TitleBar_tb_left_image_visible,
                false) ? View.VISIBLE : View.GONE);

        int leftImageMarginLeft =
                (int) typedArray.getDimension(R.styleable.TitleBar_tb_left_image_margin_left,
                        0);
        if (leftImageMarginLeft != 0) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) mImgLeft.getLayoutParams();
            params.leftMargin = leftImageMarginLeft;
            mImgLeft.setLayoutParams(params);
        }

        int leftMargin = typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_left_margin, 0);
        if (leftMargin != 0) {
            LayoutParams params = (LayoutParams) mVgLeft.getLayoutParams();
            params.leftMargin = leftMargin;
            mVgLeft.setLayoutParams(params);
        }

        mVgRight.setVisibility(typedArray.getBoolean(R.styleable.TitleBar_tb_right_visible,
                false) ? View.VISIBLE : View.GONE);
        mTxtRight.setText(typedArray.getString(R.styleable.TitleBar_tb_right_text));
        mTxtRight.setTextColor(typedArray.getColor(R.styleable.TitleBar_tb_right_text_color,
                defaultColor));
        mTxtRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                typedArray.getDimension(R.styleable.TitleBar_tb_right_text_size, 22));
        mImgRight.setVisibility(typedArray.getBoolean(R.styleable.TitleBar_tb_right_image_visible
                , false) ? View.VISIBLE : View.GONE);
        mImgRight.setImageDrawable(typedArray.getDrawable(R.styleable.TitleBar_tb_right_image));

        int rightMargin = typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_right_margin, 0);
        if (rightMargin != 0) {
            LayoutParams params = (LayoutParams) mVgRight.getLayoutParams();
            params.rightMargin = rightMargin;
            mVgRight.setLayoutParams(params);
        }
        int theight = typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_title_height, -1);
        if (theight == -1) {
            theight = (int) getContext().getResources().getDimension(R.dimen.tb_title_height);
        }
        int titleMarginHorizontal =
                typedArray.getDimensionPixelSize(R.styleable.TitleBar_tb_title_margin_horizontal,
                        0);
        LinearLayout.LayoutParams paramsTh = (LinearLayout.LayoutParams) mVgTitle.getLayoutParams();
        paramsTh.height = theight;
        mVgTitle.setLayoutParams(paramsTh);

        LayoutParams titleBoxParams = (LayoutParams) mVgTitleBox.getLayoutParams();
        titleBoxParams.leftMargin = titleMarginHorizontal;
        titleBoxParams.rightMargin = titleMarginHorizontal;
        mVgTitleBox.setLayoutParams(titleBoxParams);

        mVgLeft.setMinimumWidth(theight);
        mVgRight.setMinimumWidth(theight);
        int titleMarginTop = (int) typedArray.getDimension(R.styleable.TitleBar_tb_title_margin_top,
                1);
        int subTitleMarginTop =
                (int) typedArray.getDimension(R.styleable.TitleBar_tb_sub_title_margin_top,
                        1);
        LinearLayout.LayoutParams titleParams =
                (LinearLayout.LayoutParams) mVgTitleLayout.getLayoutParams();
        titleParams.topMargin = titleMarginTop;
        titleParams = (LinearLayout.LayoutParams) mVgSubTitleLayout.getLayoutParams();
        titleParams.topMargin = subTitleMarginTop;

        boolean showBottomLine = typedArray.getBoolean(R.styleable.TitleBar_tb_bottom_line_visible,
                false);
        mVBottomLine.setVisibility(showBottomLine ? VISIBLE : GONE);
        int bottomLineColor = typedArray.getColor(R.styleable.TitleBar_tb_bottom_line_color, 0);
        mVBottomLine.setBackgroundColor(bottomLineColor);
        int bottomLineHeight =
                (int) typedArray.getDimension(R.styleable.TitleBar_tb_bottom_line_height, 1);
        ViewGroup.LayoutParams lineParams = mVBottomLine.getLayoutParams();
        lineParams.height = bottomLineHeight;
        mVBottomLine.setLayoutParams(lineParams);

        Drawable rightTextBg =
                typedArray.getDrawable(R.styleable.TitleBar_tb_right_text_background);
        if (rightTextBg != null) {
            mTxtRight.setBackgroundDrawable(rightTextBg);
        }
        int rightTextWidth = (int) typedArray.getDimension(R.styleable.TitleBar_tb_right_text_width,
                -1);
        int rightTextHeight =
                (int) typedArray.getDimension(R.styleable.TitleBar_tb_right_text_height,
                        -1);
        if (rightTextWidth >= 0 && rightTextHeight >= 0) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) mTxtRight.getLayoutParams();
            params.width = rightTextWidth;
            params.height = rightTextHeight;
            mTxtRight.setLayoutParams(params);
        }

        mVgLeft.setOnClickListener(this);
        mTxtLeft.setOnClickListener(this);
        mImgLeft.setOnClickListener(this);
        mVgRight.setOnClickListener(this);
        mTxtRight.setOnClickListener(this);
        mImgRight.setOnClickListener(this);
        mTxtTitle.setOnClickListener(this);

        typedArray.recycle();

        updateTitleMargin();
    }

    private void updateTitleMargin() {
        updateTitleMargin(0);
    }

    public void updateTitleMargin(final int extraSpace) {
        mVgLeft.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int vgLeftWidth;

            @Override
            public void onGlobalLayout() {
                mVgLeft.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                vgLeftWidth = mVgLeft.getWidth();
                mVgRight.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    int vgRightWidth;

                    @Override
                    public void onGlobalLayout() {
                        mVgRight.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        vgRightWidth = mVgRight.getWidth();
                        LayoutParams paramsLeft =
                                (LayoutParams) mVgLeft.getLayoutParams();
                        int vgLeftLeftMargin = paramsLeft.leftMargin;
                        int vgLeftRightMargin = paramsLeft.rightMargin;
                        LayoutParams paramsRight =
                                (LayoutParams) mVgRight.getLayoutParams();
                        int vgRightLeftMargin = paramsRight.leftMargin;
                        int vgRightRightMargin = paramsRight.rightMargin;
                        int maxMargin =
                                Math.max(vgLeftWidth + vgLeftLeftMargin + vgLeftRightMargin,
                                        vgRightWidth + vgRightLeftMargin + vgRightRightMargin);

                        setTitleMarginHorizontal(maxMargin + extraSpace);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (mListener == null) {
            return;
        }
        int vid = v.getId();
        if (mViewClicked.containsKey(vid)) {
            long lastClickTime = mViewClicked.get(vid);
            if (System.currentTimeMillis() - lastClickTime <= mFastClickDuration) {
                Log.w(TAG, "you click so fast!");
                return;
            }
        }
        mViewClicked.put(vid, System.currentTimeMillis());
        if (vid == R.id.layout_left || vid == R.id.txt_left || vid == R.id.img_left) {
            mListener.onLeftAreaClick();
        } else if (vid == R.id.layout_right || vid == R.id.txt_right || vid == R.id.img_right) {
            mListener.onRightAreaClick();
        } else if (vid == R.id.txt_title) {
            mListener.onCenterAreaClick();
        }
    }

    public interface Listener {
        void onLeftAreaClick();

        void onRightAreaClick();

        void onCenterAreaClick();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setRightVisible(boolean b) {
        mVgRight.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setLeftVisible(boolean b) {
        mVgLeft.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setLeftImageVisible(boolean show) {
        mImgLeft.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setLeftImage(int drawable) {
        mImgLeft.setImageResource(drawable);
    }

    public void setLeftImage(Drawable drawable) {
        mImgLeft.setImageDrawable(drawable);
    }

    public void setRightText(int resText) {
        mTxtRight.setText(resText);
    }

    public void setRightText(String text) {
        mTxtRight.setText(text);
    }

    public void setRightTextColor(int color) {
        mTxtRight.setTextColor(color);
    }

    public String getRightText() {
        return mTxtRight.getText().toString();
    }

    public void setRightImageVisible(boolean show) {
        mImgRight.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setRightImage(int drawable) {
        mImgRight.setImageResource(drawable);
    }

    public void setRightImage(Drawable drawable) {
        mImgRight.setImageDrawable(drawable);
    }

    public void setTitle(String title) {
        mTxtTitle.setText(title);
    }

    public void setTitle(int resTitle) {
        mTxtTitle.setText(resTitle);
    }

    public String getTitle() {
        return mTxtTitle.getText().toString();
    }

    public void setTitleBackground(Drawable drawable) {
        mTxtTitle.setBackgroundDrawable(drawable);
    }

    public void setTitleVisible(boolean b) {
        mVgTitle.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setSubTitle(String subTitle) {
        mTvSubTitle.setText(subTitle);
    }

    public void setSubTitle(int subTitle) {
        mTvSubTitle.setText(subTitle);
    }

    public void setSubTitleVisible(boolean b) {
        mVgSubTitleLayout.setVisibility(b ? VISIBLE : GONE);
    }

    public String getSubTitle() {
        return mTvSubTitle.getText().toString();
    }

    public void setLeftText(int resText) {
        mTxtLeft.setText(resText);
    }

    public void setLeftText(String text) {
        mTxtLeft.setText(text);
    }

    public String getLeftText() {
        return mTxtLeft.getText().toString();
    }

    public void setLeftTextColor(int color) {
        mTxtLeft.setTextColor(color);
    }

    public void setStatusVisible(boolean show) {
        mViewStatus.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setStatusBackground(Drawable drawable) {
        mViewStatus.setBackgroundDrawable(drawable);
    }

    public void setTitleSize(float textSize) {
        mTxtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    public void setTitleColor(int color) {
        mTxtTitle.setTextColor(color);
    }

    public View getViewStatus() {
        return mViewStatus;
    }

    public ViewGroup getVgTitle() {
        return mVgTitle;
    }

    public TextView getTxtTitle() {
        return mTxtTitle;
    }

    public TextView getTvSubTitle() {
        return mTvSubTitle;
    }

    public ViewGroup getVgTitleLayout() {
        return mVgTitleLayout;
    }

    public ViewGroup getVgSubTitleLayout() {
        return mVgSubTitleLayout;
    }

    public ViewGroup getVgTitleBox() {
        return mVgTitleBox;
    }

    public ViewGroup getVgLeft() {
        return mVgLeft;
    }

    public TextView getTxtLeft() {
        return mTxtLeft;
    }

    public ImageView getImgLeft() {
        return mImgLeft;
    }

    public ViewGroup getVgRight() {
        return mVgRight;
    }

    public TextView getTxtRight() {
        return mTxtRight;
    }

    public ImageView getImgRight() {
        return mImgRight;
    }

    public View getVBottomLine() {
        return mVBottomLine;
    }

    public void setTitleMarginHorizontal(int marginHorizontal) {
        LayoutParams titleBoxParams = (LayoutParams) mVgTitleBox.getLayoutParams();
        titleBoxParams.leftMargin = marginHorizontal;
        titleBoxParams.rightMargin = marginHorizontal;
        mVgTitleBox.setLayoutParams(titleBoxParams);
    }

    public boolean adjustStatusHeight() {
        int height = getStatusHeight(getContext());
        if (height != -1) {
            ViewGroup.LayoutParams paramsSh = mViewStatus.getLayoutParams();
            paramsSh.height = height;
            mViewStatus.setLayoutParams(paramsSh);
            return true;
        }
        return false;
    }

    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object)
                    .toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public void setFastClickDuration(int fastClickDuration) {
        mFastClickDuration = fastClickDuration;
    }
}
