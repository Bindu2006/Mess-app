package com.example.myapplication.ui.grievances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.SubCategoryAdapter
import com.example.myapplication.databinding.FragmentGrievanceDetailsBinding
import com.example.myapplication.model.GrievanceItem
import com.example.myapplication.model.SubCategory

class GrievanceDetailsFragment : Fragment() {
    private var _binding: FragmentGrievanceDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: GrievanceDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrievanceDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.categoryTitle.text = args.category.title
        binding.complaintsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SubCategoryAdapter(args.category.subcategories) { subcategory ->
                navigateToSubcategoryComplaints(subcategory)
            }
        }
    }

    private fun navigateToSubcategoryComplaints(subcategory: SubCategory) {
        val action = GrievanceDetailsFragmentDirections
            .actionGrievanceDetailsToComplaintList(subcategory)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 