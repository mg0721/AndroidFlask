package gad.heartbeat.androidflask.easyupload;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class ButtonBoolean {
    ButtonBoolean (boolean use_this) {
        this.use_this = use_this;
    }
    boolean use_this;
}

public class MainActivity extends AppCompatActivity {
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");
    final int SELECT_MULTIPLE_IMAGES = 1;
    ArrayList<String> selectedImagesPaths; // Paths of the image(s) selected by the user.
    boolean imagesSelected = false; // Whether the user selected at least an image or not.
    boolean nirSelected = false;

    ButtonBoolean use_button[] = {
        new ButtonBoolean(false),
        new ButtonBoolean(false),
        new ButtonBoolean(false),
        new ButtonBoolean(false),
    };
    String nir_names[] = {
            "Cotton", "Poly", "Nylon", "Wool",
    };
    String nir_values[][] = {
            {   // Cotton = 0
                "0.0", "0.0"," 0.0",
                "1.1417472634577899", "1.0093176660640792", "0.8650800891472363", "0.9409295926719259",
                "1.0308508764203295", "0.8315421183927704", "0.6908137300721815", "0.7603202585573675",
                "0.9803491382031694", "0.9692163976450652", "0.7883998093119339", "0.8173548210014507", "1.0534289035257653",
            },
            {   // Poly = 1
                "0.0", "0.0"," 0.0",
                "1.0229138551506973", "0.9276318319935382", "0.8615480860713960", "0.9117078522543232",
                "0.9607702809830002", "0.8796931138898912", "0.8231811808692943", "0.8806130943636453",
                "0.9715733316497253", "0.8614412026677674", "0.7865882726839345", "0.8284123436458455", "0.9862843858969391",
            },
            {   // Nylon = 2
                "0.0", "0.0"," 0.0",
                "1.031779502174239", "1.0596176776029846", "0.8994877943878947", "0.9215297006425996",
                "0.9952985994891219", "0.9407565464329982", "0.7585812951134652", "0.7978918760099898",
                "0.9840425531914893", "0.9782755099824032", "0.7546162402669633", "0.7733992079363212", "1.0098798915149167",
            },
            {   // Wool = 3
                "0.0", "0.0"," 0.0",
                "0.9261602189233769", "0.8326474095157507", "0.7605406180353721", "0.8128450272538879",
                "0.8669184356557739", "0.7031080847859171", "0.6063509373214935", "0.6627246461975417",
                "0.8181545552118189", "0.7614650996067867", "0.6320673764500239", "0.6527859467513625", "0.7915149166989539",
            },
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 2);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void connectServer(View v) {
        TextView responseText = findViewById(R.id.responseText);
        if (nirSelected == true && imagesSelected == true) {
            responseText.setText("Sending both the NIR and image data. Please Wait ...");
        }
        else if (nirSelected == true && imagesSelected == false) {
            responseText.setText("Sending the NIR data. Please Wait ...");
        }
        else if (nirSelected == false && imagesSelected == true) {
            responseText.setText("Sending the Image files. Please Wait ...");
        }
        else {
            responseText.setText("No Image and NIR data Selected to Upload. Try Again.");
            return;
        }

        EditText ipv4AddressView = findViewById(R.id.IPAddress);
        String ipv4Address = ipv4AddressView.getText().toString();
        EditText portNumberView = findViewById(R.id.portNumber);
        String portNumber = portNumberView.getText().toString();
        EditText appNameView = findViewById(R.id.appName);
        String appName = appNameView.getText().toString();

        Matcher matcher = IP_ADDRESS.matcher(ipv4Address);
        if (!matcher.matches()) {
            responseText.setText("Invalid IPv4 Address. Please Check Your Inputs.");
            return;
        }

        String postUrl = "http://" + ipv4Address + ":" + portNumber + "/predict";

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        multipartBodyBuilder.addFormDataPart("name", appName);

        if (imagesSelected == true) {
            for (int i = 0; i < selectedImagesPaths.size(); i++) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try {
                    // Read BitMap by file path.
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagesPaths.get(i), options);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                } catch (Exception e) {
                    Log.d("FAIL", e.getMessage());
                    responseText.setText("Please Make Sure the Selected File is an Image or This app has proper permissions.");
                    return;
                }
                byte[] byteArray = stream.toByteArray();

                multipartBodyBuilder
                        .addFormDataPart("image", "Android_Flask_" + i + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
            }
        }
        if (nirSelected == true) {
            for(int i = 0; i < use_button.length; i++) {
                if (use_button[i].use_this == true) {
                    for(int j = 0; j < nir_values[i].length; j++) {
                        multipartBodyBuilder.addFormDataPart("v", nir_values[i][j]);
                    }
                }
            }
        }

