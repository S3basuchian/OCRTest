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

    MLKitTask(MainActivity mainActivity, FirebaseVisionTextRecognizer textRecognizer,
              ArrayList<ListEntry> list) {
        this.list = list;
        this.mainActivity = mainActivity;
        this.textRecognizer = textRecognizer;
        this.time = new long[list.size()];
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        for (int i = 0; i < bitmaps.length; i++){
            time[i] = System.currentTimeMillis();
            final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmaps[i]);
            final int finalI = i;
            Runnable runnable = new Runnable() {
                public void run() {
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
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // failed
                                        }
                                    });
                }
            };
            new Thread(runnable).start();
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
