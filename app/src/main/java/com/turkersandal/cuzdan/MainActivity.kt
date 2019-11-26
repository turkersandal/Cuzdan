package com.turkersandal.cuzdan


import android.app.DatePickerDialog
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_statics.view.*
import kotlinx.android.synthetic.main.dialog_gecmis.view.*
import kotlinx.android.synthetic.main.dialog_gelir_ekle.view.*
import kotlinx.android.synthetic.main.dialog_gider_ekle.view.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {



     var mInterstitialAd:InterstitialAd?=null



    val sdf_month = SimpleDateFormat("M")
    val sdf_year = SimpleDateFormat("YYYY")
    val ay = sdf_month.format(Date())
    val yil = sdf_year.format(Date())


    var mAuth = FirebaseAuth.getInstance().currentUser
    val database_statics = FirebaseDatabase.getInstance().getReference("${mAuth!!.uid}/Harcama")

    lateinit var pieChart:PieChart
    lateinit var harcamaList:MutableList<Harcama>
    lateinit var gecmisList:MutableList<Harcama>
    val sdf = SimpleDateFormat("d:M:yyyy")
    val currentDate = sdf.format(Date())
    var database = FirebaseDatabase.getInstance().getReference("${mAuth!!.uid}/Gelir")
    var database_harcama = FirebaseDatabase.getInstance().getReference("${mAuth!!.uid}/Harcama/${currentDate}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        harcamaList = mutableListOf()
        gecmisList= mutableListOf()


        txtGelir.setOnClickListener{

            var alertDialog = LayoutInflater.from(this).inflate(R.layout.dialog_gelir_ekle,null)
            var builder = AlertDialog.Builder(this).setTitle("Gelir Ekle").setView(alertDialog)
                .setPositiveButton("Ekle"){dialog, which ->


                    if(!alertDialog.dialogGelir.text.isEmpty()) {
                        var rev = Gelir(
                            "1",
                            (txtGelir.text.toString().toDouble() + alertDialog.dialogGelir.text.toString().toDouble())
                        )
                        database.child("1").setValue(rev).addOnSuccessListener {
                            Toast.makeText(this,"Gelir Eklendi",Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this,"Lütfen Alanı Eksikisiz Doldurunuz.",Toast.LENGTH_SHORT).show()

                    }

                }
            builder.setNegativeButton("Kapat"){dialog, which -> }

            var mAlert =  builder.show()


        }




        //BANNER REKLAM
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)


        //GELİR ÇEKME
        database.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(baseContext,"",Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0!!.exists()) {
                    for(h in p0.children){

                        var gelir = h.getValue(Gelir::class.java)
                        txtGelir.text = gelir!!.miktar.toString()

                    }
                }
            }
        })

        //HARCAMA ÇEKME
        database_harcama.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {


                harcamaList.clear()
                if(p0!!.exists()){
                    for(h in p0.children){
                        var harcama = h.getValue(Harcama::class.java)
                        harcamaList.add(harcama!!)


                    }
                    val adapter = ListAdapter(this@MainActivity,R.layout.list,harcamaList)
                    harcamaListesi.adapter=adapter

                }

            }


        })

        //GİDER EKLEME
        imageButton2.setOnClickListener{
            val alertGider = LayoutInflater.from(this).inflate(R.layout.dialog_gider_ekle,null)
            val builder = AlertDialog.Builder(this).setTitle("Gider Ekle").setView(alertGider)
                .setNegativeButton("Kapat"){dialog, which ->  }
            var mGider = builder.show()
            alertGider.butonEkle.setOnClickListener {

                if(!alertGider.dialogEditText.text.isEmpty() && !alertGider.dialogEditText2.text.isEmpty()) {
                    var id = database_harcama.push().key
                    var harcama = Harcama(
                        id.toString(),
                        alertGider.dialogEditText.text.toString(),
                        alertGider.dialogEditText2.text.toString().toDouble()
                    )
                    database_harcama.child("$id").setValue(harcama).addOnSuccessListener {

                        Toast.makeText(this,"Gider Eklendi",Toast.LENGTH_SHORT).show()
                        var guncel = 0.0
                        guncel=txtGelir.text.toString().toDouble()-alertGider.dialogEditText2.text.toString().toDouble()
                        var gelir = Gelir("1",guncel)
                        database.child("1").setValue(gelir)





                    }
                }else
                {
                    Toast.makeText(this,"Lütfen alanları eksiksiz doldurunuz.",Toast.LENGTH_SHORT).show()

                }

            }



        }


    }


    //GECMİS GORUNTULEME FONKSİYONU
    private fun alertGecmis(tarih:String){

        var database= FirebaseDatabase.getInstance().getReference("${mAuth!!.uid}/Harcama/${tarih}")
        val alertGecmis = LayoutInflater.from(this).inflate(R.layout.dialog_gecmis,null)
        val builder = AlertDialog.Builder(this).setTitle("Gecmis Harcamalar/$tarih").setView(alertGecmis)
        database.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                gecmisList.clear()
                if(p0!!.exists()){
                    for(h in p0.children)
                    {
                        var harcama = h.getValue(Harcama::class.java)
                        gecmisList.add(harcama!!)

                    }
                    val adapter = ListGecmis(this@MainActivity,R.layout.list_gecmis,gecmisList)
                    alertGecmis.lstGecmis.adapter = adapter

                }
            }
        })
        builder.setNegativeButton("Kapat"){dialog, which ->
        }

        val mAlert= builder.show()



    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = menuInflater
        inflater.inflate(R.menu.menu,menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId){
            R.id.mnu_Gelir -> {

                var alertDialog = LayoutInflater.from(this).inflate(R.layout.dialog_gelir_ekle,null)
                var builder = AlertDialog.Builder(this).setTitle("Gelir Ekle").setView(alertDialog)
                    .setPositiveButton("Ekle"){dialog, which ->


                        if(!alertDialog.dialogGelir.text.isEmpty()) {
                            var rev = Gelir(
                                "1",
                                (txtGelir.text.toString().toDouble() + alertDialog.dialogGelir.text.toString().toDouble())
                            )
                            database.child("1").setValue(rev).addOnSuccessListener {
                                Toast.makeText(this,"Gelir Eklendi",Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(this,"Lütfen Alanı Eksikisiz Doldurunuz.",Toast.LENGTH_SHORT).show()

                        }

                    }
                builder.setNegativeButton("Kapat"){dialog, which -> }

                var mAlert =  builder.show()

            }
            R.id.mnu_Gider -> {
                val alertGider = LayoutInflater.from(this).inflate(R.layout.dialog_gider_ekle,null)
                val builder = AlertDialog.Builder(this).setTitle("Gider Ekle").setView(alertGider)
                    .setNegativeButton("Kapat"){dialog, which ->  }
                var mGider = builder.show()
                alertGider.butonEkle.setOnClickListener {

                    if(!alertGider.dialogEditText.text.isEmpty() && !alertGider.dialogEditText2.text.isEmpty()) {
                        var id = database_harcama.push().key
                        var harcama = Harcama(
                            id.toString(),
                            alertGider.dialogEditText.text.toString(),
                            alertGider.dialogEditText2.text.toString().toDouble()
                        )
                        database_harcama.child("$id").setValue(harcama).addOnSuccessListener {

                            var guncel = 0.0
                            guncel=txtGelir.text.toString().toDouble()-alertGider.dialogEditText2.text.toString().toDouble()
                            var gelir = Gelir("1",guncel)
                            database.child("1").setValue(gelir).addOnSuccessListener {
                                Toast.makeText(this,"Gider Eklendi",Toast.LENGTH_SHORT).show()
                            }


                        }
                    }else
                    {
                        Toast.makeText(this,"Lütfen alanları eksiksiz doldurunuz.",Toast.LENGTH_SHORT).show()

                    }

                }



            }
            R.id.mnu_Gecmis -> {

                val c =Calendar.getInstance()
                val year =c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(this,DatePickerDialog.OnDateSetListener{view:DatePicker , mYear , mMonth , mDay ->

                    alertGecmis("$mDay:${mMonth+1}:$mYear")
                    //Toast.makeText(this,"$mDay:${mMonth+1}:$mYear",Toast.LENGTH_SHORT).show()

                },year,month,day)
                dpd.show()

            }
            R.id.mnuGelirZero ->{


                var gelir = Gelir("1",0.0)
                database.child("1").setValue(gelir).addOnSuccessListener {
                    Toast.makeText(this,"Gelir Sıfırlandı",Toast.LENGTH_SHORT).show()
                }



            }
            R.id.mnuIstatistik -> {
                val alertStatics = LayoutInflater.from(this).inflate(R.layout.activity_statics,null)
                val builder = AlertDialog.Builder(this).setView(alertStatics)
                val yVals = ArrayList<PieEntry>()
                var toplam = 0.0

                database_statics.addValueEventListener(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        for(i in 0..31){
                        if (p0!!.exists()) {
                            for (h in p0.child("$i:$ay:$yil").children) {
                                var harcama = h.getValue(Harcama::class.java)
                                yVals.add(PieEntry(harcama!!.urunMiktar.toFloat(), harcama!!.urunAd))
                                toplam =toplam + harcama!!.urunMiktar
                               alertStatics.txtToplam.text="Bu Ay Yapılan Toplam Harcama:" + toplam.toString() +"TL"
                                val dataSet = PieDataSet(yVals, "")
                                dataSet.valueTextSize = 0f
                                dataSet.sliceSpace=2f



                              val colors = ArrayList<Int>()
                                colors.add(Color.rgb(255,165,0))
                                colors.add(Color.rgb(0,128,0))
                                colors.add(Color.BLUE)
                                colors.add(Color.RED)
                                colors.add(Color.GREEN)
                                colors.add(Color.MAGENTA)
                                colors.add(Color.YELLOW)



                                dataSet.setColors(colors)
                                val data = PieData(dataSet)
                                alertStatics.pieChart.data = data
                                alertStatics.pieChart.centerTextRadiusPercent = 0f
                                alertStatics.pieChart.isDrawHoleEnabled = false
                                alertStatics.pieChart.legend.isEnabled = true
                                alertStatics.pieChart.description.isEnabled = false
                                alertStatics.pieChart.isDrawHoleEnabled=true
                                alertStatics.pieChart.setHoleColor(Color.rgb(165, 241, 255))
                                alertStatics.pieChart.setExtraOffsets(5f,10f,5f,5f)
                                alertStatics.pieChart.invalidate()
                            }


                        }

                    }

                    }
                })
                //builder.setNegativeButton("Kapat"){dialog, which ->  }
                builder.show()



            }


        }


        return super.onOptionsItemSelected(item)
    }







}


