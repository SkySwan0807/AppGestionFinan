package com.example.trial.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trial.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecycleFragment : Fragment() {

    private val viewModel: RecycleViewModel by viewModels()
    private lateinit var adapter: RecycleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recycle, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = RecycleAdapter(emptyList())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        observeData()

        return view
    }

    private fun observeData() {
        viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            adapter.updateData(expenses)
        }
    }
}
