package com.turkersandal.cuzdan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ListGecmis(val mCtx:Context,val layoutResId:Int,val harcamaList:List<Harcama>)
    :ArrayAdapter<Harcama>(mCtx,layoutResId,harcamaList)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = LayoutInflater.from(mCtx)
        val view = layoutInflater.inflate(layoutResId,null)
        val harcama = harcamaList[position]

        val urunAd = view.findViewById<TextView>(R.id.textGecmis)
        val urunMiktar = view.findViewById<TextView>(R.id.textGecmisPrize)

        urunAd.setText(harcama.urunAd)
        urunMiktar.setText(harcama.urunMiktar.toString())

        return view

    }

}