package bachelor.sebas.ocrtest;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;

public class TessTask extends AsyncTask<Bitmap, Void, Void> {

    private ArrayList<ListEntry> list;
    private TessBaseAPI mTess;
    private MainActivity mainActivity;
    private long[] time;

    TessTask(MainActivity mainActivity, TessBaseAPI mTess, ArrayList<ListEntry> list) {
        this.mTess = mTess;
        this.list = list;
        this.mainActivity = mainActivity;
        this.time = new long[list.size()];
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        for (int i = 0; i < bitmaps.length; i++){
            time[i] = System.currentTimeMillis();
            mTess.setImage(bitmaps[i]);
            String text = mTess.getUTF8Text();
            list.get(i).setTess(text);
            long duration = System.currentTimeMillis() - time[i];
            list.get(i).setDurTess(duration);
            MainActivity.writeToSDFile(list.get(i), 1);
            publishProgress();
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
