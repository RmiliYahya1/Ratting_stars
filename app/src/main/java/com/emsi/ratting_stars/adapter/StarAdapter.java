package com.emsi.ratting_stars.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.emsi.ratting_stars.R;
import com.emsi.ratting_stars.bean.Star;
import com.emsi.ratting_stars.service.StarService;

import java.util.ArrayList;
import java.util.List;

public class StarAdapter  extends RecyclerView.Adapter<StarAdapter.StarViewHolder>  implements Filterable {
    private static final String TAG = "StarAdapter";
    private List<Star> stars;
    private List<Star> starsFilter;
    private Context context;
    private NewFilter mfilter;
    public StarAdapter(Context context, List<Star> stars) {
        this.stars = stars;
        this.context = context;
        this.starsFilter = new ArrayList<>(stars);
        this.mfilter = new NewFilter(this);
    }

    @NonNull
    @Override
    public StarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.star_item, viewGroup, false);
        final StarViewHolder holder = new StarViewHolder(v);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popup = LayoutInflater.from(context).inflate(R.layout.star_edit_item, null, false);
                final ImageView img = popup.findViewById(R.id.img);
                final RatingBar bar = popup.findViewById(R.id.ratingBar);
                final TextView idss = popup.findViewById(R.id.idss);

                ImageView starImageView = v.findViewById(R.id.img);
                Drawable drawable = starImageView.getDrawable();

                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    img.setImageBitmap(bitmap);
                } else {

                    img.setImageResource(R.mipmap.star);
                }


                bar.setRating(((RatingBar) v.findViewById(R.id.stars)).getRating());
                idss.setText(((TextView) v.findViewById(R.id.ids)).getText().toString());


                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Notez : ")
                        .setMessage("Donner une note entre 1 et 5 :")
                        .setView(popup)
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                float s = bar.getRating();
                                int ids = Integer.parseInt(idss.getText().toString());
                                Star star = StarService.getInstance().findById(ids);
                                star.setStar(s);
                                StarService.getInstance().update(star);
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .create();
                dialog.show();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Récupérer la position et l'identifiant de la star
                int position = holder.getAdapterPosition();
                final Star starToDelete = starsFilter.get(position);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Supprimer")
                        .setMessage("Voulez-vous supprimer " + starToDelete.getName() + " ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                StarService.getInstance().delete(starToDelete);

                                // Mettre à jour les listes
                                starsFilter.remove(starToDelete);
                                stars.remove(starToDelete);


                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Non", null)
                        .create();

                dialog.show();
                return true;
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull StarViewHolder holder, int position) {
        Log.d(TAG, "onBindView call ! "+ position);
        Glide.with(context)
                .asBitmap()
                .load(starsFilter.get(position).getImg())
                .apply(new RequestOptions().override(100, 100))
                .into(holder.img);
        holder.name.setText(starsFilter.get(position).getName().toUpperCase());
        holder.stars.setRating(starsFilter.get(position).getStar());
        holder.idss.setText(starsFilter.get(position).getId()+"");
    }

    @Override
    public int getItemCount() {
        return starsFilter.size();
    }

    @Override
    public Filter getFilter() {
        return mfilter;
    }

    public class StarViewHolder extends RecyclerView.ViewHolder {
        TextView idss;
        ImageView img;
        TextView name;
        RatingBar stars;
        RelativeLayout parent;

        public StarViewHolder(@NonNull View itemView) {
            super(itemView);
            idss = itemView.findViewById(R.id.ids);
            img = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.name);
            stars = itemView.findViewById(R.id.stars);
            parent = itemView.findViewById(R.id.parent);
        }
    }

    public class NewFilter extends Filter {
        public RecyclerView.Adapter mAdapter;
        public NewFilter(RecyclerView.Adapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
        }
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            starsFilter.clear();
            final FilterResults results = new FilterResults();
            if (charSequence.length() == 0) {
                starsFilter.addAll(stars);
            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Star p : stars) {
                    if (p.getName().toLowerCase().startsWith(filterPattern)) {
                        starsFilter.add(p);
                    }
                }
            }
            results.values = starsFilter;
            results.count = starsFilter.size();
            return results;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            starsFilter = (List<Star>) filterResults.values;
            this.mAdapter.notifyDataSetChanged();
        }
    }

}
