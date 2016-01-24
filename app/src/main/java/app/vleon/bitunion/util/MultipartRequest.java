package app.vleon.bitunion.util;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by vleon on 2016/1/22.
 */
public class MultipartRequest extends Request<NetworkResponse> {
    public Response.Listener<NetworkResponse> mListener;
    public Response.ErrorListener mErrorListener;
    public Map<String, String> mHeaders;
    public String mMimeType;
    public byte[] mMultipartBody;

    public MultipartRequest(String url, Map<String, String> headers,
                            Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
    }

    public void buildBody(Map<String, String> formData, String twoHyphens, String lineEnd, String boundary) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        for (Object o : formData.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes(String.format("Content-Disposition: form-data; name=\"%s\"", key) + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(value + lineEnd);
        }
        //结尾
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        this.mMultipartBody = bos.toByteArray();
    }

    public void buildBody(Map<String, String> formData, String boundary) throws IOException {
        final String twoHyphens = "--";
        final String lineEnd = "\r\n";
        this.mMimeType = "multipart/form-data;boundary=" + boundary;
        buildBody(formData, twoHyphens, lineEnd, boundary);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return mMimeType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mMultipartBody;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}