package com.example.deeptraderspos.setting.unit

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.ActivityUnitBinding
import com.example.deeptraderspos.models.Units
import com.google.firebase.firestore.FirebaseFirestore

class UnitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnitBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var unitAdapter: UnitAdapter
    private val unitList = mutableListOf<Units>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUnitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.unit)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@UnitActivity, AddUnitActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        unitAdapter = UnitAdapter(
            units = unitList,
            this,
            onDeleteClicked = { units ->
                deleteUnit(units) { success ->
                    if (success) {
                        unitAdapter.removeItem(units)
                    } else {
                        // Handle error
                    }
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UnitActivity)
            adapter = unitAdapter
        }

        // Fetch units from Firestore
        fetchUnitsFromFirebase()

        // Implement search functionality
        binding.etxtUnitsSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList =
                    unitList.filter { it.unitName?.contains(s.toString(), true) == true }
                        .toMutableList()

                unitAdapter = UnitAdapter(
                    units = filteredList,
                    this@UnitActivity,
                    onDeleteClicked = { unit ->
                        deleteUnit(unit) { success ->
                            if (success) {
                                unitAdapter.removeItem(unit)
                            } else {
                                // Handle error
                            }
                        }
                    }
                )
                binding.recyclerView.adapter = unitAdapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchUnitsFromFirebase() {
        firestore.collection("AllUnits")
            .get()
            .addOnSuccessListener { result ->
                unitList.clear() // Clear the list before adding new items
                for (document in result) {
                    val unit = document.toObject(Units::class.java)
                    unitList.add(unit)
                }
                unitAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    fun deleteUnit(unit: Units, callback: (Boolean) -> Unit) {
        val unitId = unit.id ?: run {
            Toast.makeText(this, "Unit ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        firestore.collection("AllUnits").document(unitId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Unit deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete unit: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                callback(false)
            }
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerview()
        fetchUnitsFromFirebase() // Refresh the units list
    }

    private fun setupRecyclerview() {
        // Set up RecyclerView
        unitAdapter = UnitAdapter(
            units = unitList,
            this,
            onDeleteClicked = { unit ->
                deleteUnit(unit) { success ->
                    if (success) {
                        unitAdapter.removeItem(unit)
                    } else {
                        // Handle error
                    }
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UnitActivity)
            adapter = unitAdapter
        }
    }
}
