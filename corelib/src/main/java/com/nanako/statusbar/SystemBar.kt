package com.nanako.statusbar

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.nanako.log.Log.Companion.LOG

object SystemBar {
    fun setColor(act: Activity, statusBar: Boolean, statusRes: Int) {
        setColor(act.window, statusBar, statusRes, false, false, 0, false)
    }

    fun setColor(
        act: Activity,
        statusBar: Boolean,
        statusRes: Int,
        androidMLightStatusBar: Boolean
    ) {
        setColor(act.window, statusBar, statusRes, androidMLightStatusBar, false, 0, false)
    }

    fun setColor(
        act: Activity,
        statusBar: Boolean,
        statusRes: Int,
        navBar: Boolean,
        navRes: Int,
        androidMLightStatusBar: Boolean
    ) {
        setColor(
            act.window,
            statusBar,
            statusRes,
            androidMLightStatusBar,
            navBar,
            navRes,
            false
        )
    }

    fun setColor(
        window: Window,
        statusBar: Boolean,
        statusColorRes: Int,
        lightStatusBar: Boolean,
        navBar: Boolean,
        navColorRes: Int,
        lightNavBar: Boolean
    ) {
        if (statusBar || navBar) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        if (statusBar) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = window.context.resources.getColor(statusColorRes)
        }
        if (navBar) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.navigationBarColor = window.context.resources.getColor(navColorRes)
        }
        var visibility = window.decorView.systemUiVisibility
        visibility =
            visibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (lightStatusBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_STATUS_BAR SDK must bigger than 23!!!")
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                visibility = visibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_STATUS_BAR SDK must bigger than 23!!!")
            }
        }
        if (lightNavBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR SDK must bigger than 26!!!")
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility = visibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR SDK must bigger than 26!!!")
            }
        }
        window.decorView.systemUiVisibility = visibility
    }

    fun setStatusLight(window: Window, lightStatusBar: Boolean) {
        var visibility = window.decorView.systemUiVisibility
        if (lightStatusBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_STATUS_BAR SDK must bigger than 23!!!")
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                visibility = visibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_STATUS_BAR SDK must bigger than 23!!!")
            }
        }
        window.decorView.systemUiVisibility = visibility
    }

    fun setNavigationLight(window: Window, lightNavBar: Boolean) {
        var visibility = window.decorView.systemUiVisibility
        if (lightNavBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR SDK must bigger than 26!!!")
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility = visibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            } else {
                LOG.w("Use SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR SDK must bigger than 26!!!")
            }
        }
        window.decorView.systemUiVisibility = visibility
    } /*@Deprecated
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void caseLollipop(Activity act,
                                    boolean statusBar,
                                    int statusRes,
                                    boolean navBar,
                                    int navRes,
                                    boolean androidMLightStatusBar) {
        caseLollipop(act.getWindow(), statusBar, statusRes, navBar, navRes, androidMLightStatusBar);
    }

    @Deprecated
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void caseLollipop(Window window,
                                    boolean statusBar,
                                    int statusRes,
                                    boolean navBar,
                                    int navRes,
                                    boolean androidMLightStatusBar) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        boolean isM = Build.VERSION.SDK_INT == Build.VERSION_CODES.M;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && androidMLightStatusBar) {
            if (isM && MIUISetStatusBarLightMode(window, true)) {
                //do nothing
            } else if (isM && FlymeSetStatusBarLightMode(window, true)) {
                //do nothing
            } else {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
        window.getDecorView().setSystemUiVisibility(visibility);

        if (statusBar || navBar) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        if (statusBar) {
            window.setStatusBarColor(window.getContext().getResources().getColor(statusRes));
        }

        if (navBar) {
            window.setNavigationBarColor(window.getContext().getResources().getColor(navRes));
        }
    }*/
    /*@Deprecated
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void caseKitkat(Activity act,
                                  boolean statusBar,
                                  int statusRes,
                                  boolean navBar,
                                  int navRes) {
        Window window = act.getWindow();
        int flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (statusBar) {
            window.addFlags(flags);
        } else {
            window.clearFlags(flags);
        }
        flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        if (navBar) {
            window.addFlags(flags);
        } else {
            window.clearFlags(flags);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(act);
        tintManager.setStatusBarTintEnabled(statusBar);
        tintManager.setStatusBarTintResource(statusRes);
        tintManager.setNavigationBarTintEnabled(navBar);
        tintManager.setNavigationBarTintResource(navRes);
    }*/
    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    /*
    public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField(
                        "MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }*/
    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    /*
    public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }*/
}