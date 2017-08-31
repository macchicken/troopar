package com.troopar.trooparapp;

import android.app.Application;
import android.graphics.Typeface;
import android.os.Build;
import android.util.LongSparseArray;

import java.lang.reflect.Field;

/**
 * Created by Barry on 5/02/2016.
 */
public class FontsOverrideApplication extends Application {

    private static final String DEFAULT_SERIF_BOLD_FONT_FILENAME = "fonts/century-gothic-bold.ttf";
    private static final String DEFAULT_SERIF_BOLD_ITALIC_FONT_FILENAME = "fonts/century-gothic.ttf";
    private static final String DEFAULT_SERIF_ITALIC_FONT_FILENAME = "fonts/century-gothic.ttf";

    private static final String DEFAULT_SANS_BOLD_FONT_FILENAME = "fonts/century-gothic-bold.ttf";
    private static final String DEFAULT_SANS_BOLD_ITALIC_FONT_FILENAME = "fonts/century-gothic-bold.ttf";
    private static final String DEFAULT_SANS_ITALIC_FONT_FILENAME = "fonts/century-gothic.ttf";

    private static final String DEFAULT_NORMAL_BOLD_FONT_FILENAME = "fonts/century-gothic-bold.ttf";
    private static final String DEFAULT_NORMAL_BOLD_ITALIC_FONT_FILENAME = "fonts/century-gothic-bold.ttf";
    private static final String DEFAULT_NORMAL_ITALIC_FONT_FILENAME = "fonts/century-gothic.ttf";
    private static final String DEFAULT_NORMAL_NORMAL_FONT_FILENAME = "fonts/century-gothic.ttf";

    private static final String DEFAULT_MONOSPACE_BOLD_FONT_FILENAME = "fonts/century-gothic-bold.ttf";
    private static final String DEFAULT_MONOSPACE_BOLD_ITALIC_FONT_FILENAME = "fonts/century-gothic-bold.ttf";
    private static final String DEFAULT_MONOSPACE_ITALIC_FONT_FILENAME = "fonts/century-gothic.ttf";
    private static final String DEFAULT_MONOSPACE_NORMAL_FONT_FILENAME = "fonts/century-gothic.ttf";

    private static final String DEFAULT_SANS_NORMAL_FONT_FILENAME = "fonts/century-gothic.ttf";
    private static final String DEFAULT_SERIF_NORMAL_FONT_FILENAME = "fonts/century-gothic.ttf";

