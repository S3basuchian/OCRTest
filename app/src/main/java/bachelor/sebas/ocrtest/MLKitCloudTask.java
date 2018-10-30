package bachelor.sebas.ocrtest;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;

import java.util.ArrayList;

public class MLKitCloudTask extends AsyncTask<Bitmap, Void, Void> {

    private ArrayList<ListEntry> list;
    private MainActivity mainActivity;
    private FirebaseVisionDocumentTextRecognizer cloudTextRecognizer;
    private long[] time;
    private boolean start;

    MLKitCloudTask(MainActivity mainActivity,
                   FirebaseVisionDocumentTextRecognizer cloudTextRecognizer,
                   ArrayList<ListEntry> list) {
        this.list = list;
        this.mainActivity = mainActivity;
        this.cloudTextRecognizer = cloudTextRecognizer;
        this.time = new long[list.size()];
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        int i = 0;
        start = true;
        while (i < bitmaps.length) {
            if (start) {
                time[i] = System.currentTimeMillis();
                Bitmap current = bitmaps[i];
                final int finalI = i;
                i++;
                start = false;
                final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(current);
                cloudTextRecognizer.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                            @Override
                            public void onSuccess(FirebaseVisionDocumentText result) {
                                String text = result.getText();
                                list.get(finalI).setMlKitCloud(text);
                                long duration = System.currentTimeMillis() - time[finalI];
                                list.get(finalI).setDurMLKitCloud(duration);
                                MainActivity.writeToSDFile(list.get(finalI), 2);
                                publishProgress();
                                start = true;
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // failed
                                        start = true;
                                    }
                                });
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        mainActivity.updateProgress();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        mainActivity.updateProgress();
    }
}
