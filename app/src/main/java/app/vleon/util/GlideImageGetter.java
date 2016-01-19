package app.vleon.util;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import java.util.HashSet;
import java.util.Set;

import app.vleon.bitunion.R;
import app.vleon.buapi.BuAPI;

public final class GlideImageGetter implements Html.ImageGetter, Drawable.Callback {

    private final Context mContext;

    private final TextView mTextView;

    private final Set<ImageGetterViewTarget> mTargets;

    public GlideImageGetter(Context context, TextView textView) {
        this.mContext = context;
        this.mTextView = textView;

        clear();
        mTargets = new HashSet<>();
        mTextView.setTag(R.id.choice, this);
    }

    public static GlideImageGetter get(View view) {
        return (GlideImageGetter) view.getTag(R.id.choice);
    }

    public void clear() {
        GlideImageGetter prev = get(mTextView);
        if (prev == null) return;

        for (ImageGetterViewTarget target : prev.mTargets) {
            Glide.clear(target);
        }
    }

    @Override
    public Drawable getDrawable(String url) {
        final UrlDrawable urlDrawable = new UrlDrawable();
        System.out.println("Downloading from: " + url);
        url = BuAPI.getAvailableUrl(url);
        Glide.with(mContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new ImageGetterViewTarget(mTextView, urlDrawable));


        return urlDrawable;

    }

