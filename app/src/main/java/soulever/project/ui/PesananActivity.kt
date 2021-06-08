package soulever.project.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import soulever.project.R
import soulever.project.databinding.ActivityPesananBinding
import soulever.project.entity.Recommended
import soulever.project.utils.DummyData

class PesananActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPesananBinding
    private var jumlah = 1
    private var totalHarga = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.action_bar);
        var nomorHpRumahKemasan: String = ""
        var strMessage: String = ""
        val listRumahKemasan = DummyData.getAllRumahKemasan()
        val namaSeluruhRumahKemasan = ArrayList<String>()


        val itemRecommended = intent.getParcelableExtra<Recommended>("extra_item")
        if (itemRecommended != null) {

            Glide.with(this)
                .load(itemRecommended.image)
                .fitCenter()
                .apply(
                    RequestOptions.placeholderOf(R.drawable.ic_baseline_loading_24)
                        .error(R.drawable.ic_error)
                )
                .into(binding.ivPesanan)

            binding.tvJenisKemasan.text = itemRecommended.kemasan
            binding.tvBahan.text = itemRecommended.bahan
            binding.tvJenisProduk.text = itemRecommended.jenis
            binding.tvHarga.text = itemRecommended.harga
            binding.tvWarna.text = itemRecommended.warna

        }

        for (i in 0..listRumahKemasan.size - 1) {
            namaSeluruhRumahKemasan.add(listRumahKemasan[i].Nama)
        }

        val arrayAdapterSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            namaSeluruhRumahKemasan
        )
        arrayAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(binding.spinnerRumahKemasan)
        {
            adapter = arrayAdapterSpinner
            setSelection(0, false)
            prompt = "Pilih Rumah Kemasan yang diinginkan"
            gravity = Gravity.CENTER
        }

        totalHarga = itemRecommended!!.harga!!.toInt()
        binding.totalPrice.text = ("Rp. $totalHarga")

        binding.btnPlus.setOnClickListener {
            jumlah += 1
            binding.quantity.text = jumlah.toString()
            totalHarga += itemRecommended.harga!!.toInt()
            binding.totalPrice.text = ("Rp. $totalHarga")
        }
        binding.totalPrice.text = ("Rp. $totalHarga")

        binding.btnMinus.setOnClickListener {
            jumlah -= 1
            totalHarga -= itemRecommended.harga!!.toInt()
            if (jumlah <= 0) {
                jumlah = 0
                totalHarga = 0
                binding.totalPrice.text = ("Rp. 0")
                binding.quantity.text = "0"

            } else {
                binding.quantity.text = jumlah.toString()
                totalHarga += itemRecommended.harga.toInt()
                binding.totalPrice.text = ("Rp. $totalHarga")
            }
        }

        if (itemRecommended != null) {
            binding.btnMinus.setOnClickListener {
                jumlah -= 1
                totalHarga -= itemRecommended.harga?.toInt() ?: 0
                if (jumlah <= 0) {
                    jumlah = 0
                    totalHarga = 0
                    binding.totalPrice.text = ("Rp. 0")
                    binding.quantity.text = "0"
                } else {
                    binding.quantity.text = jumlah.toString()
                    binding.totalPrice.text = ("Rp. $totalHarga")
                }


            }
        }




        binding.spinnerRumahKemasan.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Toast.makeText(
                        this@PesananActivity,
                        namaSeluruhRumahKemasan[position] + " selected",
                        Toast.LENGTH_LONG
                    ).show()
                    for (i in 0..listRumahKemasan.size - 1) {
                        if (namaSeluruhRumahKemasan[position] == listRumahKemasan[i].Nama) {
                            nomorHpRumahKemasan = listRumahKemasan[i].Kontak
                        }
                    }

                    nomorHpRumahKemasan = nomorHpRumahKemasan.replace("-", "")
                    nomorHpRumahKemasan =
                        "+62" + nomorHpRumahKemasan.substring(1, nomorHpRumahKemasan.length)
                    Log.d("NomorHP", nomorHpRumahKemasan)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding.btnPesan.setOnClickListener {

            if (itemRecommended != null) {
                strMessage =
                    "Hallo\nSaya memesan kemasan dengan spesifikasi berikut:\n\nJenis Pesanan ${itemRecommended.jenis} dengan Jumlah $jumlah dan Harga : Rp. ${totalHarga}\nContoh Gambar : ${itemRecommended.image}\nFile Template Desain Kemasan: https://www.figma.com/file/XXeIZ5KibShc3nTlXU13z0/Desain-Kemasan"
            }


            val installed: Boolean = appInstalledOrNot("com.whatsapp")

            if (installed) {
                val intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        "https://api.whatsapp.com/send?phone=" + "+6285774342477"
                                + "&text=" + strMessage
                    )
                )
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "WhatsApp not installed on your Device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val packageManager = packageManager
        val appInstalled: Boolean
        appInstalled = try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return appInstalled
    }

    @SuppressLint("CheckResult")
    private fun showItem(itemRecommended: Recommended) {

    }

}