    private static final int sans_idx = 1;
    private static final int serif_idx = 2;
    private static final int monospace_idx = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            setDefaultFonts();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // The following code is only necessary if you are using the android:typeface attribute
                setDefaultFontForTypeFaceSansSerif();
                setDefaultFontForTypeFaceSans();
                setDefaultFontForTypeFaceMonospace();
            }
        } catch (Throwable e) {
            e.printStackTrace();// Must not crash app if there is a failure with overriding fonts!
        }

    }

    private void setDefaultFontForTypeFaceMonospace() throws NoSuchFieldException, IllegalAccessException {
        final Typeface bold = Typeface.createFromAsset(getAssets(), DEFAULT_MONOSPACE_BOLD_FONT_FILENAME);
        final Typeface italic = Typeface.createFromAsset(getAssets(), DEFAULT_MONOSPACE_ITALIC_FONT_FILENAME);
        final Typeface boldItalic = Typeface.createFromAsset(getAssets(), DEFAULT_MONOSPACE_BOLD_ITALIC_FONT_FILENAME);
        final Typeface normal = Typeface.createFromAsset(getAssets(), DEFAULT_MONOSPACE_NORMAL_FONT_FILENAME);
        setTypeFaceDefaults(normal, bold, italic, boldItalic, monospace_idx);
    }

    private void setDefaultFontForTypeFaceSans() throws NoSuchFieldException, IllegalAccessException {
        final Typeface bold = Typeface.createFromAsset(getAssets(), DEFAULT_SANS_BOLD_FONT_FILENAME);
        final Typeface italic = Typeface.createFromAsset(getAssets(), DEFAULT_SANS_ITALIC_FONT_FILENAME);
        final Typeface boldItalic = Typeface.createFromAsset(getAssets(), DEFAULT_SANS_BOLD_ITALIC_FONT_FILENAME);
        final Typeface normal = Typeface.createFromAsset(getAssets(), DEFAULT_SANS_NORMAL_FONT_FILENAME);
        setTypeFaceDefaults(normal, bold, italic, boldItalic, sans_idx);
    }

    private void setDefaultFontForTypeFaceSansSerif() throws NoSuchFieldException, IllegalAccessException {
        final Typeface bold = Typeface.createFromAsset(getAssets(), DEFAULT_SERIF_BOLD_FONT_FILENAME);
        final Typeface italic = Typeface.createFromAsset(getAssets(), DEFAULT_SERIF_ITALIC_FONT_FILENAME);
        final Typeface boldItalic = Typeface.createFromAsset(getAssets(), DEFAULT_SERIF_BOLD_ITALIC_FONT_FILENAME);
        final Typeface normal = Typeface.createFromAsset(getAssets(), DEFAULT_SERIF_NORMAL_FONT_FILENAME);

        setTypeFaceDefaults(normal, bold, italic, boldItalic, serif_idx);
    }

    private void setTypeFaceDefaults(Typeface normal, Typeface bold, Typeface italic, Typeface boldItalic, int typefaceIndex) throws NoSuchFieldException, IllegalAccessException {
        Field typeFacesField = Typeface.class.getDeclaredField("sTypefaceCache");
        typeFacesField.setAccessible(true);

        LongSparseArray<LongSparseArray<Typeface>> sTypefaceCacheLocal = new LongSparseArray<>(3);
        typeFacesField.get(sTypefaceCacheLocal);

        LongSparseArray<Typeface> newValues = new LongSparseArray<>(4);
        newValues.put(Typeface.NORMAL, normal);
        newValues.put(Typeface.BOLD, bold);
        newValues.put(Typeface.ITALIC, italic);
        newValues.put(Typeface.BOLD_ITALIC, boldItalic);
        sTypefaceCacheLocal.put(typefaceIndex, newValues);

        typeFacesField.set(null, sTypefaceCacheLocal);
    }

    private void setDefaultFonts() throws NoSuchFieldException, IllegalAccessException {
        final Typeface bold = Typeface.createFromAsset(getAssets(), DEFAULT_NORMAL_BOLD_FONT_FILENAME);
        final Typeface italic = Typeface.createFromAsset(getAssets(), DEFAULT_NORMAL_ITALIC_FONT_FILENAME);
        final Typeface boldItalic = Typeface.createFromAsset(getAssets(), DEFAULT_NORMAL_BOLD_ITALIC_FONT_FILENAME);
        final Typeface normal = Typeface.createFromAsset(getAssets(), DEFAULT_NORMAL_NORMAL_FONT_FILENAME);

        Field defaultField = Typeface.class.getDeclaredField("DEFAULT");
        defaultField.setAccessible(true);
        defaultField.set(null, normal);

        Field defaultBoldField = Typeface.class.getDeclaredField("DEFAULT_BOLD");
        defaultBoldField.setAccessible(true);
        defaultBoldField.set(null, bold);

        Field sDefaults = Typeface.class.getDeclaredField("sDefaults");
        sDefaults.setAccessible(true);
        sDefaults.set(null, new Typeface[]{normal, bold, italic, boldItalic});

        final Typeface normal_sans = Typeface.createFromAsset(getAssets(), DEFAULT_SANS_NORMAL_FONT_FILENAME);
        Field sansSerifDefaultField = Typeface.class.getDeclaredField("SANS_SERIF");
        sansSerifDefaultField.setAccessible(true);
        sansSerifDefaultField.set(null, normal_sans);

        final Typeface normal_serif = Typeface.createFromAsset(getAssets(), DEFAULT_SERIF_NORMAL_FONT_FILENAME);
        Field serifDefaultField = Typeface.class.getDeclaredField("SERIF");
        serifDefaultField.setAccessible(true);
        serifDefaultField.set(null, normal_serif);

        final Typeface normal_monospace = Typeface.createFromAsset(getAssets(), DEFAULT_MONOSPACE_NORMAL_FONT_FILENAME);
        Field monospaceDefaultField = Typeface.class.getDeclaredField("MONOSPACE");
        monospaceDefaultField.setAccessible(true);
        monospaceDefaultField.set(null, normal_monospace);
    }


}
