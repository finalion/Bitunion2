package app.vleon.bitunion.util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by vleon on 2016/1/17.
 */
public class Utils {

    /**
     * JsonWriter
     * 把Json数据写入文件里面
     *
     * @param gson
     * @param mapList
     * @return
     * @throws Exception
     */
    private static File writerJsonToFile(Gson gson, List<Map> mapList) throws Exception {
        File file = new File("gson");// 把json保存项目根目录下无后缀格式的文本
        OutputStream out = new FileOutputStream(file);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));//设计编码
        gson.toJson(mapList, new TypeToken<List<Map<String, String>>>() {
        }.getType(), writer);
        writer.flush();
        writer.close();
        return file;
    }

    /**
     * JsonReader
     * 读取从本地文件的Json数据
     *
     * @param gson
     * @throws Exception
     */
    public static Map<String, List<Map<String, String>>> readJsonFromFile(InputStream input) throws Exception {
//        InputStream input = new FileInputStream(file);
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new InputStreamReader(input));
        Map<String, List<Map<String, String>>> content = gson.fromJson(reader, new TypeToken<Map<String, List<Map<String, String>>>>() {
        }.getType());
        //List<Map> content = gson.fromJson(new InputStreamReader(input),new TypeToken<List<Map<String, String>>>() {}.getType());
        reader.close();
        return content;
    }

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

            // TODO: 2016/1/19  以下获取img标签，可进行点击处理
//            ImageSpan[] imgs = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), ImageSpan.class);
//            for (final ImageSpan span : imgs) {
//
//            }
        }
        return clickableHtmlBuilder;
    }

    public interface OnClickedClickableSpanListener {
        void onClick(View view, URLSpan urlSpan);
    }
}