    @Override
    public void invalidateDrawable(Drawable who) {
        mTextView.invalidate();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {

    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {

    }

    private class ImageGetterViewTarget extends ViewTarget<TextView, GlideDrawable> {

        private final UrlDrawable mDrawable;
        private Request request;

        private ImageGetterViewTarget(TextView view, UrlDrawable drawable) {
            super(view);
            mTargets.add(this);
            this.mDrawable = drawable;
        }

        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            Rect rect;
            if (resource.getIntrinsicWidth() > 100) {
                float width;
                float height;
                System.out.println("Image width is " + resource.getIntrinsicWidth());
                System.out.println("View width is " + view.getWidth());
                if (resource.getIntrinsicWidth() >= getView().getWidth()) {
                    float downScale = (float) resource.getIntrinsicWidth() / getView().getWidth();
                    width = (float) resource.getIntrinsicWidth() / downScale;
                    height = (float) resource.getIntrinsicHeight() / downScale;
                } else {
                    float multiplier = (float) getView().getWidth() / resource.getIntrinsicWidth();
                    width = (float) resource.getIntrinsicWidth() * multiplier;
                    height = (float) resource.getIntrinsicHeight() * multiplier;
                }
                System.out.println("New Image width is " + width);


                rect = new Rect(0, 0, Math.round(width), Math.round(height));
            } else {
                rect = new Rect(0, 0, resource.getIntrinsicWidth() * 2, resource.getIntrinsicHeight() * 2);
            }
            resource.setBounds(rect);

            mDrawable.setBounds(rect);
            mDrawable.setDrawable(resource);


            if (resource.isAnimated()) {
                mDrawable.setCallback(get(getView()));
                resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
                resource.start();
            }

            getView().setText(getView().getText());
            getView().invalidate();
        }

        @Override
        public Request getRequest() {
            return request;
        }

        @Override
        public void setRequest(Request request) {
            this.request = request;
        }
    }
}
//
//public final class GlideImageGetter implements Html.ImageGetter, View.OnAttachStateChangeListener, Drawable.Callback {
//
//    private final Context mContext;
//
//    private final TextView mTextView;
//
//    /**
//     * Weak {@link java.util.HashSet}.
//     */
//    private final Set<ViewTarget> mViewTargetSet = Collections.newSetFromMap(new WeakHashMap<>());
//
//    public GlideImageGetter(Context context, TextView textView) {
//        this.mContext = context;
//        this.mTextView = textView;
//
//        // save Drawable.Callback in TextView
//        // and get back when finish fetching image
//        // see https://github.com/goofyz/testGlide/pull/1 for more details
//        mTextView.setTag(R.id.book_now, this);
//        // add this listener in order to clean any pending images loading
//        // and set drawable callback tag to null when detached from window
//        mTextView.addOnAttachStateChangeListener(this);
//    }
//
//    /**
//     * We display image depends on settings and Wi-Fi status,
//     * but display emoticons at any time.
//     */
//    @Override
//    public Drawable getDrawable(final String url) {
//        UrlDrawable urlDrawable = new UrlDrawable();
//
//        // url has no domain if it comes from server.
//        if (!URLUtil.isNetworkUrl(url)) {
//            ImageGetterViewTarget imageGetterViewTarget = new ImageGetterViewTarget(mTextView,
//                    urlDrawable);
//            // We may have this image in assets if this is emoticon.
//            if (url.startsWith(BuAPI.URL_EMOTICON_IMAGE_PREFIX)) {
//                String emoticonFileName = url.substring(BuAPI.URL_EMOTICON_IMAGE_PREFIX.length());
//                TransformationUtil.SizeMultiplierBitmapTransformation sizeMultiplierBitmapTransformation =
//                        new TransformationUtil.SizeMultiplierBitmapTransformation(mContext,
//                                mContext.getResources().getDisplayMetrics().density);
//                Glide.with(mContext)
//                        .load(Uri.parse(EmoticonFactory.ASSET_PATH_EMOTICON + emoticonFileName))
//                        .transform(sizeMultiplierBitmapTransformation)
//                        .listener(new RequestListener<Uri, GlideDrawable>() {
//
//                            /**
//                             * Occurs If we don't have this image (maybe a new emoticon) in assets.
//                             */
//                            @Override
//                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                // append domain to this url
//                                Glide.with(mContext)
//                                        .load(BuAPI.BASEURL + url)
//                                        .transform(sizeMultiplierBitmapTransformation)
//                                        .into(imageGetterViewTarget);
//
//                                return true;
//                            }
//
//                            @Override
//                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                return false;
//                            }
//                        })
//                        .into(imageGetterViewTarget);
//            } else {
//                Glide.with(mContext)
//                        .load(BuAPI.BASEURL + url)
//                        .transform(new TransformationUtil.GlMaxTextureSizeBitmapTransformation(
//                                mContext))
//                        .into(imageGetterViewTarget);
//            }
//
//            mViewTargetSet.add(imageGetterViewTarget);
//            return urlDrawable;
//        }
//
//        if (App.getAppComponent(mContext).getDownloadPreferencesManager().isImagesDownload()) {
//            ImageGetterViewTarget imageGetterViewTarget = new ImageGetterViewTarget(mTextView,
//                    urlDrawable);
//            Glide.with(mContext)
//                    .load(url)
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .transform(new TransformationUtil.GlMaxTextureSizeBitmapTransformation(mContext))
//                    .into(imageGetterViewTarget);
//
//            mViewTargetSet.add(imageGetterViewTarget);
//            return urlDrawable;
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public void onViewAttachedToWindow(View v) {}
//
//    @Override
//    public void onViewDetachedFromWindow(View v) {
//        // cancels any pending images loading
//        for (ViewTarget viewTarget : mViewTargetSet) {
//            Glide.clear(viewTarget);
//        }
//        mViewTargetSet.clear();
//        v.removeOnAttachStateChangeListener(this);
//
//        v.setTag(R.id.book_now, null);
//    }
//
//    /**
//     * Implements {@link Drawable.Callback} in order to
//     * redraw the TextView which contains the animated GIFs.
//     */
//    @Override
//    public void invalidateDrawable(Drawable who) {
//        mTextView.invalidate();
//    }
//
//    @Override
//    public void scheduleDrawable(Drawable who, Runnable what, long when) {}
//
//    @Override
//    public void unscheduleDrawable(Drawable who, Runnable what) {}
//
//    private static final class ImageGetterViewTarget extends ViewTarget<TextView, GlideDrawable> {
//
//        private final UrlDrawable mDrawable;
//
//        private Request mRequest;
//
//        private ImageGetterViewTarget(TextView view, UrlDrawable drawable) {
//            super(view);
//
//            this.mDrawable = drawable;
//        }
//
//        @Override
//        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
//            // resize this drawable's width & height to fit its container
//            final int resWidth = resource.getIntrinsicWidth();
//            final int resHeight = resource.getIntrinsicHeight();
//            int width, height;
//            TextView textView = getView();
//            if (textView.getWidth() >= resWidth) {
//                width = resWidth;
//                height = resHeight;
//            } else {
//                width = textView.getWidth();
//                height = (int) (resHeight / ((float) resWidth / width));
//            }
//
//            Rect rect = new Rect(0, 0, width, height);
//            resource.setBounds(rect);
//            mDrawable.setBounds(rect);
//            mDrawable.setDrawable(resource);
//
//            if (resource.isAnimated()) {
//                Drawable.Callback callback = (Drawable.Callback) textView.getTag(
//                        R.id.book_now);
//                // note: not sure whether callback would be null sometimes
//                // when this Drawable' host view is detached from View
//                if (callback != null) {
//                    // set callback to drawable in order to
//                    // signal its container to be redrawn
//                    // to show the animated GIF
//                    mDrawable.setCallback(callback);
//                    resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
//                    resource.start();
//                }
//            } else {
//                textView.setTag(R.id.book_now, null);
//            }
//
//            // see http://stackoverflow.com/questions/7870312/android-imagegetter-images-overlapping-text#comment-22289166
//            textView.setText(textView.getText());
//        }
//
//        /**
//         * See https://github.com/bumptech/glide/issues/550#issuecomment-123693051
//         *
//         * @see com.bumptech.glide.GenericRequestBuilder#into(com.bumptech.glide.request.target.Target)
//         */
//        @Override
//        public Request getRequest() {
//            return mRequest;
//        }
//
//        @Override
//        public void setRequest(Request request) {
//            this.mRequest = request;
//        }
//    }
//}
