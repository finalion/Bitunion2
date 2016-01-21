package app.vleon.bitunion.util;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import org.xml.sax.XMLReader;

/**
 * Created by vleon on 2016/1/17.
 */
public class HtmlTagHandler implements Html.TagHandler {
    private final Context mContext;
    private int sIndex = 0;
    private int eIndex = 0;

    public HtmlTagHandler(Context context) {
        mContext = context;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

        switch (tag.toLowerCase()) {
            case "unknown":
                if (opening) {
                    sIndex = output.length();
                } else {
                    eIndex = output.length();
//                    output.insert(eIndex,"============");
                    output.setSpan(new MySpan(), sIndex, eIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                break;
            default:
                break;
        }
    }

    private class MySpan extends ClickableSpan {
        @Override
        public void onClick(View widget) {
            Toast.makeText(mContext, "tag handled", Toast.LENGTH_SHORT).show();
        }
    }
}