        RequestBody postBodyImage = multipartBodyBuilder.build();
        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText("Failed to Connect to Server. Please Try Again.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String temporal = null;
                try {
                    temporal = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final String response_str = temporal;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText("Server's Response :\n" + response_str);
                    }
                });
            }
        });
    }

    public void selectImage(View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_MULTIPLE_IMAGES);
    }

    public void setFlag(int num, boolean set_true) {
        for(int i = 0; i < use_button.length; i++) {
            if (i == num && set_true == true) {
                use_button[i].use_this = true;
            }
            else {
                use_button[i].use_this = false;
            }
        }
    }

    public String get_values_string(int buttom_num){
        String values_string = "";
        for(int i = 0; i < nir_values[buttom_num].length; i++) {
            int end_num = 6;
            if (nir_values[buttom_num][i].length() < end_num) {
                end_num = nir_values[buttom_num][i].length();
            }
            values_string += ("[" + i + "]" + nir_values[buttom_num][i].substring(0, end_num) + ", ");
        }
        return values_string;
    }

    public void useNirButton(View v) {
        int buttom_num = -1;
        TextView checkNIR = findViewById(R.id.checkNIR);
        switch (v.getId()) {
            case (R.id.button0):
                buttom_num = 0;
                break;
            case (R.id.button1):
                buttom_num = 1;
                break;
            case (R.id.button2):
                buttom_num = 2;
                break;
            case (R.id.button3):
                buttom_num = 3;
                break;
            default:
                buttom_num = -1;
                break;
        }
        if (use_button[buttom_num].use_this == false) {
            nirSelected = true;
            String values_string = get_values_string(buttom_num);
            checkNIR.setText(nir_names[buttom_num] + ", " + values_string);
            setFlag(buttom_num, true);
        }
        else {
            nirSelected = false;
            checkNIR.setText("No NIR Data Selected.");
            setFlag(buttom_num, false);
        }
    }

    String currentImagePath = null;
    Uri uri = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == SELECT_MULTIPLE_IMAGES && resultCode == RESULT_OK && null != data) {
                // When a single image is selected.

                selectedImagesPaths = new ArrayList<>();
                TextView numSelectedImages = findViewById(R.id.numSelectedImages);
                if (data.getData() != null) {
//                    Uri uri = data.getData();
                    uri = data.getData();
                    currentImagePath = getPath(getApplicationContext(), uri);
                    Log.d("ImageDetails", "Single Image URI : " + uri);
                    Log.d("ImageDetails", "Single Image Path : " + currentImagePath);
                    selectedImagesPaths.add(currentImagePath);
                    imagesSelected = true;
                    numSelectedImages.setText("Number of Selected Images : " + selectedImagesPaths.size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath, options);
//                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
//                            Bitmap myBitmap = BitmapFactory.decodeFile(currentImagePath);
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            Log.d("ImageDetails", "width : " + width);
                            Log.d("ImageDetails", "height : " + height);
//                            Bitmap new = Bitmap.createBitmap(src, x, y, cw, ch);
                            int patch_size = 0;
                            if (width < height) {
                                patch_size = width;
                            }
                            else {
                                patch_size = height;
                            }

                            Bitmap new_bitmap = Bitmap.createBitmap(bitmap, width - patch_size, height - patch_size, patch_size, patch_size);
                            ImageView myImage = (ImageView) findViewById(R.id.imageView1);
                            myImage.setImageBitmap(new_bitmap);
//                            myImage.setImageURI(uri);
                        }
                    });
                } else {
                    // When multiple images are selected.
                    // Thanks tp Laith Mihyar for this Stackoverflow answer : https://stackoverflow.com/a/34047251/5426539
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {

                            ClipData.Item item = clipData.getItemAt(i);
//                            Uri uri = item.getUri();
                            uri = item.getUri();
                            currentImagePath = getPath(getApplicationContext(), uri);
                            selectedImagesPaths.add(currentImagePath);
                            Log.d("ImageDetails", "Image URI " + i + " = " + uri);
                            Log.d("ImageDetails", "Image Path " + i + " = " + currentImagePath);
                            imagesSelected = true;
                            numSelectedImages.setText("Number of Selected Images : " + selectedImagesPaths.size());
                        }
                    }
                }
            } else {
                Toast.makeText(this, "You haven't Picked any Image.", Toast.LENGTH_LONG).show();
            }

            Toast.makeText(getApplicationContext(), selectedImagesPaths.size() + " Image(s) Selected.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Something Went Wrong.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}