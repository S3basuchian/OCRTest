package bachelor.sebas.ocrtest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {
    private ArrayList<ListEntry> mDataset;
    private Context mContext;


    static class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout tile;
        MyViewHolder(LinearLayout v) {
            super(v);
            tile = v;
        }
    }

    ListAdapter(ArrayList<ListEntry> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public ListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tile_listentry, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ListEntry listEntry = mDataset.get(position);
        ImageView doc_image = holder.tile.findViewById(R.id.doc_img);
        TextView doc_mlkit = holder.tile.findViewById(R.id.doc_mlkit);
        TextView doc_mlkit_cloud = holder.tile.findViewById(R.id.doc_mlkit_cloud);
        TextView doc_tess = holder.tile.findViewById(R.id.doc_tess);
        doc_image.setImageBitmap(listEntry.getPicture());
        doc_mlkit.setText(listEntry.getMlKit());
        doc_mlkit_cloud.setText(listEntry.getMlKitCloud());
        doc_tess.setText(listEntry.getTess());
        doc_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(listEntry.getUri(), "image/*");
                mContext.startActivity(intent);
            }
        });
        doc_mlkit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(listEntry.getMlKit())
                        .setTitle("MLKit " + listEntry.getDurMLKit() + "ms");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        doc_mlkit_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(listEntry.getMlKitCloud())
                        .setTitle("MLKit Cloud " + listEntry.getDurMLKitCloud() + "ms");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        doc_tess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(listEntry.getTess())
                        .setTitle("Tesseract " + listEntry.getDurTess() + "ms");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
