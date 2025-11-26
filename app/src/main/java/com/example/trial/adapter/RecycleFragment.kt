package com.example.trial.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trial.R

class RecycleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recycle, container, false)

        // 1. Obtener referencia del RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        // 2. Crear datos de ejemplo (puede ser una lista de ExpenseEntity)
        val data = listOf("Primer objeto", "Segundo objeto", "Tercer objeto")

        // 3. Configurar adapter
        val adapter = RecycleAdapter(data)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }
}
