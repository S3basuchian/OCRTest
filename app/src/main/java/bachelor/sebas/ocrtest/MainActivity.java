package bachelor.sebas.ocrtest;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private ArrayList<ListEntry> list;
    private ArrayList<Bitmap> imageList;
    private RecyclerView mRecyclerView;
    private ProgressBar spinner;
    private TessBaseAPI mTess;
    private FirebaseVisionTextRecognizer textRecognizer;
    private FirebaseVisionDocumentTextRecognizer cloudTextRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        list = new ArrayList<>();
        imageList = new ArrayList<>();
        ListAdapter mAdapter = new ListAdapter(list, this);
        mRecyclerView.setAdapter(mAdapter);

        mTess = new TessBaseAPI();
        //new language files can be obtained here: https://github.com/tesseract-ocr/tessdata/tree/3.04.00
        mTess.init(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "eng");

        FirebaseApp.initializeApp(this);
        FirebaseVisionCloudDocumentRecognizerOptions options =
                new FirebaseVisionCloudDocumentRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("en"))
                        .build();
        cloudTextRecognizer = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer(options);
        textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        findViewById(R.id.bChooseFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        });
    }

    public static void writeToSDFile(ListEntry listEntry, int method) {
        File root = android.os.Environment.getExternalStorageDirectory();

        String name = listEntry.getName().replace(".png", "");
        String ocr = null;
        String text = null;
        long duration = 0;
        switch (method) {
            case 0:
                ocr = "mlkit";
                duration = listEntry.getDurMLKit();
                text = listEntry.getMlKit();
                break;
            case 1:
                ocr = "tess";
                duration = listEntry.getDurTess();
                text = listEntry.getTess();
                break;
            case 2:
                ocr = "mlkitCloud";
                duration = listEntry.getDurMLKitCloud();
                text = listEntry.getMlKitCloud();
        }
        String[] lines = text.split("\n");

        File dir = new File(root.getAbsolutePath() + "/download/results/" + ocr);
        dir.mkdirs();
        File file = new File(dir, name + ".xml");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
            pw.println();
            pw.println("<!DOCTYPE form SYSTEM 'http://www.iam.unibe.ch/~fki/iamdb/form-metadata.dtd'>");
            pw.println();
            pw.println("<form duration=\"" + duration + "ms\">");
            pw.println("<ocr-interpreted-text>");
            for (String line : lines) {
                pw.println("<ocr-interpreted-line>" + line + "</ocr-interpreted-line>");
            }
            pw.println("</ocr-interpreted-text>");
            pw.println("</form>");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void performFileSearch() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(pickPhoto, READ_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                ClipData clipData = resultData.getClipData();
                if (clipData != null) {
                    list.clear();
                    imageList.clear();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        try {
                            Bitmap image = getBitmapFromUri(uri);
                            String name = getFileName(uri);
                            list.add(new ListEntry(image, uri, name));
                            imageList.add(image);
                        } catch (IOException e) {
                            //sad
                        }
                    }
                    //addImagesToList();
                    workOverList();
                }
            }
        }
    }

    private void workOverList() {
        new MLKitTask(this, textRecognizer, list).execute(imageList.toArray(new Bitmap[imageList.size()]));
        new MLKitCloudTask(this, cloudTextRecognizer, list).execute(imageList.toArray(new Bitmap[imageList.size()]));
        new TessTask(this, mTess, list).execute(imageList.toArray(new Bitmap[imageList.size()]));
    }

    public void updateProgress() {
        mRecyclerView.swapAdapter(new ListAdapter(list, MainActivity.this), true);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
