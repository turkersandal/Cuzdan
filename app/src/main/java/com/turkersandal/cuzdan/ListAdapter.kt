package com.turkersandal.cuzdan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

var mAuth = FirebaseAuth.getInstance().currentUser
class ListAdapter(val mCtx:Context,val layoutResId:Int,val harcamaList:List<Harcama>) : ArrayAdapter<Harcama>(mCtx,layoutResId,harcamaList)

{
    var guncel = 0.0
    val sdf = SimpleDateFormat("d:M:yyyy")
    val currentDate = sdf.format(Date())
    var databaseGelir = FirebaseDatabase.getInstance().getReference("${mAuth!!.uid}/Gelir")
    var database_harcama = FirebaseDatabase.getInstance().getReference("${mAuth!!.uid}/Harcama/${currentDate}")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = LayoutInflater.from(mCtx)
        val view = layoutInflater.inflate(layoutResId,null)

        val harcama = harcamaList[position]

        val urunAd= view.findViewById<TextView>(R.id.txtAd)
        val urunMiktar = view.findViewById<TextView>(R.id.txtPara)
        val total = view.findViewById<TextView>(R.id.textViewLstTotal)

        databaseGelir.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {

                if(p0!!.exists()){
                    for(h in p0.children)
                    {
                        var gelir=h.getValue(Gelir::class.java)
                        total.text = gelir!!.miktar.toString()


                    }


                }
            }


        })


        val editButton = view.findViewById<Button>(R.id.btnEdit)
        val delButton = view.findViewById<Button>(R.id.btnSil)


        urunAd.text=harcama.urunAd
        urunMiktar.text=harcama.urunMiktar.toString()

        editButton.setOnClickListener {

            val builder = android.app.AlertDialog.Builder(mCtx)
            builder.setTitle("Güncelle")
            val alertEdit = LayoutInflater.from(mCtx)
            val view2 = alertEdit.inflate(R.layout.dialog_gider_update,null)

            val editName = view2.findViewById<EditText>(R.id.edtUpdateName)
            val editPrize = view2.findViewById<EditText>(R.id.edtUpdatePrize)

            editName.setText(harcama.urunAd)
            editPrize.setText(harcama.urunMiktar.toString())

            builder.setView(view2)
            builder.setPositiveButton("Güncelle"){dialog, which ->


                var harcama = Harcama(harcama.id,editName.text.toString(),editPrize.text.toString().toDouble())
                database_harcama.child(harcama.id).setValue(harcama).addOnSuccessListener {


                    var yeni = editPrize.text.toString().toDouble()
                    var eski=urunMiktar.text.toString().toDouble()
                    var fark= abs(yeni-eski)


                    if(yeni>eski){
                        guncel = total.text.toString().toDouble()-fark
                        var gelir = Gelir("1",guncel)
                        databaseGelir.child("1").setValue(gelir).addOnSuccessListener {
                            Toast.makeText(mCtx,"Gider Güncellendi",Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        guncel=total.text.toString().toDouble()+fark
                        var gelir = Gelir("1",guncel)
                        databaseGelir.child("1").setValue(gelir).addOnSuccessListener {
                            Toast.makeText(mCtx,"Gider Güncellendi",Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            }
            val alert = builder.create()
            alert.show()


        }
        delButton.setOnClickListener {
            val sdf = SimpleDateFormat("d:M:yyyy")
            val currentDate = sdf.format(Date())

            var database= FirebaseDatabase.getInstance().getReference("${mAuth!!.uid}/Harcama/${currentDate}")
            database.child(harcama.id).removeValue().addOnSuccessListener {
                Toast.makeText(mCtx,"Silindi.",Toast.LENGTH_SHORT).show()
                guncel=urunMiktar.text.toString().toDouble()+total.text.toString().toDouble()
                var gelir = Gelir("1",guncel)
                databaseGelir.child("1").setValue(gelir)


            }
        }

        return view

    }

}