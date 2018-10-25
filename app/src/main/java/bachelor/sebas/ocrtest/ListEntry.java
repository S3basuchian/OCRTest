package bachelor.sebas.ocrtest;

import android.graphics.Bitmap;
import android.net.Uri;

public class ListEntry {

    private Bitmap picture;
    private String mlKit;
    private String mlKitCloud;
    private String tess;
    private String name;
    private long durMLKit;
    private long durMLKitCloud;
    private long durTess;
    private Uri uri;

    ListEntry(Bitmap picture, Uri uri, String name) {
        this.picture = picture;
        this.uri = uri;
        this.name = name;
    }

    Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    String getMlKit() {
        return mlKit;
    }

    void setMlKit(String mlKit) {
        this.mlKit = mlKit;
    }

    String getTess() {
        return tess;
    }

    void setTess(String tess) {
        this.tess = tess;
    }

    long getDurMLKit() {
        return durMLKit;
    }

    void setDurMLKit(long durMLKit) {
        this.durMLKit = durMLKit;
    }

    long getDurTess() {
        return durTess;
    }

    void setDurTess(long durTess) {
        this.durTess = durTess;
    }

    Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMlKitCloud() {
        return mlKitCloud;
    }

    public void setMlKitCloud(String mlKitCloud) {
        this.mlKitCloud = mlKitCloud;
    }

    public long getDurMLKitCloud() {
        return durMLKitCloud;
    }

    public void setDurMLKitCloud(long durMLKitCloud) {
        this.durMLKitCloud = durMLKitCloud;
    }
}
