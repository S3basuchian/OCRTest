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
        for (int i = 0; i < bitmaps.length; i++){
            time[i] = System.currentTimeMillis();
            final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmaps[i]);
            final int finalI = i;
            Runnable runnable = new Runnable() {
                public void run() {
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
