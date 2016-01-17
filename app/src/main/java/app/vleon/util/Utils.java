package app.vleon.util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

/**
 * Created by vleon on 2016/1/17.
 */
public class Utils {

    private static void setLinkClickable(final SpannableStringBuilder clickableHtmlBuilder,
                                         final URLSpan urlSpan, final OnClickedClickableSpanListener clickableSpanListener) {
        int start = clickableHtmlBuilder.getSpanStart(urlSpan);
        int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
        int flags = clickableHtmlBuilder.getSpanFlags(urlSpan);
        ClickableSpan clickableSpan = new ClickableSpan() {
            public void onClick(View view) {
                clickableSpanListener.onClick(view, urlSpan);
            }
        };
        clickableHtmlBuilder.setSpan(clickableSpan, start, end, flags);
        clickableHtmlBuilder.removeSpan(urlSpan);
    }

    public static CharSequence getClickableHtml(String html, final OnClickedClickableSpanListener clickableSpanListener,
                                                Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        Spanned spannedHtml = Html.fromHtml(html, imageGetter, tagHandler);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        if (clickableSpanListener != null) {
            URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
            for (final URLSpan span : urls) {
                setLinkClickable(clickableHtmlBuilder, span, clickableSpanListener);
            }
        }
        return clickableHtmlBuilder;
    }

    public static interface OnClickedClickableSpanListener {
        void onClick(View view, URLSpan urlSpan);
    }
}
