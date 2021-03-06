package com.xy.xydoctor.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xy.xydoctor.R;
import com.xy.xydoctor.bean.FamilyMemberBean;

import java.util.List;

public class FamilyMemberNewAdapter extends BaseQuickAdapter<FamilyMemberBean, BaseViewHolder> {
    private boolean bl;

    public FamilyMemberNewAdapter(@Nullable List<FamilyMemberBean> data) {
        super(R.layout.item_home_sign, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, FamilyMemberBean personBean) {
        holder.setText(R.id.tv_name, personBean.getNickname());
        holder.setText(R.id.tv_identity, getRelationString(personBean.getRelation()));
        holder.setText(R.id.tv_phone_number, personBean.getTel());
        holder.setText(R.id.tv_id_number, personBean.getIdcard());
        ImageView ivRemove = holder.getView(R.id.iv_remove);
        if (bl) {
            holder.setVisible(R.id.iv_arrow, false);
            ivRemove.setVisibility(View.VISIBLE);
        } else {
            holder.setVisible(R.id.iv_arrow, true);
            ivRemove.setVisibility(View.GONE);
        }

        //        holder.getConvertView().setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                onItemClickListener.OnItemClick(v, position);
        //            }
        //        });
        //
        //        ivRemove.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                onItemRemoveListener.OnItemRemove(personBean.getId());
        //            }
        //        });
    }

    private String getRelationString(int rel) {
        String relation = "";
        switch (rel) {
            case 1:
                relation = "??????";
                break;
            case 2:
                relation = "??????";
                break;
            case 3:
                relation = "???";
                break;
            case 4:
                relation = "??????";
                break;
            case 5:
                relation = "???";
                break;
            case 6:
                relation = "??????";
                break;
            case 7:
                relation = "??????????????????";
                break;
            case 8:
                relation = "??????";
                break;
        }
        return relation;
    }

    public void setRemoveButton(boolean bl) {
        this.bl = bl;
        notifyDataSetChanged();
    }
}
