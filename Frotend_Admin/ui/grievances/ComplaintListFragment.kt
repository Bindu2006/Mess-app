package com.example.myapplication.ui.grievances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.GrievanceItemAdapter
import com.example.myapplication.databinding.FragmentComplaintListBinding

class ComplaintListFragment : Fragment() {
    private var _binding: FragmentComplaintListBinding? = null
    private val binding get() = _binding!!
    private val args: ComplaintListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            subcategoryTitle.text = args.subcategory.title
            complaintsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = GrievanceItemAdapter(args.subcategory.complaints) { complaint ->
                    val action = ComplaintListFragmentDirections
                        .actionComplaintListToComplaintDetails(complaint)
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 