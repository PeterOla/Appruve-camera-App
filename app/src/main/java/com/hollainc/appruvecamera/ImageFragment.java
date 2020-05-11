package com.hollainc.appruvecamera;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageFragment extends Fragment {

    private Bitmap bitmap;
    private String path;
    private File file;
    @BindView(R.id.res_photo)
    ImageView resPhoto;
    private String ENDPOINT = "https://stage.appruve.co/v1/verifications/test/file_upload";
    ProgressDialog p;
    OkHttpClient client;

    public void imageSetupFragment(Bitmap bitmap, String path) {
        if (bitmap != null) {
            this.bitmap = bitmap;
            this.path = path;

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, view);
        client = new OkHttpClient();

        if (bitmap != null) {
            resPhoto.setImageBitmap(bitmap);
        }


        view.findViewById(R.id.sendBtn).setOnClickListener(v -> {

            try {

                file = new File(path);
                new ServerPushAsync().execute(ENDPOINT);

            } catch (Exception e) {
                Toast.makeText(getActivity(), "H" + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        return view;
    }


    private class ServerPushAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setMessage("Uploading.. Please wait...");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String endPoint = strings[0];

                String uniqueID = UUID.randomUUID().toString();

                RequestBody formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("document", file.getName(),
                                RequestBody.create(MediaType.parse("image/png"), file))
                        .addFormDataPart("user_id", uniqueID)
                        .build();
                Request request = new Request.Builder().url(endPoint).post(formBody).build();
                Response response = client.newCall(request).execute();

                Log.e("APPRUVE_LOG", response.body().string());
                return response.body().string();
            } catch (Exception e) {
                Log.e("APPRUVE_LOG", e.getMessage());
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            p.hide();
            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getActivity(), MainActivity.class);
            startActivity(i);
        }
    }


}
