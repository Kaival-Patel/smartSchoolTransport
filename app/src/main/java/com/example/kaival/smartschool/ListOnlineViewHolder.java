package com.example.kaival.smartschool;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtEmail;
    public TextView txtlogintype;
    ItemClicklistener itemClicklistener;
    View mview;
    public ListOnlineViewHolder(View itemView)
    {
        super(itemView);
        mview=itemView;
        txtlogintype=itemView.findViewById(R.id.logintypetxt);
        txtEmail=(TextView)itemView.findViewById(R.id.textemail);
    }

    public void setItemClicklistener(ItemClicklistener itemClicklistener) {
        this.itemClicklistener = itemClicklistener;
    }

    @Override
    public void onClick(View view) {
        itemClicklistener.onClick(view,getAdapterPosition());
    }
}
