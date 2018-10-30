package bachelor.sebas.ocrtest;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;

public class MLKitTask extends AsyncTask<Bitmap, Void, Void> {

    private ArrayList<ListEntry> list;
    private MainActivity mainActivity;
    private FirebaseVisionTextRecognizer textRecognizer;
    private long[] time;
    private boolean start;

    MLKitTask(MainActivity mainActivity, FirebaseVisionTextRecognizer textRecognizer,
              ArrayList<ListEntry> list) {
        this.list = list;
        this.mainActivity = mainActivity;
        this.textRecognizer = textRecognizer;
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
                textRecognizer.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText result) {
                                String text = result.getText();
                                list.get(finalI).setMlKit(text);
                                long duration = System.currentTimeMillis() - time[finalI];
                                list.get(finalI).setDurMLKit(duration);
                                MainActivity.writeToSDFile(list.get(finalI), 0);
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
