package org.bcss.collect.naxa.contact;
/**
 * Add the contact inforamation instance to the recycler view
 *
 * @author Nishon Tandukar
 */

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.bcss.collect.android.R;
import org.bcss.collect.naxa.common.GlideApp;

import java.util.List;

import io.reactivex.annotations.NonNull;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    private List<FieldSightContactModel> contactList;
    private ContactDetailListener contactDetailListener;
    private Context context;


    ContactAdapter(List<FieldSightContactModel> contactList, @NonNull ContactDetailListener contactDetailListener, Context context) {
        this.contactList = contactList;
        this.contactDetailListener = contactDetailListener;
        this.context = context;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFullName, tvUserName, tvPhone;
        private ImageView ivProfilePicture;

        private CardView card;

        MyViewHolder(View view) {
            super(view);

            ivProfilePicture = view.findViewById(R.id.imageView2);
            tvFullName = view.findViewById(R.id.contact_name);
            tvUserName = view.findViewById(R.id.contact_username);
            card = view.findViewById(R.id.card_contact_list_item);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    contactDetailListener.onContactClicked(contactList.get(pos));
                }
            });
        }
    }


    @android.support.annotation.NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@android.support.annotation.NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_list_item, parent, false);

        context = parent.getContext();


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@android.support.annotation.NonNull final MyViewHolder holder, int position) {
        final FieldSightContactModel contact = contactList.get(position);
        holder.tvFullName.setText(contact.getFull_name());
        holder.tvUserName.setText(contact.getEmail());
        /*
        //open tvSkype
        holder.tvSkype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Opening Skype", Toast.LENGTH_SHORT).show();
            }
        });


        //open tvFacebook
        holder.tvFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Opening Facebook", Toast.LENGTH_SHORT).show();

                Uri uri = Uri.parse("fb-messenger://user/100005727815736");
                //uri = ContentUris.withAppendedId(uri,[100005727815736]);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
                try {
                    context.startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context, "Please Install Facebook Messenger", Toast.LENGTH_LONG).show();
                }
            }
        });

        //open phone
        holder.phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Opening Phone", Toast.LENGTH_SHORT).show();

                //TODO get phone number form localdatabase
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getPhone().toString()));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                context.startActivity(intent);
            }
        });
        //open tvSkype
        holder.tvMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Opening Message", Toast.LENGTH_SHORT).show();

                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", contact.getPhone().toString());
                context.startActivity(smsIntent);
            }
        });
*/
        String Img_Thumb_Url = contact.getProfilePicture();

        if (Img_Thumb_Url != null) {
            GlideApp.with(context)
                    .load(Img_Thumb_Url)
                    .circleCrop()
                    .thumbnail(0.5f)
                    .into(holder.ivProfilePicture);
        }
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public interface ContactDetailListener {
        void onContactClicked(FieldSightContactModel contactModel);

    }
}